package com.example.visimpaired.Weather

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import com.example.visimpaired.TTSConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class WeatherForcastItem( name: String?, val context: Context?, private val city: String?) : Item(name), LifecycleItem{

    private val command = name;

    override fun loadItems() {}

    override fun onEnter() {
        pronouceWeather(city)
    }
    private fun pronouceWeather(city: String?){
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val url = HttpUrl.Builder()
                .scheme("http")
                .host("dataservice.accuweather.com")
                .addPathSegment("locations")
                .addPathSegment("v1")
                .addPathSegment("cities")
                .addPathSegment("search")
                .addQueryParameter("apikey", "ELeqtK0nneaAniLzElHGj6k1yZGGn1Fk")
                .addQueryParameter("q", city)
                .addQueryParameter("language", "ru-ru")
                .build()

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val jsonArray = JSONArray(responseBody)
                                val firstObject = jsonArray.getJSONObject(0)
                                val key = firstObject.getString("Key")
                                getForcast(key)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            println("Response body is null")
                        }
                    } else {
                        println("Request failed with code: ${response.code}")
                    }
                }
            })
        }
    }

    fun getForcast(locationKey: String?){
        val address1 : String
        val address2 : String
        if (command!!.contains("5")){
            address1 = "daily"
            address2 = "5day"
        } else{
            address1 = "hourly"
            address2 = "1hour"
        }
        val client = OkHttpClient()
        val url = HttpUrl.Builder()
            .scheme("http")
            .host("dataservice.accuweather.com")
            .addPathSegment("forecasts")
            .addPathSegment("v1")
            .addPathSegment(address1)
            .addPathSegment(address2)
            .addPathSegment(locationKey!!)
            .addQueryParameter("apikey", "ELeqtK0nneaAniLzElHGj6k1yZGGn1Fk")
            .addQueryParameter("language", "ru-ru")
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (command.contains("5")){
                            get5DailyBody(responseBody)
                        } else {
                            get1HourBody(responseBody)
                        }
                    } else {
                        println("Response body is null")
                    }
                } else {
                    println("Request failed with code: ${response.code}")
                }
            }
        })
    }

    private fun parseTemp(temperatureValue : Double) : Int{
        return ((temperatureValue - 32) * 5 / 9).roundToInt()
    }

    private fun formatDateToText(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        val dateTime = OffsetDateTime.parse(dateString, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
        return dateTime.format(outputFormatter)
    }

    fun get1HourBody(response: String){
        try {
            val jsonArray = JSONArray(response)
            val firstObject = jsonArray.getJSONObject(0)
            val iconPhrase = firstObject.getString("IconPhrase")
            val temperatureObject = firstObject.getJSONObject("Temperature")
            val temperatureValue = temperatureObject.getDouble("Value")
            val temperatureUnit = temperatureObject.getString("Unit")
            if (temperatureUnit == "F") {
                val celsius = parseTemp(temperatureValue)
                TTSConfig.getInstance(context).speak("Температура в городе $city $celsius градусов, а погодные условия $iconPhrase")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun get5DailyBody(response: String){
        try {
            val jsonObject = JSONObject(response)
            var textToPronounce = "$city: "
            val dailyForecasts = jsonObject.getJSONArray("DailyForecasts")
            for (i in 0 until dailyForecasts.length()) {
                val forecast = dailyForecasts.getJSONObject(i)
                val date = formatDateToText(forecast.getString("Date"))
                val minTemp = parseTemp(forecast.getJSONObject("Temperature").getJSONObject("Minimum").getDouble("Value"))
                val maxTemp = parseTemp(forecast.getJSONObject("Temperature").getJSONObject("Maximum").getDouble("Value"))
                val dayPhrase = forecast.getJSONObject("Day").getString("IconPhrase")
                val nightPhrase = forecast.getJSONObject("Night").getString("IconPhrase")
                textToPronounce += "$date, температура от $minTemp до $maxTemp градусов, день $dayPhrase, ночь $nightPhrase. "
            }
            TTSConfig.getInstance(context).speak(textToPronounce)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}