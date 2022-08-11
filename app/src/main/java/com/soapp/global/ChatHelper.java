package com.soapp.global;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ProgressBar;

import com.soapp.SoappApi.ApiModel.Resource1v3Model;
import com.soapp.SoappApi.Interface.DownloadFromUrlInterface;
import com.soapp.SoappApi.Interface.Resource1v3Interface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.WorkManager.media.download.DownloadManagerWorker;
import com.soapp.WorkManager.media.download.DownloadRetrofitWorker;
import com.soapp.WorkManager.media.upload.audio.UploadAudioWorker;
import com.soapp.WorkManager.media.upload.image.CompressImageWorker;
import com.soapp.WorkManager.media.upload.image.UploadImageWorker;
import com.soapp.WorkManager.media.upload.video.CompressVideoWorker;
import com.soapp.WorkManager.media.upload.video.UploadVideoWorker;
import com.soapp.camera.Util;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.MediaWorker;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

public class ChatHelper {

    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public static String unsuccessful = "unsuccessful";
    public static String failed = "failed";
    public static String successful = "successful";

    //for outgoing
    public String[] replaceThumbnailWithDesiredName(String uniqueId) {
        //name of thumb with unique ID (first time come in)
        File thumbnailWithUniqueIdNaming = new File(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + uniqueId + ".jpg");
        //try to get file name based on VIDxxx
        String desireFileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/", "VID");
        String desireFileNameWOExtension = desireFileName.substring(0, desireFileName.indexOf("."));
        File thumbnailWithDesiredNaming = new File(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH, desireFileNameWOExtension + ".jpg");
        boolean renamedCompleted = thumbnailWithUniqueIdNaming.renameTo(thumbnailWithDesiredNaming);
        return new String[] {renamedCompleted+"", desireFileNameWOExtension};
    }

    public void copyFileFrom(String originalVideoFilePath, String desireFileNameWOExtension, int type) {
        File file = new File(originalVideoFilePath);
        File copiedFile = new File(GlobalVariables.VIDEO_SENT_PATH + "/" + desireFileNameWOExtension + ".mp4");
        try {
            copiedFile = Util.ryanCopyFile(file, copiedFile);
            if(type == -5){
                file.delete();
            }
        } catch (IOException e) {
            Log.i("mad","copyFileFrom: "+e.toString());
        }
    }
    //end for outgoing

