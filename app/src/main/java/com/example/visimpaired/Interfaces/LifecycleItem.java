package com.example.visimpaired.Interfaces;
import com.example.visimpaired.Menu.Item;
import com.example.visimpaired.TTSConfig;

public interface LifecycleItem {

    default void onEnter(){
        loadItems();
        Item item = (Item) this;
        if (item.getDescription() != null) {
            TTSConfig.getInstance(item.getContext()).speak(item.getDescription());
        } else {
            TTSConfig.getInstance(item.getContext()).speak(item.getName());
        }
    }

    default void onEscape() {
        if (this instanceof Item) {
            Item item = (Item) this;
            if (item.getItems() != null) {
                item.getItems().clear();
            }
        }
    }

    void loadItems();
}
