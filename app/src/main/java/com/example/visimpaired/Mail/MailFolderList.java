package com.example.visimpaired.Mail;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.Menu.Item;

import java.util.LinkedHashMap;

public class MailFolderList extends Item implements LifecycleItem {

    public MailFolderList(String name) {
        super(name);
    }

    @Override
    public void onEnter() {
        loadItems();
    }

    @Override
    public void loadItems() {
        LinkedHashMap<String, Item> folders = new LinkedHashMap<>();
        folders.put("Входящие", new Item("Входящие"));
        folders.put("Отправленные", new Item("Отправленные"));
        folders.put("Корзина", new Item("Корзина"));
        this.setItems(folders);
    }
}
