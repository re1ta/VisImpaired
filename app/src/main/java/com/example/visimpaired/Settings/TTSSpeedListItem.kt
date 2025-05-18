package com.example.visimpaired.Settings

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item

class TTSSpeedListItem(context: Context?, name: String?) : Item(context, "Выберите скорость"), LifecycleItem {
    override fun loadItems() {
        val speed = LinkedHashMap<String, Item>()
        var i = 0.0f
        var text: String
        while (i <= 2.0f) {
            i += 0.1f
            text = ((i*100).toInt()).toString() + " процентов"
            speed[text] = ChangeTTSSpeedItem(context, text, i)
        }
        items = speed
    }
}