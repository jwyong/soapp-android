package com.soapp.global;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.soapp.camera.ImageCropPreviewActivity;
import com.soapp.global.sharing.SharingExistHolder;
import com.soapp.setup.Soapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    public static boolean cam;
    public static int camOrient;
    private static Activity activity;

    public ImageHelper(Activity act) {
        this.activity = act;
    }

    //function to check whether need compress or not
    private static boolean needCompress(Bitmap bitmap) {
        int actualHeight = bitmap.getHeight();
        int actualWidth = bitmap.getWidth();

        if (actualWidth > actualHeight) { //horizontal image, check height
            return (actualHeight > 720); //height more than 720 needs compression
        } else { //vertical or square image
            return (actualWidth > 720);
        }
    }

    //dragon ball
    public static boolean ryanSimpleCheckNeedCompress(Uri uri) {
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());

        int actualHeight = bitmap.getHeight();
        int actualWidth = bitmap.getWidth();
        if (actualHeight > actualWidth) {
            if (actualWidth >= 720 && actualHeight >= 1280) {
                return true;
            }
        } else if (actualHeight < actualWidth) {
            if (actualWidth >= 1280 && actualHeight >= 720) {
                return true;
            }
        }
        return false;
    }

    //wtf dude
    public static String ryanCompressAndSaveImage(String newFilePath, Uri imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap scaledBitmap = null;

        Bitmap bmp = BitmapFactory.decodeFile(imageUri.getPath(), options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        int orientation = 0;
        int rotation = getOrientation(Soapp.getInstance().getApplicationContext(), imageUri);

        if (actualWidth > actualHeight) { //horizontal image (compress to 720p height)
            actualWidth = (int) ((float) actualWidth / (float) actualHeight * 720);
            actualHeight = 720;
            switch (rotation) {
                case 6:
                    orientation = 90;
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageCropPreviewActivity.orient = "vertical";
                                }
                            });
                    break;

                case 8:
                    orientation = 270;
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageCropPreviewActivity.orient = "vertical";
                                }
                            });
                    break;

                case 3:
                    orientation = 180;
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageCropPreviewActivity.orient = "horizontal";
                                }
                            });
                    break;

                default:
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    ImageCropPreviewActivity.orient = "horizontal";
                                }
                            });
                    break;
            }

        } else { //vertical or square image
            actualHeight = (int) ((float) actualHeight / (float) actualWidth * 720);
            actualWidth = 720;
            if (rotation == 3) {
                orientation = 180;
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                ImageCropPreviewActivity.orient = "vertical";
                            }
                        });
            } else {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                ImageCropPreviewActivity.orient = "vertical";
                            }
                        });
            }
        }

        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
