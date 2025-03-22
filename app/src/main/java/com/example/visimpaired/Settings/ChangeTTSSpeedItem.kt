package com.example.visimpaired.Settings

import android.content.Context
import android.content.SharedPreferences
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.MainActivity
import com.example.visimpaired.Menu.Item
import com.example.visimpaired.TTSConfig

class ChangeTTSSpeedItem(context: Context?, name: String?, private val rate: Float) : Item(context, name), LifecycleItem {

    private val shard: SharedPreferences = (context as MainActivity).activity.getPreferences(Context.MODE_PRIVATE)

    override fun loadItems() {
        TTSConfig.getInstance(context).setSpeechRate(rate)
        val editor = shard.edit()
        if (shard.all.containsKey("speed")) {
            editor.remove("speed")
        }
        editor.putFloat("speed", rate)
        editor.apply()
    }
}