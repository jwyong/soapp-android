package com.soapp.WorkManager.media.download;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.soapp.global.ChatHelper;
import com.soapp.global.DecryptionHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

public class DownloadManagerWorker extends Worker{
    //
    DatabaseHelper databaseHelper;
    DecryptionHelper decryptionHelper;
    Context mContext;
    //

    public DownloadManagerWorker(){
        databaseHelper = DatabaseHelper.getInstance();
        decryptionHelper = new DecryptionHelper();
        mContext  = Soapp.getInstance().getApplicationContext();

    }
    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String resource_id = data.getString("resource_id");
        String url = data.getString("url");
        String row_id = data.getString("row_id");
        String type = data.getString("type");

//        Log.i("diao","resource_id: "+resource_id+", url: "+url+", row_id: "+row_id+", type: "+type);
//        while (download){
//            counter += 1;
//            Log.i("diao","counter: "+counter);
//            if(counter == max){
//                download = false;
//                return Result.SUCCESS;
//            }
//        }
//        Log.i("diao","rubbish");
//        try {
        int download_id = databaseHelper.rdb.selectQuery().getMsgDownloadId(row_id);
        if(download_id != 0){
            String[] result = new ChatHelper().checkDownloadProgress(mContext, databaseHelper, ""+download_id, row_id);
            if(result[0].equals("STATUS_SUCCESSFUL")){
                new ChatHelper().renamedMediaBasedOnType(decryptionHelper, type, resource_id, row_id);
                return Result.SUCCESS;
            }
        }
        download(resource_id, url, row_id, type);

//            return Result.SUCCESS;
//        }catch (Exception e){
//            Log.i("diao",e.toString());
//            return Result.FAILURE;
//        }
//
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        return Result.SUCCESS;
    }

    public void download(String resourceID, String url, String rowid, String type) {
//        DatabaseHelper databaseHelper = new DatabaseHelper();
        DownloadManager downloadManager = (DownloadManager) Soapp.getInstance().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        switch (type) {
            case "image":
                request.setTitle(resourceID + "jpg")
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationInExternalPublicDir("/Soapp/SoappImages", resourceID + ".jpg");
                break;

            case "video":
                request.setTitle(resourceID + ".mp4")
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationInExternalPublicDir("/Soapp/SoappVideo", resourceID + ".mp4");
                break;

            case "audio":
                request.setTitle(resourceID + ".m4a")
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationInExternalPublicDir("/Soapp/SoappAudio", resourceID + ".m4a");
                break;
        }
        final long enqueueId = downloadManager.enqueue(request);

        ContentValues cvMSG = new ContentValues();

        //save download id (enqueue id) into MSG_MSGUNIQUEID
        cvMSG.put(DatabaseHelper.MSG_CONTACT, enqueueId);
        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowid);

        WorkManager workManager = WorkManager.getInstance();

        Data d = new Data.Builder()
                .putString("row_id", ""+rowid)
                .put("resource_id",resourceID)
                .put("enqueueId",""+enqueueId)
                .put("type",type)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DownloadingWorker.class)
                .setInputData(d)
                .addTag("DownloadingImage")
                .build();
        //
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.MEDIA_WORKER_WORKER_ENQUEUE_ID, oneTimeWorkRequest.getId().toString());
        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, cv,
                DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, rowid);
        String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(rowid);
        if(id != null){
            workManager.enqueue(oneTimeWorkRequest);
        }
    }
}
