package com.example.visimpaired.Menu;

import android.content.Context;

import com.example.visimpaired.Interfaces.End;
import com.example.visimpaired.Interfaces.LifecycleItem;

import java.util.LinkedHashMap;

public class Item {

    private String name;
    private String description;
    private LinkedHashMap<String, Item> items;
    private Item parent;
    private Context context;

    public Item(Context context, String name, String description) {
        this.context = context;
        this.name = name;
        this.description = description;
        this.items = null;
        this.parent = null;
    }

    public Item(Context context, String name) {
        this.context = context;
        this.name = name;
        this.items = null;
        this.parent = null;
    }

    public Item(Context context, String name, LinkedHashMap<String, Item> items) {
        this.context = context;
        this.name = name;
        this.items = items;
        this.parent = null;
        for (Item child : items.values()) {
            child.setParent(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() { return description; }

    public LinkedHashMap<String, Item> getItems() {
        return items;
    }

    public void setItems(LinkedHashMap<String, Item> items) {
        this.items = items;
        for (Item child : items.values()) {
            child.setParent(this);
        }
    }

    public Item getParent() {
        return parent;
    }

    public void setParent(Item parent) {
        this.parent = parent;
    }

    public Context getContext() { return context; }

    public boolean isMenu() {
        return (items != null && !items.isEmpty()) || !(this instanceof End);
    }

    public Item findByName(String name){
        return items.get(name);
    }
}