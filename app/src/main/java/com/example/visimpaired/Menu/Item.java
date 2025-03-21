package com.example.visimpaired.Menu;

import com.example.visimpaired.Interfaces.LifecycleItem;

import java.util.LinkedHashMap;

public class Item {

    private String name;
    private LinkedHashMap<String, Item> items;
    private Item parent;

    public Item(String name) {
        this.name = name;
        this.items = null;
        this.parent = null;
    }

    public Item(String name, LinkedHashMap<String, Item> items) {
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

    public boolean isMenu() {
        return (items != null && !items.isEmpty()) || (this instanceof LifecycleItem);
    }

    public Item findByName(String name){
        return items.get(name);
    }
}