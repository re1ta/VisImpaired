package com.example.visimpaired.Weather;

import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;

public class ChooseWeatherForcastList extends Item implements LifecycleItem {

    private Context context;
    private String name;

    public ChooseWeatherForcastList(String name, Context context) {
        super(context, name);
        this.name = name;
        this.context = context;
    }

    @Override
    public void loadItems() {
        LinkedHashMap<String, Item> typeForcast = new LinkedHashMap<>();
        typeForcast.put("Погода на сегодня", new WeatherForcastItem("Погода на сегодня", context, name));
        typeForcast.put("Погода на 5 дней", new WeatherForcastItem("Погода на 5 дней", context, name));
        this.setItems(typeForcast);
    }
}
