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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeoPhoto {

    private File photoFile;
    private Intent callCameraApp;
    private Context appContext;
    private String packageProvider;

    public GeoPhoto(Context context) {
        photoFile = null;
        callCameraApp = new Intent();
        appContext = context;
        packageProvider = appContext.getApplicationContext().getPackageName() + ".provider";
    }

    public void openCamera() throws IOException {
        Uri uriFile;
        callCameraApp.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        createImageFile();
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
        File storageDirectory = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)?
                appContext.getCacheDir():
                Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DCIM));
        photoFile = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        Log.i("createImage",photoFilePath());
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
    public void setImageDescription(String desc){
        try{
            ExifInterface exif = new ExifInterface(photoFilePath());
            if(desc == null || desc.equals("")) {
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, photoFileName());
            }else{
                exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,desc);
            }
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT,"");
            exif.saveAttributes();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                saveImage();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean deleteGeoPhoto(){
        callCameraApp = null;
        return photoFile.delete();
    }

    private void saveImage(){
        File storageDirectory =
                Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DCIM));
        File targetLocation= new File (storageDirectory + "/" + photoFileName());
        Log.i("save",storageDirectory+ "/" + photoFileName());
    try {
        InputStream in = new FileInputStream(photoFile);
        OutputStream out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }catch(FileNotFoundException f){
        f.printStackTrace();
    }
    catch (IOException e){
        e.printStackTrace();
    }
    }
}
