package com.example.visimpaired.Mail;

import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;

public class EnterMailItem extends Item implements LifecycleItem {

    private Context context;

    public EnterMailItem(Context context, String name) {
        super(context, "Влевом вверхнем углу появилось поле для ввода для данных");
        this.context = context;
    }

    @Override
    public void onEscape() {
        ((MainActivity) context).disableKeyboard();
        LifecycleItem.super.onEscape();
    }

    @Override
    public void loadItems() {
        ((MainActivity) context).enableKeyboard();
        LinkedHashMap<String, Item> saveAction = new LinkedHashMap<>();
        saveAction.put("Войти в почту", new MailFolderList("Войти в почту", context));
        saveAction.put("Сохранить логин", new ConfirmTextInputItem("Подтвердить логин", context, "login"));
        saveAction.put("Сохранить пароль", new ConfirmTextInputItem("Подтвердить пароль", context, "password"));
        this.setItems(saveAction);
    }

}
