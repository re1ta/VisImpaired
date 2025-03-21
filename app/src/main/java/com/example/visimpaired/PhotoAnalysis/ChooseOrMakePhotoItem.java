package com.example.visimpaired.PhotoAnalysis;

import android.app.Activity;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;

public class ChooseOrMakePhotoItem extends Item implements LifecycleItem {

    private Activity activity;
    private int countEnter = 0;

    public ChooseOrMakePhotoItem(String name, Activity activity) {
        super(name);
        this.activity = activity;
    }

    @Override
    public void loadItems() {
        countEnter += 1;
        System.out.println(countEnter);
        LinkedHashMap<String, Item> variants = new LinkedHashMap<>();
        variants.put("Сделать фото", new MakePhotoItem("Сделать фото", activity));
        variants.put("Выбрать из галереи", new ChoosePhotoItem("Выбрать из галереи", activity));
        this.setItems(variants);
    }
}
