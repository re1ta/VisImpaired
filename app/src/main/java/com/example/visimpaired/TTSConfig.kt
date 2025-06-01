package com.example.visimpaired

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TTSConfig(private val context: Context?) : OnInitListener {

    var textToSpeech: TextToSpeech? = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech!!.language = Locale("ru", "RU")
        } else {
            println("Ошибка инициализации языка для ТТС")
        }
    }

    fun speak(text: String?) {
        if (textToSpeech != null) {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (textToSpeech == null || text.isEmpty()) {
            onComplete?.invoke()
            return
        }
        textToSpeech?.stop()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)?.let { result ->
            if (result == TextToSpeech.ERROR) {
                onComplete?.invoke()
            } else {
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        onComplete?.invoke()
                    }
                    override fun onError(utteranceId: String?) {
                        onComplete?.invoke()
                    }
                    override fun onStart(utteranceId: String?) {}
                })
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        if (textToSpeech != null) {
            textToSpeech!!.setSpeechRate(rate)
        }
    }

    companion object {
        private var instance: TTSConfig? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context?): TTSConfig {
            if (instance == null) {
                instance = TTSConfig(context)
            }
            return instance!!
        }
    }
}
