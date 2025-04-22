package com.example.visimpaired.VoiceAssistant

import android.app.Activity
import android.app.Notification
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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.visimpaired.MainActivity
import com.example.visimpaired.TTSConfig
import com.example.visimpaired.Weather.WeatherForcastItem

class VoiceAssistantService : Service() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private val binder = LocalBinder()
    private lateinit var tts: TTSConfig
    private var isWaitingForWakeWord = true
    private lateinit var context: Context
    private lateinit var activity: Activity

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "voice_assistant_channel"
        private val WAKE_WORD = arrayOf("визи", "вези", "виз", "вез", "везе", "визе", "безе", "бези", "бизе", "бизи")
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)?.lowercase() ?: return
                    if (isWaitingForWakeWord) {
                        if (findWakeWord(text)) {
                            onWakeWordDetected(text)
                        }
                    } else {
                        processCommand(text, false)
                    }
                }

                override fun onEndOfSpeech() {
                    if (!isWaitingForWakeWord) {
                        startListening(commandMode = true)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    return
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    return
                }

                override fun onReadyForSpeech(params: Bundle?) {
                    return
                }

                override fun onBeginningOfSpeech() {
                    return
                }

                override fun onRmsChanged(rmsdB: Float) {
                    return
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    return
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        else -> "Unknown error: $error"
                    }
                    Log.e("VoiceAssistant", errorMessage)
                    if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                        startListening(isWaitingForWakeWord)
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({ startListening(isWaitingForWakeWord) },  1 * 1000)
                    }
                }
            })
        }

        startListening()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Voice Assistant", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Voice command recognition"
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Голосовой помощник")
            .setContentText("Слушает команды...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
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
    }

    private fun onWakeWordDetected(command: String) {
        processCommand(command, true)
    }

    private fun startListening(commandMode: Boolean = false) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            if (commandMode) {
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            } else {
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
        }
        speechRecognizer.startListening(intent)
    }

    private fun processCommand(command: String, isWithVisi : Boolean) {
        isWaitingForWakeWord = true
        when {
            command.contains("погод") -> sayTodayWeather(command)
            command.contains("камер") -> sayTextFromCamera()
            command.contains("галер") -> sayTextFromGallery()
            else -> if (isWithVisi) { isWaitingForWakeWord = false }
                    else { tts.speak("Такой команды нет") }
        }
        startListening(commandMode = true)
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun sayTodayWeather(text : String) {
        WeatherForcastItem("1", context, text.split(" ")[1]).onEnter()
    }

    private fun sayTextFromGallery() {
        (activity as MainActivity).choosePhoto()
    }

    private fun sayTextFromCamera() {
        (activity as MainActivity).takePhoto()
    }
}