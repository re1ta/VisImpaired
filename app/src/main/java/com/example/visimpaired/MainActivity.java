package com.example.visimpaired;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.visimpaired.Mail.EnterMailItem;
import com.example.visimpaired.Mail.MailFolderList;
import com.example.visimpaired.Menu.Menu;
import com.example.visimpaired.Menu.Item;
import com.example.visimpaired.PhotoAnalysis.ChooseOrMakePhotoItem;
import com.example.visimpaired.PhotoAnalysis.PhotoService;
import com.example.visimpaired.Weather.CitiesWeatherList;

import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static Menu menu;
    private PhotoService photoService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.textInput).setVisibility(View.INVISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        menu = createStartMenu();
        new ButtonHandler(getContext(), menu).setupButton(getAllButtons());
    }

    private List<Object> getAllButtons(){
        return List.of(findViewById(R.id.leftButton), findViewById(R.id.confirmButton), findViewById(R.id.rightButton), findViewById(R.id.escapeButton));
    }

    private Menu createStartMenu(){
        LinkedHashMap<String, Item> items = new LinkedHashMap<>();
        items.put("Почта", new EnterMailItem("Почта", MainActivity.this));
        items.put("Погода", new CitiesWeatherList("Погода", MainActivity.this));
        items.put("Настройки", new Item("Настройки"));
        items.put("Послушать, что на фото", new ChooseOrMakePhotoItem("Послушать, что на фото", getActivity()));
        Item rootMenu = new Item("Главное меню", items);
        return new Menu(rootMenu);
    }

    public Context getContext(){
        return getApplicationContext();
    }

    public Activity getActivity(){
        return MainActivity.this;
    }

    public void takePhoto(){
        photoService = new PhotoService(this, makePhotoActivityResultLauncher);
        photoService.askOrMakePhoto();
    }

    public void choosePhoto(){
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

    public String getTextInput(){
        EditText textInput = findViewById(R.id.textInput);
        String text = String.valueOf(textInput.getText());
        textInput.setText("");
        return text;
    }
}
