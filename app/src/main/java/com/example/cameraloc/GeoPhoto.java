package com.example.cameraloc;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeoPhoto {

    private File photoFile;
    private Intent callCameraApp;
    private String descriptionString;
    private Context appContext;
    private String packageProvider;

    public GeoPhoto(Context context) {
        photoFile = null;
        callCameraApp = new Intent();
        appContext = context;
        packageProvider = appContext.getApplicationContext().getPackageName() + ".provider";
    }

    public void openCamera(String message) throws IOException {
        Uri uriFile;
        callCameraApp.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        createImageFile();
        descriptionString = message;
        uriFile = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)?
                FileProvider.getUriForFile(appContext,packageProvider,photoFile):
                Uri.fromFile(photoFile);
        Log.i("openCamera",uriFile.toString());
        callCameraApp.putExtra(MediaStore.EXTRA_OUTPUT,uriFile);
    }

    public String photoFilePath() {
        return photoFile.getAbsolutePath();
    }

    public String photoFileName() {
        return photoFile.getName();
    }

    public Intent returnCameraIntent() {
        return callCameraApp;
    }

    private void createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        File storageDirectory = Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DCIM));
        photoFile = File.createTempFile(imageFileName, ".jpg", storageDirectory);
    }

    public boolean markGeoTagImage() {
        try {
            ExifInterface exif = new ExifInterface(photoFilePath());
            Log.i("geotag", photoFilePath());
            GPSTrack location = new GPSTrack(appContext);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSTrack.convert(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSTrack.latitudeRef(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSTrack.convert(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSTrack.longitudeRef(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, descriptionString);
            exif.saveAttributes();
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }catch(NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteGeoPhoto(){
        callCameraApp = null;
        descriptionString = null;
        return photoFile.delete();
    }
}
