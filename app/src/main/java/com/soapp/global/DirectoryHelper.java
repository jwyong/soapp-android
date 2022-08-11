package com.soapp.global;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.soapp.registration.Splash;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/* Created by chang on 27/07/2017. */

public class DirectoryHelper {
    public void checkAndCreateDir(Context context, boolean needRestart) {
        File newFolder;

        //check if storage is accessible first (android permission bug)
        if (needRestart) {
            newFolder = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH, ".soappcheck");
            //check if test folder exists first
            if (!newFolder.exists()) { //not exist, try create
                createFolder(context, newFolder);

            } else { //exsists, delete then try create
                newFolder.delete();
                createFolder(context, newFolder);
            }

            //delete test soappcheck file once done
            if (newFolder.exists()) {
                newFolder.delete();
            }
        }

        //check and create each folder if not exist
        //wallpaper
        newFolder = new File(GlobalVariables.WALLPAPERS_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        //chat images
        newFolder = new File(GlobalVariables.IMAGES_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.IMAGES_SENT_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        //profile img
        newFolder = new File(GlobalVariables.PROFILE_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.PROFILE_CROPPED_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        //chat audio
        newFolder = new File(GlobalVariables.AUDIO_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.AUDIO_SENT_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        //chat video
        newFolder = new File(GlobalVariables.VIDEO_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.VIDEO_SENT_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }
        newFolder = new File(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        newFolder = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH, ".nomedia");
        if (!newFolder.exists()) {
            try {
                newFolder.createNewFile();
            } catch (IOException ignore) { }
        }

        newFolder = new File(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH, ".nomedia");
        if (!newFolder.exists()) {
            try {
                newFolder.createNewFile();
            } catch (IOException ignore) { }
        }

        newFolder = new File(GlobalVariables.VIDEO_SENT_PATH, ".nomedia");
        if (!newFolder.exists()) {
            try {
                newFolder.createNewFile();
            } catch (IOException ignore) { }
        }

        newFolder = new File(GlobalVariables.IMAGES_SENT_PATH, ".nomedia");
        if (!newFolder.exists()) {
            try {
                newFolder.createNewFile();
            } catch (IOException ignore) { }
        }

        newFolder = new File(GlobalVariables.PROFILE_CROPPED_PATH, ".nomedia");
        if (!newFolder.exists()) {
            try {
                newFolder.createNewFile();
            } catch (IOException ignore) { }
        }
    }

    //function for creating folder and restart app if failed
    private void createFolder(Context context, File newFolder) {
        try {
            //create passsed, continue on
            newFolder.createNewFile();
        } catch (IOException e) {
            //create failed, restart app
            Intent mStartActivity = new Intent(context, Splash.class);
            int mPendingIntentId = android.os.Process.myPid();

            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

            android.os.Process.killProcess(mPendingIntentId);
        }
    }

    public static byte[] getBytesFromBitmap100(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    //changed to 20% testing
    public static byte[] getBytesFromBitmap33(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        return stream.toByteArray();
    }
}