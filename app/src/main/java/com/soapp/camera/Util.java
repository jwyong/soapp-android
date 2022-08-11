package com.soapp.camera;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/* Created by Kirill on 10/30/2017. */

public class Util {

    public static String getFileName() {
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        return date + ".jpg";
    }

    public static File copyFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        try (FileChannel outputChannel = new FileOutputStream(newFile).getChannel(); FileChannel inputChannel = new FileInputStream(file).getChannel()) {
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);

        } catch (FileNotFoundException e) {
            Log.i("diao","e: "+e);
            //restart Soapp if can't find file - means no permission Android bug
//            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return newFile;
    }

    public static File ryanCopyFile(File source, File destination) throws IOException {
        try (FileChannel outputChannel = new FileOutputStream(destination).getChannel(); FileChannel inputChannel = new FileInputStream(source).getChannel()) {
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);

        } catch (FileNotFoundException e) {
            //restart Soapp if can't find file - means no permission Android bug
            android.os.Process.killProcess(android.os.Process.myPid());

        }
        return destination;
    }


    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
