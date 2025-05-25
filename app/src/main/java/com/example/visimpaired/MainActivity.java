package com.example.visimpaired;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.visimpaired.Mail.EnterMailItem;
import com.example.visimpaired.Menu.Menu;
import com.example.visimpaired.Menu.Item;
import com.example.visimpaired.PhotoAnalysis.ChooseOrMakePhotoItem;
import com.example.visimpaired.PhotoAnalysis.PhotoService;
import com.example.visimpaired.Settings.SettingsList;
import com.example.visimpaired.VoiceAssistant.MailVoiceControl;
import com.example.visimpaired.VoiceAssistant.VoiceAssistantService;
import com.example.visimpaired.Weather.CitiesWeatherList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public class MainActivity extends AppCompatActivity {

    private static Menu menu;
    private PhotoService photoService;
    private VoiceAssistantService voiceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.textInput).setVisibility(View.INVISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        menu = createStartMenu();
        new ButtonHandler(getContext(), menu).setupButton(getAllButtons());
        setShardSettings();
        setVoiceAssistant();
    }

    private void setVoiceAssistant() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            int REQUEST_RECORD_AUDIO_PERMISSION = 200;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        initializeVoiceService();
    }

    private void initializeVoiceService() {
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                VoiceAssistantService.LocalBinder binder = (VoiceAssistantService.LocalBinder) service;
                voiceService = binder.getService();
                voiceService.initializeTTS(getApplicationContext(), MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                voiceService = null;
            }
        };

        Intent serviceIntent = new Intent(this, VoiceAssistantService.class);
        startForegroundService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void setShardSettings() {
        Map<String, ?> shard = this.getPreferences(MODE_PRIVATE).getAll();
        Integer color = (Integer) shard.get("color");
        changeColorButtons(color == null ? R.color.Жёлтый : color);
        Float speed = (Float) shard.get("speed");
        TTSConfig.getInstance(this).setSpeechRate(speed == null ? 1.0f : speed);
    }

    private List<Object> getAllButtons(){
        return List.of(findViewById(R.id.leftButton), findViewById(R.id.confirmButton), findViewById(R.id.rightButton), findViewById(R.id.escapeButton));
    }

    private Menu createStartMenu(){
        LinkedHashMap<String, Item> items = new LinkedHashMap<>();
        items.put("Почта", new EnterMailItem(MainActivity.this, "Почта"));
        items.put("Погода", new CitiesWeatherList("Погода", MainActivity.this));
        items.put("Настройки", new SettingsList(MainActivity.this, MainActivity.this,"Настройки"));
        items.put("Послушать, что на фото", new ChooseOrMakePhotoItem("Послушать, что на фото", getActivity()));
        Item rootMenu = new Item(MainActivity.this, "Главное меню", items);
        return new Menu(rootMenu, MainActivity.this);
    }

    public Context getContext(){
        return getApplicationContext();
    }

    public Activity getActivity(){
        return MainActivity.this;
    }

    public void takePhoto() {
        photoService = new PhotoService(this, makePhotoActivityResultLauncher);
        photoService.askOrMakePhoto();
    }

    public void choosePhoto() {
        photoService = new PhotoService(this, choosePhotoActivityResultLauncher);
        photoService.askOrChoosePhoto();
    }

    ActivityResultLauncher<Intent> makePhotoActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> photoService.resultActivityMakePhoto(result));

    ActivityResultLauncher<Intent> choosePhotoActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> photoService.resultActivityChoosePhoto(result));

    public void enableKeyboard(){
        findViewById(R.id.textInput).setVisibility(View.VISIBLE);
    }

    public void disableKeyboard(){
        findViewById(R.id.textInput).setVisibility(View.INVISIBLE);
    }

    public String getTextInput() {
        EditText textInput = findViewById(R.id.textInput);
        String text = String.valueOf(textInput.getText());
        textInput.setText("");
        return text;
    }

    public void changeColorButtons(int color) {
        List<Object> buttons = getAllButtons();
        for (Object button : buttons) {
            ((Button) button).setBackgroundColor(color);
        }
    }

    private Boolean getVoiceAssistantBackgroundStatus() {
        return this.getPreferences(MODE_PRIVATE).getBoolean("isVoiceAssistantBackgroundEnable", false);
    }

    private void startVoiceAssistant() {
        if (voiceService != null) {
            voiceService.setOutApp(false);
            voiceService.startListening();
        }
    }

    private void stopVoiceAssistant() {
        if (voiceService != null) {
            voiceService.setOutApp(!getVoiceAssistantBackgroundStatus());
        }
    }

    private void closeStore() {
        Thread closeMail = new Thread(() -> {
            if (MailVoiceControl.store != null) {
                if (MailVoiceControl.store.isConnected()) {
                    try {
                        MailVoiceControl.store.close();
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        closeMail.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startVoiceAssistant();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeStore();
        stopVoiceAssistant();
    }
}
