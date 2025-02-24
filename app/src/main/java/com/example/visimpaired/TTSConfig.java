package com.example.visimpaired;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSConfig implements TextToSpeech.OnInitListener {

    public TextToSpeech textToSpeech;
    private static TTSConfig instance;

    public TTSConfig(Context context) {
        this.textToSpeech = new TextToSpeech(context, this);
    }

    public static synchronized TTSConfig getInstance(Context context) {
        if (instance == null) {
            instance = new TTSConfig(context);
        }
        return instance;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(new Locale("ru", "RU"));
        } else {
            System.out.println("Ошибка инициализации языка для ТТС");
        }
    }

    public void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
