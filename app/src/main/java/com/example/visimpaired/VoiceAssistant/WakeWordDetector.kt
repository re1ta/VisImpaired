package com.example.visimpaired.VoiceAssistant

import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.content.Context
import android.util.Log
import com.example.visimpaired.MainActivity
import com.example.visimpaired.VoiceAssistant.AssetUtils.copyAssetToCache


class WakeWordDetector {

    private var porcupineManager: PorcupineManager? = null
    private val accessKey = "LFFg2Euj+F5NdVXee/bDTqjMuP3gWS5lRcIP+NMIgswcxSPx2gOtPg=="
    private val assetKeywordPath = "OK-Voice_en_android_v3_0_0.ppn"
    private lateinit var context : Context

    fun initPorcupine(context: Context) {
        val keywordFilePath = copyAssetToCache(context, assetKeywordPath)
        this.context = context
        if (keywordFilePath == null) {
            Log.e("WakeWordDetector", "Failed to copy keyword file from assets")
            return
        }
        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeywordPaths(arrayOf<String>(keywordFilePath))
                .build(context, wakeWordcallback)
        } catch (e: Exception) {
            Log.e("WakeWordDetector", "Error initializing Porcupine", e)
        }
    }

    var wakeWordcallback: PorcupineManagerCallback = PorcupineManagerCallback { keywordIndex: Int ->
        println(keywordIndex)
        if (keywordIndex == 0) {
            stopListening()
            (context as MainActivity).voiceService.startListening()
        }
    }

    fun startListening() {
        if (porcupineManager != null) {
            try {
                porcupineManager!!.start()
            } catch (e: Exception) {
                Log.e("WakeWordDetector", "Error starting Porcupine", e)
            }
        }
    }

    fun stopListening() {
        if (porcupineManager != null) {
            try {
                porcupineManager!!.stop()
            } catch (e: Exception) {
                Log.e("WakeWordDetector", "Error stopping Porcupine", e)
            }
        }
    }

    fun release() {
        if (porcupineManager != null) {
            porcupineManager!!.delete()
        }
    }
}