package com.example.visimpaired.Mail;

import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;

public class EnterMailItem extends Item implements LifecycleItem {

    private Context context;
    private String login;
    private String password;

    public EnterMailItem(String name, Context context) {
        super(name);
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
        saveAction.put("Сохранить логин", new ConfirmTextInputItem("Подтвердить логин", context, "login"));
        saveAction.put("Сохранить пароль", new ConfirmTextInputItem("Подтвердить пароль", context, "password"));
        saveAction.put("Подтвердить почту", new MailFolderList("Подтвердить почту", context));
        this.setItems(saveAction);
    }

    public void setPassword(String password) { this.password = password; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public String getLogin() { return login; }
}
