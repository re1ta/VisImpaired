package com.example.visimpaired.Settings

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import java.util.LinkedHashMap

class SettingsList(context: Context?, name: String?) : Item(context, name), LifecycleItem {
    override fun loadItems() {
        val settings = LinkedHashMap<String, Item>()
        settings["Поменять цвет интерфейса"] = ColorList(context, "Поменять цвет интерфейса")
        settings["Поменять скорость воспроизведение синтезатора речи"] = TTSSpeedListItem(context, "Поменять скорость воспроизведение синтезатора речи")
        items = settings
    }
}