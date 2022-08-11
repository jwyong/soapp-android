package com.soapp.WorkManager.media.download;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.soapp.global.ChatHelper;
import com.soapp.global.DecryptionHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import androidx.work.Data;
import androidx.work.Worker;

public class DownloadingWorker extends Worker {
    //
    DatabaseHelper databaseHelper;
    DecryptionHelper decryptionHelper;
    Context mContext;
    ChatHelper chatHelper;
    //

    public DownloadingWorker(){
        databaseHelper = DatabaseHelper.getInstance();
        decryptionHelper = new DecryptionHelper();
        mContext  = Soapp.getInstance().getApplicationContext();
        chatHelper = new ChatHelper();
    }
    @Override
    public Result doWork() {
        Log.i("ryan","DownloadingWorker 3");
        Data data = getInputData();
        String resource_id = data.getString("resource_id");
        String enqueueId = data.getString("enqueueId");
        String row_id = data.getString("row_id");
        String type = data.getString("type");
        String[] result = new ChatHelper().checkDownloadProgress(mContext, databaseHelper, enqueueId, row_id);
        if(result[0].equals("STATUS_SUCCESSFUL")){
            new ChatHelper().renamedMediaBasedOnType(decryptionHelper, type, resource_id, row_id);
            return Result.SUCCESS;
        }
        AsyncTask asyncTask = new DownloaderAsyncTask().execute(String.valueOf(enqueueId), row_id, type, resource_id);
        AsyncTask.Status status = asyncTask.getStatus();
        while (status == AsyncTask.Status.RUNNING){
            status = asyncTask.getStatus();
            String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);
            if(id == null){
                asyncTask.cancel(true);
                return Result.FAILURE;
            }
            if(status != AsyncTask.Status.RUNNING){
                return Result.SUCCESS;
            }
        }
        return Result.FAILURE;
    }

    //    String.valueOf(enqueueId), rowid, type, resourceID
    class DownloaderAsyncTask extends AsyncTask<String, Integer, Void> {
//        final DatabaseHelper mDatabaseHelper;

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        //String.valueOf(enqueueId), rowId, type
        @Override
        protected Void doInBackground(String... strings) {
            int STATUS_PAUSED_COUNT = 0;
            boolean downloading = true;
            while (downloading && !isCancelled()) {
                String[] ok = new ChatHelper().checkDownloadProgress(mContext, databaseHelper, strings[0], strings[1]);
                if (ok[0].equals("STATUS_SUCCESSFUL")) { //download success
                    new ChatHelper().renamedMediaBasedOnType(decryptionHelper, strings[2], strings[3], strings[1]);
                    downloading = false;
                }else if(ok[0].equals("STATUS_FAILED")){
                    chatHelper.stopDownloadWorker(strings[1], Integer.valueOf(strings[0]));
                }else if(!ok[0].equals("STATUS_RUNNING")){
                    STATUS_PAUSED_COUNT += 1;
                    if (STATUS_PAUSED_COUNT > 10) {
                        chatHelper.stopDownloadWorker(strings[1], Integer.valueOf(strings[0]));
                    }
                }
            }
            return null;
        }
    }
}
