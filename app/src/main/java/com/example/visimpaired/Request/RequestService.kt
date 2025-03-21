package com.example.visimpaired.Request

import android.content.Context
import com.example.visimpaired.Request.DTO.ImageToTextResponse
import com.example.visimpaired.TTSConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class RequestService( private var context : Context){

    private var API_URL: String = "https://www.imgocr.com/api/imgocr_get_text"
    private var API_KEY: String = "786d632f51d38fe61ef9169f485cf6b9"

    fun fetchData(base64_image : String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = doRequestToITT(API_URL, jsonInputString = "{\"api_key\": \"${API_KEY}\", \"image\": \"${base64_image}\"}"
                )
                if (response.message == "success") {
                    TTSConfig.getInstance(context).speak(response.text)
                    println("Распознанный текст: ${response.text}")
                } else {
                    TTSConfig.getInstance(context).speak(response.error)
                    println("Ошибка: ${response.error}")
                }
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
            }
        }
    }

    private suspend fun doRequestToITT(urlString: String, jsonInputString: String): ImageToTextResponse {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(JSON, jsonInputString)
            val request = Request.Builder()
                .url(urlString)
                .post(body)
                .build()

            try {
                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Ошибка при выполнении запроса: ${response.code}")
                }
                val gson = Gson()
                gson.fromJson(response.body?.charStream(), ImageToTextResponse::class.java)
            } catch (e: IOException) {
                throw RuntimeException("Ошибка сети: ${e.message}", e)
            }
        }
    }
}