package com.example.visimpaired.Menu;

import android.content.Context;

import com.example.visimpaired.Interfaces.LifecycleItem;
import com.example.visimpaired.TTSConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Menu {

    private Item currentMenu;
    private String selected;
    private Context context;

    public Menu(Item rootMenu, Context context) {
        this.currentMenu = rootMenu;
        this.selected = getFirstItem();
        this.context = context;
    }

    public void changeSelectedItem(String direction) {
        if (currentMenu.isMenu()) {
            LinkedHashMap<String, Item> items = currentMenu.getItems();
            List<String> keys = new ArrayList<>(items.keySet());
            int currentIndex = keys.indexOf(selected);
            int newIndex = switch (direction) {
                case "left" -> (currentIndex - 1 + keys.size()) % keys.size();
                case "right" -> (currentIndex + 1) % keys.size();
                default -> throw new IllegalArgumentException("Invalid direction: " + direction);
            };
            selected = keys.get(newIndex);
            TTSConfig.getInstance(context).speak(selected);
        }
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        if (currentMenu.isMenu() && currentMenu.getItems().containsKey(selected)) {
            this.selected = selected;
        } else {
            throw new IllegalArgumentException("Invalid selected item: " + selected);
        }
    }

    private String getFirstItem() {
        if (currentMenu.isMenu()) {
            if (currentMenu.getItems() != null) {
                List<String> keys = new ArrayList<>(currentMenu.getItems().keySet());
                return keys.get(0);
            }
            return null;
        }
        return null;
    }

    public void enterSelectedItem() {
        if (currentMenu.isMenu()) {
            Item selectedItem = currentMenu.getItems().get(selected);
            if (selectedItem != null) {
                if (selectedItem instanceof LifecycleItem) {
                    ((LifecycleItem) selectedItem).onEnter();
                }
                if (selectedItem.isMenu()) {
                    Item lastMenu = currentMenu;
                    currentMenu = selectedItem;
                    selected = getFirstItem();
                    if(selected == null){
                        if (selectedItem.getDescription() != null)
                            TTSConfig.getInstance(context).speak(selectedItem.getDescription());
                        else
                            TTSConfig.getInstance(context).speak(selectedItem.getName());
                        currentMenu = lastMenu;
                        selected = getFirstItem();
                    }
                }
            }
        }
    }


    public void goBack() {
        if (currentMenu.getParent() != null) {
            if (currentMenu instanceof LifecycleItem) {
                ((LifecycleItem) currentMenu).onEscape();
            }
            selected = currentMenu.getName();
            currentMenu = currentMenu.getParent();
            if (currentMenu.getDescription() != null) {
                TTSConfig.getInstance(currentMenu.getContext()).speak(currentMenu.getDescription());
            } else {
                TTSConfig.getInstance(currentMenu.getContext()).speak(currentMenu.getName());
            }
        }
    }

    public Item getCurrentMenu() {
        return currentMenu;
    }
}