package com.example.cameraloc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

    private static final int ACTIVITY_CAMERA_BUTTON_1 = 1;
    private static final int ACTIVITY_CAMERA_BUTTON_2 = 2;
    private ImageView photoImageViewOne;
    private ImageView photoImageViewTwo;
    private Button firstPhotoButton;
    private Button secondPhotoButton;
    private String imageOneFileLocation;
    private String imageTwoFileLocation;
    GPSTrack gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoImageViewOne = (ImageView) findViewById(R.id.photoImgOne);
        photoImageViewTwo = (ImageView) findViewById(R.id.photoImgTwo);
        firstPhotoButton = (Button) findViewById(R.id.cam_button_one);
        secondPhotoButton = (Button) findViewById(R.id.cam_button_two);
    }
    public void takePhoto(View v){
        Toast.makeText(this, "camera button pressed", Toast.LENGTH_SHORT).show();
        Intent callCameraApplication = new Intent();
        callCameraApplication.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {

            switch(v.getId()){
                case R.id.cam_button_one:
                    photoFile = createImageFile(ACTIVITY_CAMERA_BUTTON_1);
                    callCameraApplication.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(callCameraApplication, ACTIVITY_CAMERA_BUTTON_1);
                    //File fli = contentValuesImage(photoFile);
                    break;
                case R.id.cam_button_two:
                    photoFile = createImageFile(ACTIVITY_CAMERA_BUTTON_2);
                    callCameraApplication.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(callCameraApplication, ACTIVITY_CAMERA_BUTTON_2);
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photoCapturedBitmap;
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case ACTIVITY_CAMERA_BUTTON_1:
                    gps = new GPSTrack(this);
                    if(gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        MarkGeoTagImage(imageOneFileLocation);
                    }
                    photoCapturedBitmap = BitmapFactory.decodeFile(imageOneFileLocation);
                    photoImageViewOne.setImageBitmap(photoCapturedBitmap);
                    firstPhotoButton.setVisibility(View.GONE);
                    readGeoTagImage(imageOneFileLocation);
                    break;
                case ACTIVITY_CAMERA_BUTTON_2:
                    gps = new GPSTrack(this);
                    if(gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        MarkGeoTagImage(imageTwoFileLocation);
                        readGeoTagImage(imageTwoFileLocation);
                    }
                    photoCapturedBitmap = BitmapFactory.decodeFile(imageTwoFileLocation);
                    photoImageViewTwo.setImageBitmap(photoCapturedBitmap);


                    secondPhotoButton.setVisibility(View.GONE);
                    break;
            }
        }
        //if (requestCode == ACTIVITY_CAMERA_BUTTON_1 && resultCode == RESULT_OK) {
        //Toast.makeText(this,"Picture taken successfully", Toast.LENGTH_LONG).show();
        //Bundle extras = data.getExtras();
        //Bitmap photoCapturedBitmap = (Bitmap) extras.get("data");
        //photoImageViewOne.setImageBitmap(photoCapturedBitmap);

        //firstPhotoButton.setVisibility(View.GONE);
        //}
    }

    private File createImageFile(int id) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_"+timeStamp;
        File storageDirectory = Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_PICTURES));
        File image = File.createTempFile(imageFileName,".jpg",storageDirectory);
        switch(id){
            case ACTIVITY_CAMERA_BUTTON_1:
                imageOneFileLocation = image.getAbsolutePath();
                break;
            case ACTIVITY_CAMERA_BUTTON_2:
                imageTwoFileLocation = image.getAbsolutePath();
                break;
        }
        return image;
    }

    private File contentValuesImage(File fl){
        ContentValues values = new ContentValues(9);
        values.put(MediaStore.Images.ImageColumns.TITLE, fl.getName());
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fl.getName() + ".jpg");
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN,  new SimpleDateFormat("yyyyMMdd").format(new Date()));
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.DATA, fl.getAbsolutePath());
        values.put(MediaStore.Images.ImageColumns.SIZE, fl.length());
        //setImageSize(values, width, height);
        gps = new GPSTrack(this);
        if(gps.canGetLocation()) {
            values.put(MediaStore.Images.ImageColumns.LATITUDE, gps.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, gps.getLongitude());
        }
        ContentResolver resolver = getBaseContext().getContentResolver();
        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        return fl;
    }

    public void MarkGeoTagImage(String imagePath)
    {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            GPSTrack location = new GPSTrack(this);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSTrack.convert(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSTrack.latitudeRef(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSTrack.convert(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSTrack.longitudeRef(location.getLongitude()));
            SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            //  exif.setAttribute(ExifInterface.TAG_DATETIME,fmt_Exif.format(new Date(location.getTime())));
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "Mi descripcion");
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readGeoTagImage(String imagePath)
    {
        Location loc = new Location("");
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            float [] latlong = new float[2] ;


            if(exif.getLatLong(latlong)){
                loc.setLatitude(latlong[0]);
                loc.setLongitude(latlong[1]);
            }
            String uc = exif.getAttribute(ExifInterface.TAG_USER_COMMENT);
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "\nLong: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "\nComment is: " + uc, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}