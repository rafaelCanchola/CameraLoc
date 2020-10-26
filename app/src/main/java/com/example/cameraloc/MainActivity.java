package com.example.cameraloc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private GeoPhoto myPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoImageViewOne = findViewById(R.id.photoImgOne);
        photoImageViewTwo = findViewById(R.id.photoImgTwo);
        firstPhotoButton = findViewById(R.id.cam_button_one);
        secondPhotoButton = findViewById(R.id.cam_button_two);
        verifyPermissions();
    }

    public void takePhoto(View v) {
        myPhoto = new GeoPhoto();
        try {
            myPhoto.OpenCamera(null);
            switch (v.getId()) {
                case R.id.cam_button_one:
                    startActivityForResult(myPhoto.ReturnCameraIntent(), Constants.ACTIVITY_CAMERA_BUTTON_1);
                    break;
                case R.id.cam_button_two:
                    startActivityForResult(myPhoto.ReturnCameraIntent(), Constants.ACTIVITY_CAMERA_BUTTON_2);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
                ResultGeoPhoto(requestCode);
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

    private void ResultGeoPhoto(int requestCode){
        Bitmap photoCapturedBitmap;
        photoCapturedBitmap = BitmapFactory.decodeFile(myPhoto.PhotoFilePath());
        if(photoCapturedBitmap == null){
            ResultGeoPhoto(requestCode);
        }
        else {
            if (myPhoto.MarkGeoTagImage(this)) {
                switch (requestCode) {
                    case Constants.ACTIVITY_CAMERA_BUTTON_1:
                            photoImageViewOne.setImageBitmap(photoCapturedBitmap);
                            firstPhotoButton.setVisibility(View.GONE);
                        break;
                    case Constants.ACTIVITY_CAMERA_BUTTON_2:
                            photoImageViewTwo.setImageBitmap(photoCapturedBitmap);
                            secondPhotoButton.setVisibility(View.GONE);
                        break;
                }
            } else {
                Toast.makeText(this, "Ocurrio un error al añadir la ubicación. Vuelva a tomar la imagen.", Toast.LENGTH_SHORT).show();
                myPhoto.DeleteGeoPhoto();
            }
        }
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