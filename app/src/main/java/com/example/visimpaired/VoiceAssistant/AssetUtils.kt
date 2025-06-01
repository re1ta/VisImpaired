package com.example.visimpaired.VoiceAssistant

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object AssetUtils {

    fun copyAssetToCache(context: Context, assetPath: String): String? {
        val fileName = File(assetPath).getName()
        val outputFile = File(context.cacheDir, fileName)
        if (outputFile.exists()) {
            return outputFile.absolutePath
        }
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while ((inputStream.read(buffer).also { length = it }) > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    return outputFile.absolutePath
                }
            }
        } catch (e: IOException) {
            Log.e("AssetUtils", "Error copying asset file: $assetPath", e)
            return null
        }
    }
}