//        options.inPurgeable = true;
//        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(imageUri.getPath(), options);
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


        Matrix matrix = new Matrix();

        if (orientation > 0) {
            matrix.postRotate(orientation);
        }

        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap
                .getHeight(), matrix, false);

        FileOutputStream out;
        try {
            out = new FileOutputStream(newFilePath);
//          write the compressed bitmap at the destination specified by filename.

            boolean ok = scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
        }

        return newFilePath;
    }

    private static String getRealPathFromURIStr(Uri contentUri) {
        String realPath = null;
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(contentUri, filePath, null, null, null);

        if (cursor == null) {
            return contentUri.getPath();
        } else {
            if (cursor.moveToFirst()) {
                try {
                    realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                } catch (IllegalArgumentException e) {
                    realPath = contentUri.getPath();
                }
            } else {
                realPath = contentUri.getPath();
            }
            cursor.close();
            return realPath;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private static int getOrientation(Context context, Uri photoUri) {
        ExifInterface mExif;
        int rotation = 0;
        try {
            mExif = new ExifInterface(photoUri.getPath());
            rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (rotation > 0) {
                mExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                        String.valueOf(6));
                mExif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

    private static Bitmap getThumbnailMaybe(Uri uri) throws IOException {
        InputStream input = Soapp.getInstance().getApplicationContext().getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;

        //ignored in Android N and above (still working for Android M and below)
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        int requiredHeight, requiredWidth;
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;


        if (originalWidth > originalHeight) { //check for portrain/landscape
            requiredWidth = 1280;
            requiredHeight = 720;
        } else {
            requiredWidth = 720;
            requiredHeight = 1280;
        }

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inSampleSize = getScale(originalWidth, originalHeight, requiredWidth, requiredHeight);
        bitmapOptions.inJustDecodeBounds = false;

        //ignored in Android N and above (still working for Android M and below)
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = Soapp.getInstance().getApplicationContext().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;
    }

    private static int getScale(int originalWidth, int originalHeight,
                                final int requiredWidth, final int requiredHeight) {

        int scale = 1;

        if ((originalWidth > requiredWidth) || (originalHeight > requiredHeight)) {
            if (originalWidth < originalHeight)
                scale = originalWidth / requiredWidth;
            else
                scale = originalHeight / requiredHeight;

        }

        return scale;
    }

    //resize images to xxdp (shortDP = dp value of shorter side)
    public static byte[] resizeByteFromByteIMGThumb60dp(byte[] oriImageBytes, Context context,
                                                        int shortDP) {
        InputStream inputStream = new ByteArrayInputStream(oriImageBytes);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();


        float density = context.getResources().getDisplayMetrics().density;
        int shortPixel = (int) (shortDP * density);

        if (width > height) { //set height to pixel60dp
            width = (int) ((float) width / (float) height * shortPixel);
            height = shortPixel;
        } else {
            height = (int) ((float) height / (float) width * shortPixel);
            width = shortPixel;
        }

        Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);

        return stream.toByteArray();
    }

    //get file name before saving media to directory (based on date)
    public static String getFileNameForSavingMedia(String filePathPrefix, String fileNamePrefix) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateformat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String dateStr = dateformat.format(currentTime);
        String fileName = "";
        int i, j;

        if (filePathPrefix.equals(GlobalVariables.VIDEO_PATH)) {
            fileName = fileNamePrefix + dateStr + ".mp4";
        } else {
            if (fileNamePrefix.equals("AUD")) {
                fileName = fileNamePrefix + dateStr + ".m4a";
            } else {
                fileName = fileNamePrefix + dateStr + ".jpg";
            }
        }

        String filePath = filePathPrefix + fileName;
        File outputFile = new File(filePath);

        for (i = 0; i >= 0; i++) {
            if (outputFile.exists()) {
                j = i + 1;
                String k = String.format("%04d", j);
                if (filePathPrefix.equals(GlobalVariables.VIDEO_PATH)) {
                    fileName = fileNamePrefix + dateStr + "-" + k + ".mp4";
                } else {
                    if (fileNamePrefix.equals("AUD")) {
                        fileName = fileNamePrefix + dateStr + "-" + k + ".m4a";
                    } else {
                        fileName = fileNamePrefix + dateStr + "-" + k + ".jpg";
                    }
                }
                filePath = filePathPrefix + fileName;

                outputFile = new File(filePath);

            } else {
                break;
            }
        }
        return fileName;
    }

    //get bitmap for profile/grp profile image and save to directory if from cam
    public Bitmap getBitmapForProfile(Uri imageUriOri) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        if (getRealPathFromURIStr(imageUriOri) != null) {
            bitmap = BitmapFactory.decodeFile(getRealPathFromURIStr(imageUriOri));
        } else {
            bitmap = BitmapFactory.decodeFile(imageUriOri.getPath());
        }
        Matrix matrix = new Matrix();
        int oriHeight = bitmap.getHeight();
        int oriWidth = bitmap.getWidth();
//        if (!cam) { //not cam, check if need to compress
            if (needCompress(bitmap)) {
                bitmap = compressImageToBitmap(imageUriOri);
                return bitmap;
            } else {
                int reqHeight;
                int reqWidth;

                if (oriWidth > oriHeight) { //horizontal image
                    reqWidth = (int) ((float) oriWidth / (float) oriHeight * 720);
                    reqHeight = 720;
                } else { //vertical or square image
                    reqHeight = (int) ((float) oriHeight / (float) oriWidth * 720);
                    reqWidth = 720;
                }
                int orientation = getOrientation(Soapp.getInstance().getApplicationContext(), imageUriOri);
                matrix.setRotate(orientation);

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, reqWidth, reqHeight, matrix, false);
                return bitmap;
            }
//        } else { //from cam, rotate based on cam orient then save to directory
//            options.inSampleSize = calculateInSampleSize(options, oriWidth, oriHeight);
//            options.inJustDecodeBounds = false;
//
//            //rotate bitmap in memory
//            matrix.setRotate(camOrient);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, oriWidth, oriHeight, matrix, false);
//
//        }
    }

    //function to move file if no need compress
    private void copyFile(Uri imageUriOri, File dst) throws IOException {//new
        String UriString = imageUriOri.toString();
        String realPath = getRealPathFromURIStr(imageUriOri);

        File src;
        if (realPath != null) {
            src = new File(realPath);
        } else {
            src = new File(imageUriOri.getPath());
        }

        if (UriString.endsWith(".png") || UriString.endsWith(".jpg") || UriString.endsWith(".jpeg")) {
            final int chunkSize = 1024;  // We'll read in one kB at a time
            byte[] imageData = new byte[chunkSize];

            try {
                InputStream inputStream = Soapp.getInstance().getApplicationContext().getContentResolver().openInputStream(imageUriOri);
                OutputStream outputStream = new FileOutputStream(dst);  // I'm assuming you already have the File object for where you're writing to

                int bytesRead;
                while ((bytesRead = inputStream.read(imageData)) > 0) {
                    outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                }
            } catch (Exception e) {
            }
        } else {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                outChannel.close();
            }
        }
    }

    //function to compress image if needed
    private Bitmap compressImageToBitmap(Uri imageUri) {
        //get real path
        String filePath = getRealPathFromURIStr(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualWidth > actualHeight) { //horizontal image (compress to 720p height)
            actualWidth = (int) ((float) actualWidth / (float) actualHeight * 720);
            actualHeight = 720;

            new Handler(Looper.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            SharingExistHolder.orient = "horizontal";
                        }
                    });

        } else { //vertical or square image
            actualHeight = (int) ((float) actualHeight / (float) actualWidth * 720);
            actualWidth = 720;

            new Handler(Looper.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            SharingExistHolder.orient = "vertical";
                        }
                    });
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
//        options.inPurgeable = true;
//        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        //get orientation from chosen file
        int orientation = getOrientation(Soapp.getInstance().getApplicationContext(), imageUri);
        Matrix matrix = new Matrix();

        if (orientation > 0) {
            matrix.postRotate(orientation);
        }

        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap
                .getHeight(), matrix, false);

        return scaledBitmap;
    }

    public Bitmap scaleDown3KImage(Bitmap image_bitmap){
        if(image_bitmap == null){
            return image_bitmap;
        }
        if(image_bitmap.getHeight() >= 3000 || image_bitmap.getWidth() >= 3000){
            int height = image_bitmap.getHeight() /2;
            int width = image_bitmap.getWidth() /2;
            if(image_bitmap.getHeight() > image_bitmap.getWidth()){
                //vertical
//                image_bitmap = Bitmap.createScaledBitmap(image_bitmap, height, width, false);
                image_bitmap = Bitmap.createScaledBitmap(image_bitmap, 720, 1280, false);
            }else{
                //horizontal
//                image_bitmap = Bitmap.createScaledBitmap(image_bitmap, width, height, false);
                image_bitmap = Bitmap.createScaledBitmap(image_bitmap, 1280, 720, false);

            }
        }
        return image_bitmap;
    }

    public String saveBitmapIntoFile(String filePathWithName, Bitmap image){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePathWithName);
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePathWithName;
    }
}