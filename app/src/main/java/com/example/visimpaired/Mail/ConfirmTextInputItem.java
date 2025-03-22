package com.example.visimpaired.Mail;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

public class ConfirmTextInputItem extends Item implements LifecycleItem {

    private Context context;
    private String field;
    private SharedPreferences shard;

    public ConfirmTextInputItem(String name, Context context, String field) {
        super(context, name);
        this.context = context;
        this.field = field;
        this.shard = ((MainActivity) context).getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onEnter() {
        SharedPreferences.Editor editor = shard.edit();
        if(shard.getAll().containsKey(field)) {
            editor.remove(field);
        }
        editor.putString(field, ((MainActivity) context).getTextInput());
        editor.apply();
    }

    @Override
    public void loadItems() {

    }
}
