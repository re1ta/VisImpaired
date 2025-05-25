package com.example.visimpaired.Weather;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChooseWeatherForcastList extends Item implements LifecycleItem {

    private Context context;
    private String name;

    public ChooseWeatherForcastList(String name, Context context) {
        super(context, name, "Выберите прогноз");
        this.name = name;
        this.context = context;
    }

    @Override
    public void loadItems() {
        SharedPreferences shard = ((MainActivity) context).getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = shard.edit();
        if(shard.getAll().containsKey("city")) {
            editor.remove("city");
        }
        editor.putString("city", name);
        editor.apply();
        LinkedHashMap<String, Item> typeForcast = new LinkedHashMap<>();
        typeForcast.put("Погода на сегодня", new WeatherForcastItem("Погода на сегодня", context, name));
        typeForcast.put("Погода на 5 дней", new WeatherForcastItem("Погода на 5 дней", context, name));
        this.setItems(typeForcast);
    }
}
