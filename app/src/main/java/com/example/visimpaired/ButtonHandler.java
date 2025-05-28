package com.example.visimpaired;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.widget.Button;

import com.example.visimpaired.Menu.Item;
import com.example.visimpaired.Menu.Menu;
import com.example.visimpaired.Interfaces.LifecycleItem;

import java.util.HashMap;
import java.util.List;

public class ButtonHandler{

    private final Context context;
    private Vibrator vibrator;
    private Menu menu;

    public ButtonHandler(Context context, Menu menu) {
        this.context = context;
        this.menu = menu;
        createVibrator();
    }

    public void setupButton(List<Object> buttons) {
        HashMap<Integer, Runnable> actions = new HashMap<>();
        actions.put(R.id.leftButton, withVibrationAndSpeak(() -> menu.changeSelectedItem("left")));
        actions.put(R.id.rightButton, withVibrationAndSpeak(() -> menu.changeSelectedItem("right")));
        actions.put(R.id.confirmButton, withVibrationAndSpeak(this::handleConfirmButton));
        actions.put(R.id.escapeButton, withVibrationAndSpeak(this::handleEscapeButton));
        actions.put(R.id.helpButton, withVibrationAndSpeak(() -> menu.sayHelp()));
        actions.put(R.id.mainButton, withVibrationAndSpeak(() -> menu.goMain()));

        for (Object button : buttons) {
            Button btn = (Button) button;
            Runnable action = actions.get(btn.getId());
            if (action != null) {
                btn.setOnClickListener(v -> action.run());
            } else {
                throw new IllegalArgumentException("Unknown button ID: " + btn.getId());
            }
        }
    }

    private Runnable withVibrationAndSpeak(Runnable action) {
        return () -> {
            vibrate();
            action.run();
        };
    }

    private void handleConfirmButton() {
        Item selectedItem = menu.getCurrentMenu().getItems().get(menu.getSelected());
        if (selectedItem != null) {
            if (selectedItem.isMenu()) {
                menu.enterSelectedItem();
            } else if (selectedItem instanceof LifecycleItem){
                ((LifecycleItem) selectedItem).onEnter();
            }
        }
    }

    private void handleEscapeButton() {
        Item currentMenu = menu.getCurrentMenu();
        if (currentMenu.getParent() != null) {
            menu.goBack();
        } else {
            TTSConfig.getInstance(context).speak(currentMenu.getName());
        }
    }

    private void createVibrator(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

    }

    private void vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }
}