    //for out going video
    public String outGoingVideoNegative5_Negative6(DatabaseHelper databaseHelper, String videoPath, String uniqueID, int rowId, int type){
        String[] result = replaceThumbnailWithDesiredName(uniqueID);
        if(result[0].equals("true")){
            ContentValues cv = new ContentValues();
            if(type == -6){
                cv.put(DatabaseHelper.MSG_MEDIASTATUS, -4);
            }else{
                copyFileFrom(videoPath ,result[1], type);
                cv.put(DatabaseHelper.MSG_MEDIASTATUS, -1);
            }
            cv.put(DatabaseHelper.MSG_MSGINFOID, result[1]);
            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW, "" + rowId);
        }
        return result[1];
    }

    //end going video

    //for outgoing image
    public String outgoingImage_Negative5(String image_path, Context context){
        //example: /data/user/0/com.soapp.demo/cache/pic.jpg
        File mainFile = new File(image_path);
        //get desire file name
        String desireFileNameWOExtension = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_SENT_PATH + "/", "IMG");
        //prepare desire file desired file
        File desiredFile = new File(GlobalVariables.IMAGES_SENT_PATH + "/" + desireFileNameWOExtension);
        boolean ok = mainFile.renameTo(desiredFile);
        try {
            desiredFile = Util.ryanCopyFile(mainFile, desiredFile);
            String imageFileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
            File imageFile = new File(GlobalVariables.IMAGES_PATH, imageFileName);
            imageFile = Util.ryanCopyFile(mainFile, imageFile);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return desireFileNameWOExtension;
    }

    public String outgoingImage_Negative6(String image_path){
        File mainFile = new File(image_path);
        //get desire file name
        String desireFileNameWOExtension = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_SENT_PATH + "/", "IMG");
        //prepare desire file desired file
        File desiredFile = new File(GlobalVariables.IMAGES_SENT_PATH + "/" + desireFileNameWOExtension);
        try {
            desiredFile = Util.ryanCopyFile(mainFile, desiredFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return desireFileNameWOExtension;
    }
    //end for outgoing image

    //incoming outgoing can use
//    public void incomingCancelDownload(DownloadUploadHelper downloadUploadHelper, String url, String str_row_id, int uniqueID, int rowId){
//        if (url == null || url.equals("")) {
//            //cancel to get url
//            if(GlobalVariables.retrofitCallStack.containsKey(str_row_id)) {
//                GlobalVariables.retrofitCallStack.get(str_row_id).cancel();
//            }
//        }
//        //if no url, means running asynctask and download manager is running
//        else {
//            //cancel asynctask and download manager progress
//            //cancelDownloadManager will update status -3
//            downloadUploadHelper.cancelDownloadManager(uniqueID, rowId);
//        }
//    }

    public boolean incomingStartDownload(DatabaseHelper databaseHelper, String url, String str_row_id, String resourceID, String type){
        //check if has url to download
        if (url == null || url.equals("") || !URLUtil.isValidUrl(url)) {
            return true;
        }
        //use video url to download
        else {
            //download using url
            ContentValues cvDownloading = new ContentValues();
            cvDownloading.put(DatabaseHelper.MSG_MEDIASTATUS, 1);
            databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvDownloading, DatabaseHelper.MSG_ROW, str_row_id);
//            databaseHelper.download(resourceID, url, str_row_id, type);
            startDownloadWorker(resourceID, url, str_row_id, type);
        }
        return false;
    }
    //end incoming out going can use

    //
    public void resourceRetro(Context context, String jid){
        String access_token = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
        retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(jid, "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<Resource1v3Model>() {
            @Override
            public void onResponse(retrofit2.Call<Resource1v3Model> call, retrofit2.Response<Resource1v3Model> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                String image_data = response.body().getThumbnail();
                String resourceURL = response.body().getResource_url();
                if (resourceURL != null && !resourceURL.equals("") && !resourceURL.equals("null")) {
                    //try to download image from resource url and load to preview
                    downloadFromUrl(resourceURL, jid, image_data);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Resource1v3Model> call, Throwable t) {
            }
        });
    }

    public void downloadFromUrl(String imageURL, String jid, String image_data){
        DownloadFromUrlInterface client = RetrofitAPIClient.getClient().create(DownloadFromUrlInterface.class);
        retrofit2.Call<ResponseBody> call = client.downloadFileWithDynamicUrlAsync(imageURL);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    updateResponseFromDownloadUrl(unsuccessful, jid, null, null, null);
                    return;
                }
                try {
                    byte[] fullImage = response.body().bytes();
                    byte[] memImgByte = null;
                    if(image_data != null){
                        memImgByte = Base64.decode(image_data, Base64.DEFAULT);
                    }
                    updateResponseFromDownloadUrl(successful, jid, memImgByte, imageURL, fullImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateResponseFromDownloadUrl(failed, jid, null, null, null);
            }
        });
    }

    public void updateResponseFromDownloadUrl(String status, String jid, byte[] memImgByte, String imageURL, byte[] fullImage){
        ContentValues cvCR = new ContentValues();
        if(status.equals(unsuccessful) || status.equals(failed)){
            cvCR.putNull(DatabaseHelper.CR_COLUMN_PHOTOURL);
            cvCR.putNull(DatabaseHelper.CR_COLUMN_PROFILEFULL);
        }else{
            if(memImgByte != null){
                cvCR.put(DatabaseHelper.CR_COLUMN_PROFILEPHOTO, memImgByte);
            }
            cvCR.put(DatabaseHelper.CR_COLUMN_PHOTOURL, imageURL);
            cvCR.put(DatabaseHelper.CR_COLUMN_PROFILEFULL, fullImage);
        }
        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.CR_TABLE_NAME, cvCR, DatabaseHelper.CR_COLUMN_JID, jid);
    }


    // fri august 24
    public void renamedMediaBasedOnType(DecryptionHelper decryptionHelper, String type, String resource_id, String row_id){
        switch (type) {
            case "image": {
                File wrongnameImageFile = new File(GlobalVariables.IMAGES_PATH + resource_id + ".jpg");
                final File decryptimg = decryptionHelper.decryptImg(wrongnameImageFile, resource_id);

                String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
                File file = new File(GlobalVariables.IMAGES_PATH, fileName);
                boolean renamedCompleted = decryptimg.renameTo(file);
                if (renamedCompleted) {
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                    cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, fileName);
                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, row_id);
                    Soapp.getInstance().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    if(decryptimg.exists()){
                        decryptimg.delete();
                    }
                } else {
                    //re try maybe ?
                }
                break;
            }
            case "video": {
                File wrongnameVideoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + resource_id + ".jpg");
                File wrongnameVideoFile = new File(GlobalVariables.VIDEO_PATH, resource_id + ".mp4");
                final File decryptvideo = decryptionHelper.decryptVideo(wrongnameVideoFile, resource_id);

                String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/", "VID");
                String renamedFileName = fileName.substring(0, fileName.indexOf("."));

                File videoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH, renamedFileName + ".jpg");
                File videoFile = new File(GlobalVariables.VIDEO_PATH, renamedFileName + ".mp4");
                boolean renamedThumbCompleted = wrongnameVideoThumbnailFile.renameTo(videoThumbnailFile);
                boolean renamedVideoCompleted = wrongnameVideoFile.renameTo(videoFile);
                if (renamedThumbCompleted && renamedVideoCompleted) {
                    Soapp.getInstance().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                    cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, renamedFileName);
                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, row_id);
                } else {
                    //re try maybe ?
                }
                break;
            }
            case "audio": {
                File wrongNameAudioFile = new File(GlobalVariables.AUDIO_PATH + resource_id + ".m4a");
                final File decryptaudio = decryptionHelper.decryptAudio(wrongNameAudioFile, resource_id + ".m4a");
                String correctAudioFileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.AUDIO_PATH, "AUD");
                File correctFile = new File(GlobalVariables.AUDIO_PATH, correctAudioFileName);
                boolean renamedCompleted = decryptaudio.renameTo(correctFile);
                if (renamedCompleted) {
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, correctAudioFileName);
                    cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, row_id);
                } else {
                    //re try mayb
                }
                break;
            }
        }
    }
    //
    public String[] checkDownloadProgress(Context context, DatabaseHelper databaseHelper, final String enqueueId, final String rowid) {
        String statusText = "";
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query dlQuery = new DownloadManager.Query();
        dlQuery.setFilterById(Long.parseLong(enqueueId));
        Cursor cursor = downloadManager.query(dlQuery);

        if (cursor.moveToFirst()) {
            int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
//
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("incomingVideoDownload"+rowid);
//            broadcastIntent.putExtra("percent", (float) dl_progress);
//            context.sendBroadcast(broadcastIntent);

            if (dl_progress == 100) {
                ContentValues cvMSG = new ContentValues();
                cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 99);
                databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowid);
            }

            //column for download  status
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusText = "STATUS_SUCCESSFUL";
                    break;

                case DownloadManager.STATUS_PENDING:
                    statusText = "STATUS_PENDING";
                    break;

                case DownloadManager.STATUS_PAUSED:
                    statusText = "STATUS_PAUSED";
                    break;

                case DownloadManager.STATUS_RUNNING:
                    statusText = "STATUS_RUNNING";
                    break;

                case DownloadManager.STATUS_FAILED:
                    statusText = "STATUS_FAILED";
                    break;
            }
            cursor.close();
            return new String[]{statusText};
        } else {
            cursor.close();
            return new String[]{statusText};
        }
    }
    //
    public void uploadingWorkerObserver(DatabaseHelper databaseHelper, String row_id, Context context){
        String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        if(worker_id != null){
            WorkManager workManager = WorkManager.getInstance();
            LiveData<WorkStatus> workStatusLiveData = workManager.getStatusById(UUID.fromString(worker_id));
            workStatusLiveData.observe((LifecycleOwner) context, state->{

                if(state != null){
                    int media_status = -3;
                    if(state.getState() != State.SUCCEEDED && state.getState() != State.ENQUEUED){
                        media_status = -3;
                    }else if(state.getState() == State.SUCCEEDED){
                        media_status = 100;
                    }
                    if(state.getState() != State.RUNNING && state.getState() != State.ENQUEUED) {
                        ContentValues cv3 = new ContentValues();
                        cv3.put(DatabaseHelper.MSG_MEDIASTATUS, media_status);
                        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv3, DatabaseHelper.MSG_ROW, "" + row_id);
                        databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, "" + row_id);
                        workManager.cancelWorkById(UUID.fromString(worker_id));
                        workStatusLiveData.removeObservers((LifecycleOwner) context);
                    }
                }
            });
        }

    }

    //might be usefull
    public boolean checkIsVideo(Context context, String video_path){
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.parse(video_path));
            //
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            return "yes".equals(hasVideo);
        }catch (Exception e){
            return false;
        }
    }

    //*********** fellow workers for download start ***********
    public void startDownloadRetorfitWorker(String resource_id, String row_id, String type){
        Data data_1 = new Data.Builder()
                .putString("resource_id", resource_id)
                .putString("row_id", row_id)
                .putString("type", type)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DownloadRetrofitWorker.class)
                .setInputData(data_1)
                .addTag("DownloadRetrofit")
                .setInitialDelay((long)0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        try {
            databaseHelper.newMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        }catch (Exception e){
            Log.i("diao","startDownloadRetorfitWorker: "+e.toString());
        }
    }

    public void startDownloadWorker(String resourceID, String url, String row_id, String type){
        WorkManager workManager = WorkManager.getInstance();
        Data data = new Data.Builder()
                .putString("resource_id", resourceID)
                .putString("url", url)
                .putString("row_id", row_id)
                .putString("type", type)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DownloadManagerWorker.class)
                .setInputData(data)
                .addTag("DownloadManager")
                .setInitialDelay((long)0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        try {
            String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
            if(worker_id == null){
                databaseHelper.newMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            } else {
                databaseHelper.updateMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            }
            //
            workManager.enqueue(oneTimeWorkRequest);
        }catch (Exception e){
            Log.i("diao","startDownloadWorker: "+e.toString());
        }
    }

    public void stopDownloadWorker(String row_id, int download_id){
        String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        if(id != null && download_id != 0){
            WorkManager.getInstance().cancelWorkById(UUID.fromString(id));
            DownloadManager downloadManager = (DownloadManager) Soapp.getInstance().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            int ok = downloadManager.remove(download_id);
            if (ok == 1) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.MSG_MEDIASTATUS, -3);
                databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                        DatabaseHelper.MSG_ROW, row_id);

                databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
            }
        }else{
            if(id != null){//worker id may not be null
                WorkManager.getInstance().cancelWorkById(UUID.fromString(id));
                databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
            }
            if(download_id != 0){//download id may not be null, download manager has not started
                DownloadManager downloadManager = (DownloadManager) Soapp.getInstance().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.remove(download_id);
            }
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.MSG_MEDIASTATUS, -3);
            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv,
                    DatabaseHelper.MSG_ROW, row_id);
        }
    }
    //*********** fellows worker for download end ***********

    //*********** fellows worker for upload start ***********

    //*********** image workers start ***********
    public void startCompressImageWorker(String group_display_name, String row_id, String path, String orient, String jid, String unique_id, String status){
        WorkManager workManager = WorkManager.getInstance();
        Data d = new Data.Builder()
                .putString("row_id", row_id)
                .putString("path", path)
                .putString("orient", orient)
                .putString("jid", jid)
                .putString("unique_id", unique_id)
                .putString("status", status)
                .putString("group_display_name", group_display_name)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(CompressImageWorker.class)
                .setInputData(d)
                .addTag("CompressImage")
                .setInitialDelay((long)0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        try {
            databaseHelper.newMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            workManager.enqueue(oneTimeWorkRequest);
        }catch (Exception e){
            Log.i("diao","startCompressImageWorker: "+e.toString());
        }


    }

    public void startUploadImageWorker(String group_display_name, String row_id, String path, String orient, String jid, String unique_id){
        Data d = new Data.Builder()
                .putString("row_id", row_id)
                .putString("path", path)
                .putString("orient", orient)
                .putString("jid", jid)
                .putString("unique_id", unique_id)
                .putString("group_display_name", group_display_name)
                .build();

        WorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadImageWorker.class)
                .setInputData(d)
                .addTag("UploadImage")
                .setInitialDelay((long)0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
        cv.put(DatabaseHelper.MEDIA_WORKER_WORKER_ENQUEUE_ID, oneTimeWorkRequest.getId().toString());
        try {
            databaseHelper.rdb.insertQuery().insertMediaWorker(MediaWorker.fromContentValues(cv));
            WorkManager.getInstance().enqueue(oneTimeWorkRequest);
        }catch (Exception e){
            Log.i("diao","startUploadImageWorker: "+e.toString());
        }
    }

    public void stopImageWorker(String row_id){
        String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        if(worker_id != null){
            WorkManager workManager = WorkManager.getInstance();
            workManager.cancelWorkById(UUID.fromString(worker_id));
        }
    }
    //*********** image workers end ***********

    //*********** video workers start ***********
    public void startCompressVideoWorker(Context context, String row_id, String video_path, String video_name_w_o_extension, String jid_str, String group_display_name, int issender, String unique_id){
        boolean isVideoCompressing = databaseHelper.checkForIsVideoCompressing();
        if (!isVideoCompressing) {
            Data d = new Data.Builder()
                    .putString("row_id", row_id)
                    .putString("video_path", video_path)
                    .putString("video_name_w_o_extension", video_name_w_o_extension)
                    .putString("jid_str", jid_str)
                    .putInt("issender", issender)
                    .putString("unique_id", unique_id)
                    .putString("group_display_name", group_display_name)
                    .build();

            WorkManager workManager = WorkManager.getInstance();
            WorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(CompressVideoWorker.class)
                    .setInputData(d)
                    .addTag("CompressVideo")
                    .setInitialDelay((long) 0.1, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                    .build();

            //
            try {
                String str_worker_id = oneTimeWorkRequest.getId().toString();
                databaseHelper.newMediaWorkerRow(row_id, str_worker_id);
                workManager.enqueue(oneTimeWorkRequest);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            databaseHelper.updateMsgMediaStatusReturn(4, row_id);
        }
    }

    public void startUploadVideoWorker(String video_path, String row_id, String video_thumbnail_path, String jid, String group_display_name, String video_orient, String unique_id){
        Data d = new Data.Builder()
                .putString("video_path", video_path)
                .putString("row_id", row_id)
                .putString("video_thumbnail_path", video_thumbnail_path)
                .putString("jid", jid)
                .putString("video_orient", video_orient)
                .putString("unique_id", unique_id)
                .putString("group_display_name", group_display_name)
                .build();

        WorkManager workManager = WorkManager.getInstance();
        WorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadVideoWorker.class)
                .setInputData(d)
                .addTag("UploadVideo")
                .setInitialDelay((long) 0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        try{
            databaseHelper.insertOrUpdateMediaWorkerRow(row_id, oneTimeWorkRequest.getId().toString());
            workManager.enqueue(oneTimeWorkRequest);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopCompressVideo(String row_id){
        String str_worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        UUID uuid_worker_id = UUID.fromString(str_worker_id);
        WorkManager workManager = WorkManager.getInstance();
        workManager.cancelWorkById(uuid_worker_id);
        try {
            databaseHelper.updateMsgMediaStatusReturn(7, row_id);
            databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void compressNextVideo(){
        if(databaseHelper.checkExistingOfUmcompressedVideo()) {
            String[] result = databaseHelper.getOneQueueingVideo();
            String videoPathX = result[0];
            String videoNameWOExtensionX = result[1];
            String uniqueIdX = result[2];
            String jidStrX = result[3];
            int issenderX = Integer.valueOf(result[4]);
            String rowIdX = result[5];
            String group_display_name = result[6];
            startCompressVideoWorker(Soapp.getInstance().getApplicationContext(), rowIdX, videoPathX, videoNameWOExtensionX, jidStrX, group_display_name, issenderX, uniqueIdX);
        }
    }

    public void videoCompressWorkerObserver(String row_id, Context context){
        String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        LifecycleOwner lifecycleOwner = (LifecycleOwner) context;
        if(worker_id != null){
            WorkManager workManager = WorkManager.getInstance();
            UUID uuid = UUID.fromString(worker_id);
            LiveData<WorkStatus> workStatusLiveData = workManager.getStatusById(uuid);
            workStatusLiveData.observe(lifecycleOwner, observe->{
                if(observe != null){
                    State worker_state = observe.getState();

                    //worker in queue, wait for n sec
                    //after n sec, cancel the worker
                    if(worker_state == State.ENQUEUED) {
                        new CountDownTimer(1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                if(observe.getState() ==  State.RUNNING || observe.getState() ==  State.CANCELLED){
                                    cancel();
                                }
                            }

                            public void onFinish() {
                                if( observe.getState() != State.RUNNING){
                                    databaseHelper.updateMsgMediaStatusReturn(7, row_id);
                                    databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
                                    workManager.cancelWorkById(uuid);
                                    workStatusLiveData.removeObservers(lifecycleOwner);
                                    compressNextVideo();
                                }
                            }
                        }.start();
                    }else if(worker_state == State.FAILED){
                        databaseHelper.updateMsgMediaStatusReturn(7, row_id);
                        workStatusLiveData.removeObservers(lifecycleOwner);
                    }

                }else{
                    workStatusLiveData.removeObservers(lifecycleOwner);
                }
            });
        }
    }

    public void videoUploadWorkerObserver(String row_id, Context context){
        String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
        LifecycleOwner lifecycleOwner = (LifecycleOwner) context;
        if(worker_id != null){
            WorkManager workManager = WorkManager.getInstance();
            UUID uuid = UUID.fromString(worker_id);
            LiveData<WorkStatus> workStatusLiveData = workManager.getStatusById(uuid);
            workStatusLiveData.observe(lifecycleOwner, observer->{
                if(observer != null){
                    State worker_state = observer.getState();
                    if(worker_state == State.ENQUEUED) {
                        new CountDownTimer(1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                if(observer.getState() ==  State.RUNNING || observer.getState() ==  State.CANCELLED){
                                    cancel();
                                }
                            }

                            public void onFinish() {
                                if( observer.getState() != State.RUNNING){
                                    databaseHelper.updateMsgMediaStatusReturn(-3, row_id);
                                    databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME,DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
                                    workManager.cancelWorkById(uuid);
                                    workStatusLiveData.removeObservers(lifecycleOwner);
                                    compressNextVideo();
                                }
                            }
                        }.start();
                    } else if(worker_state == State.FAILED){
                        databaseHelper.updateMsgMediaStatusReturn(-3, row_id);
                        workStatusLiveData.removeObservers(lifecycleOwner);
                    }
                }
                else{
                    workStatusLiveData.removeObservers(lifecycleOwner);
                }
            });
        }
    }
    //*********** video workers end ***********

    //*********** audio workers start ***********
    public boolean startUploadAudioWorker(String audio_path, String row_id, String unique_id, String jid, String group_display_name){
        Data d = new Data.Builder()
                .putString("audio_path", audio_path)
                .putString("row_id", row_id)
                .putString("unique_id", unique_id)
                .putString("jid", jid)
                .putString("group_display_name", group_display_name)
                .build();

        WorkManager workManager = WorkManager.getInstance();
        WorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadAudioWorker.class)
                .setInputData(d)
                .addTag("UploadAudio")
                .setInitialDelay((long) 0.1, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        try {
            String worker_id = oneTimeWorkRequest.getId().toString();
            databaseHelper.newMediaWorkerRow(row_id, worker_id);
            workManager.enqueue(oneTimeWorkRequest);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public void stopUploadAudioWorker(String worker_id){
        WorkManager workManager = WorkManager.getInstance();
        workManager.cancelWorkById(UUID.fromString(worker_id));
    }

    public void audioWorkerObserver(Context context, String worker_id, String row_id){
        WorkManager workManager = WorkManager.getInstance();
        UUID uuid = UUID.fromString(worker_id);
        LifecycleOwner lifecycleOwner = (LifecycleOwner)context;
        LiveData<WorkStatus> workStatusLiveData = workManager.getStatusById(uuid);
        workStatusLiveData.observe(lifecycleOwner, observer->{
            if(observer != null){
                State state = observer.getState();
                if(state == State.ENQUEUED) {
                    new CountDownTimer(1000, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            if (observer.getState() == State.ENQUEUED) {
                                databaseHelper.updateMsgMediaStatusReturn(-3, row_id);
                                workManager.cancelWorkById(uuid);
                                workStatusLiveData.removeObservers(lifecycleOwner);
                            }
                        }
                    }.start();
                }
            }
        });
    }
    //*********** audio workers end ***********

    //*********** fellows worker for upload end ***********

    //september 6 2018
    public void videoLoadingListener(int videoUploadStatus, Context context, ProgressBar progressBar, int rowId){
        MediaRequestReceiver receiver;
        IntentFilter filter24 = null;
        if(videoUploadStatus == -7 || videoUploadStatus == 1 ){
            if (videoUploadStatus == -7){
                filter24 = new IntentFilter("outgoingCompressVideo"+rowId);
            }
            if(videoUploadStatus == 1){
                filter24 = new IntentFilter("outgoingUploadVideo"+rowId);
            }
            receiver = new MediaRequestReceiver(progressBar);
            context.registerReceiver( receiver, filter24);
        }
    }

    public class MediaRequestReceiver extends BroadcastReceiver {

        ProgressBar mProgressBar;

        public MediaRequestReceiver(ProgressBar progressBar){
            mProgressBar = progressBar;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                float percent = intent.getFloatExtra("percent", 0);
//                Log.i("diao",getClass().getSimpleName()+", "+percent);
                mProgressBar.setProgress((int)percent);
            }
            //update your progressbar here
        }
    }
    //
}
