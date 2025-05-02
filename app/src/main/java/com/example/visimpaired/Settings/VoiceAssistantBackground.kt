package com.example.visimpaired.Settings

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import androidx.core.content.edit
import com.example.visimpaired.TTSConfig

class VoiceAssistantBackground(context: Context, private val activity: Activity, name: String?) : Item(context, name), LifecycleItem {

    override fun loadItems() {}
    override fun onEnter() {
        val shard = activity.getPreferences(MODE_PRIVATE)
        val isEnable = shard.all.getOrDefault("isVoiceAssistantBackgroundEnable", false) as Boolean?
        shard.edit { putBoolean("isVoiceAssistantBackgroundEnable", !isEnable!!) }
        if (isEnable!!)
            this.name = "Голосовой ассистент вне приложения включён"
        else
            this.name = "Голосовой ассистент вне приложения выключён"
    }
}