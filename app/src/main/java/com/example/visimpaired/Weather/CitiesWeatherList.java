package com.example.visimpaired.Weather;


import android.app.Activity;
import android.content.res.XmlResourceParser;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.Menu.Item;
import com.example.visimpaired.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

public class CitiesWeatherList extends Item implements LifecycleItem {

    private Activity activity;

    public CitiesWeatherList(String name, Activity activity) {
        super(name);
        this.activity = activity;
    }


    @Override
    public void loadItems() {
        parseCities();
    }

    private void parseCities(){
        LinkedHashMap<String, Item> cities = new LinkedHashMap<>();
        try (XmlResourceParser parser = activity.getResources().getXml(R.xml.cities)) {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "city".equals(parser.getName())) {
                    if (parser.next() == XmlPullParser.TEXT) {
                        String name = parser.getText();
                        cities.put(name, new ChooseWeatherForcastList(name, activity));
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        this.setItems(cities);
    }
}
