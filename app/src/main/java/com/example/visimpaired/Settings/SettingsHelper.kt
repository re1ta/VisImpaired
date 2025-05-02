package com.example.visimpaired.Settings

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visimpaired.TTSConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SettingsHelper(private val activity: Activity, private val context : Context, private val tts : TTSConfig) {

    fun toggleFlashlight(enabled: Boolean) {
        val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], enabled)
        } catch (e: Exception) {
            tts.speak("Ошибка фонарика")
        }
    }

     fun speakBluetoothStatus(context: Context) {
        val message = if (isBluetoothEnabled(context)) {
            "Блютуз включён"
        } else {
            "Блютуз выключен"
        }
        tts.speak(message)
    }

    fun speakWifiStatus(context: Context) {
        val message = if (isWifiEnabled(context)) {
            "Вай-фай включён"
        } else {
            "Вай-фай выключен"
        }
        tts.speak(message)
    }

    private fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

     fun speakBatteryLevel(context : Context) {
        val percent = getCurrentBatteryLevel(context)
        val message = when {
            percent >= 80 -> "Батарея почти полная, $percent процентов"
            percent >= 30 -> "Уровень заряда $percent процентов"
            percent >= 15 -> "Заряд батареи $percent процентов, рекомендуется подключить зарядку"
            else -> "Критический уровень заряда, $percent процентов, срочно подключите зарядное устройство"
        }
        tts.speak(message)
    }

    fun getCurrentTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        tts.speak("Сейчас " + String.format("%02d:%02d", hour, minute))
    }

    private fun getCurrentBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getDateWithDayOfWeek() {
        val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ru"))
            .format(Date())
        tts.speak(date.replaceFirstChar { it.uppercase() })
    }

    fun bluetoothEnabled(context: Context) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            activity.startActivityForResult(enableBtIntent, 1)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)
        }
    }
}