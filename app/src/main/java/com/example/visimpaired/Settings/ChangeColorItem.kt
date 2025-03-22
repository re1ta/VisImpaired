package com.example.visimpaired.Settings

import android.content.Context
import android.content.SharedPreferences
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.MainActivity
import com.example.visimpaired.Menu.Item

class ChangeColorItem(context: Context?, name: String?, private val color : Int) : Item(context, name), LifecycleItem {

    private val shard: SharedPreferences = (context as MainActivity).activity.getPreferences(Context.MODE_PRIVATE)

    override fun loadItems() {
        (context as MainActivity).changeColorButtons(color)
        val editor = shard.edit()
        if (shard.all.containsKey("color")) {
            editor.remove("color")
        }
        editor.putInt("color", color)
        editor.apply()
    }
}