package com.soapp.WorkManager;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.soapp.global.DecryptionHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.io.File;

import androidx.work.Data;
import androidx.work.Worker;

public class DownloadWorker extends Worker{
    //
//    private DatabaseHelper databaseHelper = new DatabaseHelper();
//    private DecryptionHelper decryptionHelper = new DecryptionHelper();
    //
    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String resource_id = data.getString("resource_id");
        String url = data.getString("url");
        String row_id = data.getString("row_id");
        String type = data.getString("type");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                download(resource_id, url, row_id, type);
            }
        });
        return Result.SUCCESS;
    }

    public void download(String resourceID, String url, String rowid, String type) {
        DatabaseHelper databaseHelper = new DatabaseHelper();
        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
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
        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowid);

        //add to asynctaskStack, use when cancel download
        new DownloaderAsyncTask(getApplicationContext(), databaseHelper).execute(String.valueOf(enqueueId), rowid, type, resourceID);
    }

    private String[] checkDownloadProgress(DatabaseHelper databaseHelper, final String enqueueId, final String rowid) {
        String statusText = "";
        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query dlQuery = new DownloadManager.Query();
        dlQuery.setFilterById(Long.parseLong(enqueueId));
        Cursor cursor = downloadManager.query(dlQuery);

        if (cursor.moveToFirst()) {
            int bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);


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

    //    String.valueOf(enqueueId), rowid, type, resourceID
    class DownloaderAsyncTask extends AsyncTask<String, Integer, Void> {
        final Context mContext;
        final DatabaseHelper mDatabaseHelper;
        DecryptionHelper decryptionHelper = new DecryptionHelper();

        public DownloaderAsyncTask(Context context, DatabaseHelper databaseHelper) {
            mContext = context;
            mDatabaseHelper = databaseHelper;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        //String.valueOf(enqueueId), rowId, type
        @SuppressLint("RestrictedApi")
        @Override
        protected Void doInBackground(String... strings) {
            int STATUS_PAUSED_COUNT = 0;
            boolean downloading = true;
            while (downloading && !isCancelled()) {
                String[] ok = checkDownloadProgress(mDatabaseHelper, strings[0], strings[1]);

                if (ok[0].equals("STATUS_SUCCESSFUL")) { //download success

                    switch (strings[2]) {
                        case "image": {
                            File wrongnameImageFile = new File(GlobalVariables.IMAGES_PATH + strings[3] + ".jpg");

                            final File decryptimg = decryptionHelper.decryptImg(wrongnameImageFile, strings[3]);

                            String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
                            File file = new File(GlobalVariables.IMAGES_PATH, fileName);
                            boolean renamedCompleted = decryptimg.renameTo(file);
                            if (renamedCompleted) {
                                ContentValues cvMSG = new ContentValues();
                                cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                                cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, fileName);

                                mDatabaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, strings[1]);

                                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                            } else {
                                //re try maybe ?
                            }
                            break;
                        }
                        case "video": {
                            File wrongnameVideoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + strings[3] + ".jpg");
                            File wrongnameVideoFile = new File(GlobalVariables.VIDEO_PATH, strings[3] + ".mp4");
                            final File decryptvideo = decryptionHelper.decryptVideo(wrongnameVideoFile, strings[3]);

                            String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/", "VID");
                            String renamedFileName = fileName.substring(0, fileName.indexOf("."));

                            File videoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH, renamedFileName + ".jpg");
                            File videoFile = new File(GlobalVariables.VIDEO_PATH, renamedFileName + ".mp4");
                            boolean renamedThumbCompleted = wrongnameVideoThumbnailFile.renameTo(videoThumbnailFile);
                            boolean renamedVideoCompleted = wrongnameVideoFile.renameTo(videoFile);
                            if (renamedThumbCompleted && renamedVideoCompleted) {
                                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
                                ContentValues cvMSG = new ContentValues();
                                cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                                cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, renamedFileName);
                                mDatabaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, strings[1]);
                            } else {
                                //re try maybe ?
                            }
                            break;
                        }
                        case "audio": {
                            File wrongNameAudioFile = new File(GlobalVariables.AUDIO_PATH + strings[3] + ".m4a");
                            final File decryptaudio = decryptionHelper.decryptAudio(wrongNameAudioFile, strings[3] + ".m4a");
                            String correctAudioFileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.AUDIO_PATH, "AUD");
                            File correctFile = new File(GlobalVariables.AUDIO_PATH, correctAudioFileName);
                            boolean renamedCompleted = decryptaudio.renameTo(correctFile);
                            if (renamedCompleted) {
                                ContentValues cvMSG = new ContentValues();
                                cvMSG.put(DatabaseHelper.MSG_MSGINFOURL, correctAudioFileName);
                                cvMSG.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                                mDatabaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, strings[1]);

                            } else {
                                //re try mayb
                            }
                            break;
                        }
                    }
                    downloading = false;
                }else if(ok[0].equals("STATUS_FAILED")){
                        mDatabaseHelper.stopDownloadWorker(strings[1], Integer.valueOf(strings[0]));
                }else if(!ok[0].equals("STATUS_RUNNING")){
                    STATUS_PAUSED_COUNT += 1;
                    if (STATUS_PAUSED_COUNT > 10) {
                        mDatabaseHelper.stopDownloadWorker(strings[1], Integer.valueOf(strings[0]));
                    }
                }
            }
            return null;
        }
    }
}
