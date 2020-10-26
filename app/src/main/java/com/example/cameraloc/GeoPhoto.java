package com.example.cameraloc;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeoPhoto {

    private File photoFile;
    private Intent callCameraApp;
    private String descriptionString;
    public GeoPhoto() {
        photoFile = null;
        callCameraApp = new Intent();
    }

    public void openCamera(String message) throws IOException {
        callCameraApp.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        createImageFile();
        descriptionString = message;
        callCameraApp.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        Log.i("openCamera",Uri.fromFile(photoFile).toString());
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

    public boolean markGeoTagImage(Context context) {
        try {
            ExifInterface exif = new ExifInterface(photoFilePath());
            Log.i("geotag", photoFilePath());
            GPSTrack location = new GPSTrack(context);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSTrack.convert(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSTrack.latitudeRef(location.getLatitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSTrack.convert(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSTrack.longitudeRef(location.getLongitude()));
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, descriptionString);
            exif.saveAttributes();
            Log.i("Exif Latitude", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.i("Exif latRef", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
            Log.i("Exif Long", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            Log.i("Exif longRef", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
            Log.i("Exif Desc", exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
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
