package com.example.visimpaired.PhotoAnalysis;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.visimpaired.MainActivity;
import com.example.visimpaired.Request.DTO.ImageToTextResponse;
import com.example.visimpaired.Request.RequestService;
import com.example.visimpaired.TTSConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class PhotoService extends MainActivity {

    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private String currentPhotoPath;
    private String selectedPhotoPath;
    private final Context context;
    private final ActivityResultLauncher<Intent> intentActivity;


    public PhotoService(Context context, ActivityResultLauncher<Intent> intentActivity) {
        this.context = context;
        this.intentActivity = intentActivity;
    }

    public void askOrChoosePhoto(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_GALLERY);
        } else {
            openGallery();
        }
    }

    public void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentActivity.launch(galleryIntent);
    }

    public void askOrMakePhoto(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                TTSConfig.getInstance(getContext()).speak("Разрешение на камеру не дано!");
            }
        }
        else if (requestCode == REQUEST_CODE_GALLERY){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                TTSConfig.getInstance(getContext()).speak("Разрешение на галерею не дано!");
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.example.visimpaired.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intentActivity.launch(cameraIntent);
            }
        }
    }

    public void resultActivityMakePhoto(ActivityResult result){
        if (result.getResultCode() == Activity.RESULT_OK) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                selectedPhotoPath = currentPhotoPath;
            }
            String base64Image = encodeImageToBase64(selectedPhotoPath);
            new RequestService(context).fetchData(base64Image);
        }
    }

    public void resultActivityChoosePhoto(ActivityResult result){
        if (result.getResultCode() == RESULT_OK) {
            Uri selectedImageUri = result.getData().getData();
            selectedPhotoPath = getRealPathFromURI(selectedImageUri);
            String base64Image = encodeImageToBase64(selectedPhotoPath);
            new RequestService(context).fetchData(base64Image);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public String encodeImageToBase64(String imagePath) {
        byte[] imageBytes = null;
        try {
            imageBytes = Files.readAllBytes(Paths.get(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }
}
