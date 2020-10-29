package com.example.cameraloc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView photoImageViewOne;
    private ImageView photoImageViewTwo;
    private Button firstPhotoButton;
    private Button secondPhotoButton;
    private EditText firstEditText;
    private EditText secondEditText;
    private GeoPhoto myPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoImageViewOne = findViewById(R.id.photoImgOne);
        photoImageViewTwo = findViewById(R.id.photoImgTwo);
        firstPhotoButton = findViewById(R.id.cam_button_one);
        secondPhotoButton = findViewById(R.id.cam_button_two);
        firstEditText = findViewById(R.id.getText1);
        secondEditText = findViewById(R.id.getText2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        verifyPermissions();
    }

    public void takePhoto(View v) {
        myPhoto = new GeoPhoto(this);
        try {
            myPhoto.openCamera();
            switch (v.getId()) {
                case R.id.cam_button_one:
                    startActivityForResult(myPhoto.returnCameraIntent(), Constants.ACTIVITY_CAMERA_BUTTON_1);
                    break;
                case R.id.cam_button_two:
                    startActivityForResult(myPhoto.returnCameraIntent(), Constants.ACTIVITY_CAMERA_BUTTON_2);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
                resultGeoPhoto(requestCode);
        }
        if(requestCode == RESULT_CANCELED){
            Log.i("result","canceled");
        }
    }

    public void verifyPermissions() {
        if (verifyLocation() && verifyStorage()) {
            Log.i("verifyPermissions", "all true");
        } else if (verifyLocation() && !verifyStorage()) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISSION_CODE);
            Log.i("verifyPermissions", "storage true");
        } else if (!verifyLocation() && verifyStorage()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSION_CODE);
            Log.i("verifyPermissions", "location true");
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.PERMISSION_CODE);
            Log.i("verifyPermissions", "none true");
        }
    }
//No dejar que la aplicación continue
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("requestPermission", permissions[i] + " is true");
                    } else {
                        Log.i("requestPermission", permissions[i] + " is false");
                    }
                }
                Log.i("requestPermission", String.valueOf(grantResults.length));
            } else {
                Log.i("requestPermission", "none still true");
            }
        }
    }
    
    private boolean verifyLocation() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean verifyStorage() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

    }

    private void resultGeoPhoto(int requestCode){
        Bitmap photoCapturedBitmap;
        photoCapturedBitmap = BitmapFactory.decodeFile(myPhoto.photoFilePath());
        if(photoCapturedBitmap == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            resultGeoPhoto(requestCode);
        }
        else {
            if (myPhoto.markGeoTagImage()) {
                switch (requestCode) {
                    case Constants.ACTIVITY_CAMERA_BUTTON_1:
                            myPhoto.setImageDescription(firstEditText.getText().toString());
                            photoImageViewOne.setImageBitmap(photoCapturedBitmap);
                            firstPhotoButton.setVisibility(View.GONE);
                        break;
                    case Constants.ACTIVITY_CAMERA_BUTTON_2:
                            myPhoto.setImageDescription(secondEditText.getText().toString());
                            photoImageViewTwo.setImageBitmap(photoCapturedBitmap);
                            secondPhotoButton.setVisibility(View.GONE);
                        break;
                }
            } else {
                Toast.makeText(this, "Ocurrio un error al añadir la ubicación. Vuelva a tomar la imagen.", Toast.LENGTH_SHORT).show();
                myPhoto.deleteGeoPhoto();
            }
        }
    }
    //Remover focus del textEdit cuando se toca la pantalla
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    ((EditText) v).setCursorVisible(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }


    public void readGeoTagImage(String imagePath) {
        Location loc = new Location("");
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            float[] latlong = new float[2];


            if (exif.getLatLong(latlong)) {
                loc.setLatitude(latlong[0]);
                loc.setLongitude(latlong[1]);
            }
            String uc = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "\nLong: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "\nComment is: " + uc, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}