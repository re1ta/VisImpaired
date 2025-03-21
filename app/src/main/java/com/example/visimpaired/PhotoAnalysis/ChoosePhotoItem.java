package com.example.visimpaired.PhotoAnalysis;

import android.app.Activity;

import com.example.visimpaired.Interfaces.End;
import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

public class ChoosePhotoItem extends Item implements LifecycleItem, End {

    private Activity activity;

    public ChoosePhotoItem(String name, Activity activity) {
        super(activity, name);
        this.activity = activity;
    }

    @Override
    public void onEnter() {
        ((MainActivity) activity).choosePhoto();
    }

    @Override
    public void loadItems() {}
}
