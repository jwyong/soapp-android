package com.soapp.WorkManager;

import android.arch.lifecycle.LiveData;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.soapp.global.ChatHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.IndiChatHelper;
import com.soapp.sql.DatabaseHelper;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.SynchronousWorkManager;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import androidx.work.Worker;

import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

public class UploadWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String row_id = data.getString("row_id");
        String orient = data.getString("orient");
        String jid = data.getString("jid");
        String unique_id = data.getString("unique_id");
        String path = data.getString("path");
        //
        String status = data.getString("status");
        //
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
//                if(status.equals("-5")){//compress, upload
                    String desireFileNameWExtension = new ChatHelper().outgoingImage_Negative5(path, getApplicationContext());
                    checkandCompressImage(desireFileNameWExtension, row_id, orient, jid, unique_id);
//                }else{//upload
//                    new IndiChatHelper().uploadImage(getApplicationContext(), row_id, path, orient, jid, unique_id);
//                }
            }
        });
        return Result.SUCCESS;
    }

    public void checkandCompressImage(String fileNameWithExtension, String row_id, String orient, String jid, String unique_id) {

        File fileToCompressInSentFolder = new File(GlobalVariables.IMAGES_SENT_PATH, fileNameWithExtension);
        String compressedFilePath = ImageHelper.ryanCompressAndSaveImage(fileToCompressInSentFolder.getPath(), Uri.fromFile(fileToCompressInSentFolder));
        if (!compressedFilePath.equals("failed")) {
            String path = GlobalVariables.IMAGES_SENT_PATH + "/" + fileNameWithExtension;
            ContentValues cv = new ContentValues();
            cv.put("MediaStatus", -1);
            cv.put("MsgInfoUrl", fileNameWithExtension);
            new DatabaseHelper().updateRDB1Col(MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW,
                    row_id);

            WorkManager workManager = WorkManager.getInstance();

            Data d = new Data.Builder()
                    .putString("row_id", ""+row_id)
                    .putString("path", path)
                    .putString("orient", "vertical")
                    .putString("jid", jid)
                    .putString("unique_id", unique_id)
                    .putString("status", "-3")
                    .build();

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadImageWorker.class)
                    .setInputData(d)
                    .addTag("UploadImage")
                    .build();

            workManager.enqueue(oneTimeWorkRequest);
        }
    }
}
