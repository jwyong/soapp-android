package com.soapp.WorkManager.media.upload.image;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.soapp.global.ChatHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.sql.DatabaseHelper;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

public class CompressImageWorker extends Worker {
    DatabaseHelper databaseHelper;
    public CompressImageWorker(){
        databaseHelper = DatabaseHelper.getInstance();
    }
    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String row_id = data.getString("row_id");
        String orient = data.getString("orient");
        String jid = data.getString("jid");
        String unique_id = data.getString("unique_id");
        String path = data.getString("path");
        String status = data.getString("status");
        String group_display_name = data.getString("group_display_name");
        //
//        String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
//        if(id == null){
//            stop(true);
//            ContentValues cv = new ContentValues();
//            cv.put(DatabaseHelper.MSG_MEDIASTATUS, -3);
//            new DatabaseHelper().updateRDB1Col(MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW,
//                    row_id);
//            return Result.FAILURE;
//        }
        //
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String desireFileNameWExtension;
                if(status.equals("-5")){
                     desireFileNameWExtension = new ChatHelper().outgoingImage_Negative5(path, getApplicationContext());
                }else{
                    desireFileNameWExtension = new ChatHelper().outgoingImage_Negative6(path);
                }
                checkandCompressImage(group_display_name, status, desireFileNameWExtension, row_id, orient, jid, unique_id);
            }
        });
        return Result.SUCCESS;
    }

    public void checkandCompressImage(String group_display_name, String status, String fileNameWithExtension, String row_id, String orient, String jid, String unique_id) {
        File fileToCompressInSentFolder = new File(GlobalVariables.IMAGES_SENT_PATH, fileNameWithExtension);
        if(status.equals("-6")) {
            ImageHelper.ryanCompressAndSaveImage(fileToCompressInSentFolder.getPath(), Uri.fromFile(fileToCompressInSentFolder));
        }
        String path = GlobalVariables.IMAGES_SENT_PATH + "/" + fileNameWithExtension;
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.MSG_MEDIASTATUS, -1);
        cv.put(DatabaseHelper.MSG_MSGINFOURL, fileNameWithExtension);
        new DatabaseHelper().updateRDB1Col(MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW, row_id);

        WorkManager workManager = WorkManager.getInstance();
        Data d = new Data.Builder()
                .putString("row_id", ""+row_id)
                .putString("path", path)
                .putString("orient", orient)
                .putString("jid", jid)
                .putString("unique_id", unique_id)
                .putString("group_display_name", group_display_name)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadImageWorker.class)
                .setInputData(d)
                .addTag("UploadImage")
                .build();
        //
        String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        if(id != null){
            databaseHelper.updateMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            workManager.enqueue(oneTimeWorkRequest);
        }
    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
    }
}
