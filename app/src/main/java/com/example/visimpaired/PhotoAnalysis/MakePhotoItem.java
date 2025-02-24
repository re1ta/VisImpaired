package com.example.visimpaired.PhotoAnalysis;

import android.app.Activity;
import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

public class MakePhotoItem extends Item implements LifecycleItem {

    private Activity activity;

    public MakePhotoItem(String name, Activity activity) {
        super(name);
        this.activity = activity;
    }

    @Override
    public void onEnter() {
        ((MainActivity) activity).takePhoto();
    }

    @Override
    public void loadItems() {
    }
}
