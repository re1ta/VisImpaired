package com.example.visimpaired.VoiceAssistant

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.example.visimpaired.MainActivity
import com.example.visimpaired.Settings.SettingsHelper
import com.example.visimpaired.TTSConfig
import com.example.visimpaired.Weather.WeatherForcastItem

class VoiceAssistantService : Service() {
    private val binder = LocalBinder()
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TTSConfig
    private val handler = Handler(Looper.getMainLooper())
    private var recognitionRunnable: Runnable? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var settingsHelper: SettingsHelper
    private lateinit var mailVoice : MailVoiceControl

    companion object {
        private const val LISTENING_DELAY = 100L
    }

    override fun onCreate() {
        super.onCreate()
        initializeSpeechRecognizer()
        startForegroundService()
        //scheduleRecognition()
    }

    private fun startForegroundService() {
        val channel = NotificationChannel("voice_assistant_channel", "Голосовой помощник", NotificationManager.IMPORTANCE_NONE)
            .apply { description = "Фоновое распознавание голоса" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "voice_assistant_channel").build()
        startForeground(1, notification)
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    processResults(results)
                }
                override fun onError(error: Int) {
                    when(error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> println("Не вижу слов")
                    }
                    println("ошибка + $error")
                    (activity as MainActivity).wakeWordDetector.startListening()
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {
                    println("жду команды")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun scheduleRecognition() {
        recognitionRunnable?.let { handler.removeCallbacks(it) }
        recognitionRunnable = Runnable {
            startListening()
        }.also {
            handler.postDelayed(it, LISTENING_DELAY)
        }
        (activity as MainActivity).wakeWordDetector.stopListening()
    }

    inner class LocalBinder : Binder() {
        fun getService(): VoiceAssistantService = this@VoiceAssistantService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun initializeTTS(context: Context, activity: Activity) {
        tts = TTSConfig.getInstance(context)
        this.context = context
        this.activity = activity
        mailVoice = MailVoiceControl(context, activity)
        settingsHelper = SettingsHelper(activity, context, tts)
    }

    fun startListening() {
         try {
             val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                 putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                 putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                 putExtra("android.speech.extra.GET_AUDIO", false)
                 putExtra("android.speech.extra.DICTATION_MODE", true)
                 putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", ArrayList<String>())
                 putExtra("android.speech.extra.EXTRA_ENABLE_FORMATTING", false)
                 putExtra("android.speech.extra.EXTRA_SUPPRESS_SOUND", true)  // Ключевой параметр
             }
             speechRecognizer.startListening(intent)
         } catch (_: Exception) {
             scheduleRecognition()
         }
    }

    private fun processResults(results: Bundle) {
        val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.lowercase() ?: return
        println(text)
        processCommand(text)
    }

    private fun processCommand(command: String) {
        when {
            //С интернетом
            command.contains("погод") -> sayTodayWeather(command)
            command.contains("камер") -> sayTextFromCamera()
            command.contains("галер") -> sayTextFromGallery()
            mailCommands(command) -> mailVoice.routeCommand(command)
            //Без интернета
            command.contains("выключ") -> turnOffOnCommands(command, false)
            command.contains("включ") -> turnOffOnCommands(command, true)
            command.contains("врем") -> settingsHelper.getCurrentTime()
            isDayCommand(command) -> settingsHelper.getDateWithDayOfWeek()
            command.contains("заряд") -> settingsHelper.speakBatteryLevel(context)
            command.contains("bluetooth") -> settingsHelper.speakBluetoothStatus(context)
            command.contains("wifi") -> settingsHelper.speakWifiStatus(context)
            else ->  {
                tts.speak("Такой команды нет")
                (activity as MainActivity).wakeWordDetector.startListening()
            }
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun mailCommands(command: String) : Boolean {
        return when {
            command.contains("последн") && command.contains("письм") -> true
            command.contains("след") && command.contains("письм") -> true
            command.contains("пред") && command.contains("письм") -> true
            command.contains("перв") && command.contains("письм") -> true
            else -> false
        }
    }

    private fun isDayCommand(command: String): Boolean {
        return when {
            command.contains("дат") -> true
            command.contains("день") -> true
            command.contains("число") -> true
            else -> false
        }
    }

    private fun turnOffOnCommands(command: String, isTurnOn : Boolean) {
        when {
            command.contains("фонарик") -> settingsHelper.toggleFlashlight(isTurnOn)
            command.contains("bluetooth") -> settingsHelper.bluetoothEnabled(context)
        }
    }

    private fun sayTodayWeather(text : String) {
        if (text.split(" ").size > 1) {
            WeatherForcastItem("1", context, text.split(" ")[1]).onEnter()
        } else {
            val shard = activity.getPreferences(MODE_PRIVATE).all
            val city = shard["city"] as String?
            if (city != null) {
                WeatherForcastItem("1", context, city).onEnter()
            } else {
                tts.speak("После слова погода нужно сказать название города") { startListening() }
            }
        }
    }

    private fun sayTextFromGallery() {
        (activity as MainActivity).choosePhoto()
    }

    private fun sayTextFromCamera() {
        (activity as MainActivity).takePhoto()
    }

}