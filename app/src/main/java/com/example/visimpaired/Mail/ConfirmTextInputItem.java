package com.example.visimpaired.Mail;

import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

public class ConfirmTextInputItem extends Item implements LifecycleItem {

    private Context context;
    private String field;

    public ConfirmTextInputItem(String name, Context context, String field) {
        super(context, name);
        this.context = context;
        this.field = field;
    }

    @Override
    public void onEnter() {
        switch (field) {
            case "login":
                ((EnterMailItem) getParent()).setLogin(((MainActivity) context).getTextInput());
                break;
            case "password":
                ((EnterMailItem) getParent()).setPassword(((MainActivity) context).getTextInput());
                break;
        }
    }

    @Override
    public void loadItems() {

    }
}
