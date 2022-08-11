package com.soapp.global;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import com.soapp.camera.Camera2Activity;
import com.soapp.camera.CameraActivity;
import com.soapp.setup.Soapp;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by rlwt on 6/8/18.
 */

public class CameraHelper {
    //android 7+ = camera2
    private int androidAPI = 24;

    //function to start camera intent based on criteria in this function
    public Intent startCameraIntent(Context context) {
        //version method
        Intent cameraIntent;

        //if got devOptions, override
        switch (Preferences.getInstance().getValue(context, "devSwitchCam")) {
            case "1":
                cameraIntent = new Intent(context, CameraActivity.class);
                break;

            case "2":
                cameraIntent = new Intent(context, Camera2Activity.class);
                break;

            default:
                if (Build.VERSION.SDK_INT >= androidAPI) {
                    cameraIntent = new Intent(context, Camera2Activity.class);
                } else { //Android 5.0
                    cameraIntent = new Intent(context, CameraActivity.class);
                }
                break;
        }

//        //manufacturer method
//        Intent cameraIntent;
//
//        switch (Build.MANUFACTURER.toLowerCase()) {
//            case "huawei":
//            case "htc":
//                cameraIntent = new Intent(context, Camera2Activity.class);
//                break;
//
//            default:
//                cameraIntent = new Intent(context, CameraActivity.class);
//                break;
//        }

        return cameraIntent;
    }

    //function to detect which camera activity to close
    public void finishCameraIntent() {
        //version method
        switch (Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(), "devSwitchCam")) {
            case "1":
                CameraActivity.context.finish();
                CameraActivity.context = null;
                break;

            case "2":
                Camera2Activity.context.finish();
                Camera2Activity.context = null;
                break;

            default:
                if (Build.VERSION.SDK_INT >= androidAPI) {
                    Camera2Activity.context.finish();
                    Camera2Activity.context = null;
                } else {
                    CameraActivity.context.finish();
                    CameraActivity.context = null;
                }
                break;
        }


        //manufacturer method
//        switch (android.os.Build.MANUFACTURER.toLowerCase()) {
//            case "huawei":
//            case "htc":
//                Camera2Activity.context.finish();
//                break;
//
//            default:
//                CameraActivity.context.finish();
//                break;
//        }
    }

    //function to rotate bitmap based on which camera used
    public Bitmap rotateCameraBitmap(Bitmap image_bitmap, String sensor_orientation, int rotation, Context context) {
        int rotationAngle = 0;

        //camera with sensor_orientation (camera 2)
        if (sensor_orientation != null) {
            switch (sensor_orientation) {
                case "inverse_portrait":
                    rotationAngle = 180;
                    break;

                case "landscape_right":
                    rotationAngle = 270;
                    break;

                case "landscape_left":
                    rotationAngle = 90;
                    break;
            }
        }

        //camera with rotation (camera 1)
        if (rotation != 0) {
            switch (rotation) {
                //hori
                case 6:
                    rotationAngle = 90;
                    break;

                //ver
                case 8:
                    rotationAngle = 270;
                    break;

                //hori
                case 3:
                    rotationAngle = 180;
                    break;
            }
        }

        if (rotationAngle == 0) {
            return image_bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);

        return Bitmap.createBitmap(image_bitmap, 0, 0, image_bitmap.getWidth(), image_bitmap.getHeight(),
                matrix, true);
    }

    public String getVideoTimeFormat(String path) {
        if (path != null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            File file = new File(path);
            if (!file.exists() || file.length() <= 200) {
                return "";
            }
            retriever.setDataSource(path);
            long milli = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(milli),
                    TimeUnit.MILLISECONDS.toSeconds(milli) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))
            );
        } else {
            return "null";
        }
    }
}
