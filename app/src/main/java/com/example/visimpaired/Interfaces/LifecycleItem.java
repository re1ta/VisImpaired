package com.example.visimpaired.Interfaces;
import com.example.visimpaired.Menu.Item;

public interface LifecycleItem {

    void onEnter();

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
