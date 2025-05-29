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
    private var isWaitingForWakeWord = true
    private val handler = Handler(Looper.getMainLooper())
    private var recognitionRunnable: Runnable? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var settingsHelper: SettingsHelper
    private lateinit var mailVoice : MailVoiceControl
    private var outApp = false

    companion object {
        private const val LISTENING_DELAY = 100L
        private val WAKE_WORD = arrayOf("визи", "вези", "виз", "вез", "везе", "визе", "безе", "бези", "бизе", "бизи")
    }

    override fun onCreate() {
        super.onCreate()
        initializeSpeechRecognizer()
        startForegroundService()
        scheduleRecognition()
    }

    fun setOutApp(status : Boolean) {
        outApp = status
    }

    private fun startForegroundService() {
        val channel = NotificationChannel("voice_assistant_channel", "Голосовой помощник", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "Фоновое распознавание голоса" }

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, "voice_assistant_channel").build()
        startForeground(1, notification)
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    processResults(results)
                    scheduleRecognition()
                }
                override fun onError(error: Int) {
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> handler.postDelayed({
                            scheduleRecognition() }, 1000)
                        else -> scheduleRecognition()
                    }
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

    }

    private fun scheduleRecognition() {
        recognitionRunnable?.let { handler.removeCallbacks(it) }
        recognitionRunnable = Runnable {
            startListening()
        }.also {
            handler.postDelayed(it, LISTENING_DELAY)
        }
    }

    private fun findWakeWord(text : String): Boolean {
        return WAKE_WORD.any { wakeWord -> text.contains(wakeWord) }
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
        if (outApp == false) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra("android.speech.extra.GET_AUDIO", false)
                    putExtra("android.speech.extras.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 0)
                    putExtra("android.speech.extras.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 0)
                    putExtra("android.speech.extras.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 0)
                }
                speechRecognizer.startListening(intent)
            } catch (_: Exception) {
                scheduleRecognition()
            }
        }
    }

    private fun processResults(results: Bundle) {
        val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.lowercase() ?: return
        println(text)
        if (isWaitingForWakeWord) {
            if (findWakeWord(text)) {
                isWaitingForWakeWord = false
                tts.speak("Слушаю вас") {
                    handler.post { startListening() }
                }
            }
        } else {
            processCommand(text, false)
            isWaitingForWakeWord = true
        }
    }

    private fun processCommand(command: String, isWithVisi : Boolean) {
        isWaitingForWakeWord = true
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
            else -> if (isWithVisi) { isWaitingForWakeWord = false }
                    else { tts.speak("Такой команды нет") }
        }
        startListening()
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
                tts.speak("После слова погода нужно сказать название города")
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