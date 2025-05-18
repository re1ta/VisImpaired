package com.example.visimpaired.Settings

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import com.example.visimpaired.R

class ColorList(context: Context?, name: String?) : Item(context, "Выберите цвет"), LifecycleItem {

    override fun loadItems() {
        val colors = LinkedHashMap<String, Item>()
        getAllColors(context).drop(7).forEach {
            colors[it.first] = ChangeColorItem(context, it.first, it.second)
        }
        items = colors
    }

    fun getAllColors(context: Context): List<Pair<String, Int>> {
        val colorList = mutableListOf<Pair<String, Int>>()
        val fields = R.color::class.java.fields

        for (field in fields) {
            val colorName = field.name
            val colorResId = field.getInt(null)
            val colorValue = ContextCompat.getColor(context, colorResId)
            colorList.add(colorName to colorValue)
        }

        return colorList
    }
}