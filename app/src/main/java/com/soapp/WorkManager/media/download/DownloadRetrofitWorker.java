package com.soapp.WorkManager.media.download;

import android.content.ContentValues;
import android.content.Context;

import com.soapp.SoappApi.ApiModel.Resource1v3Model;
import com.soapp.SoappApi.Interface.Resource1v3Interface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.ChatHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.io.IOException;

import androidx.work.Data;
import androidx.work.Worker;
import retrofit2.Response;

import static com.soapp.sql.DatabaseHelper.MSG_MEDIASTATUS;
import static com.soapp.sql.DatabaseHelper.MSG_MSGINFOURL;
import static com.soapp.sql.DatabaseHelper.MSG_ROW;
import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

public class DownloadRetrofitWorker extends Worker{
    DatabaseHelper databaseHelper;
    Context context;
    ChatHelper chatHelper;

    public DownloadRetrofitWorker(){
        databaseHelper = DatabaseHelper.getInstance();
        context = Soapp.getInstance().getApplicationContext();
        chatHelper = new ChatHelper();
    }

    @Override
    public Result doWork() {
        Data data = getInputData();
        String rowId = data.getString("row_id");
        String resourceID = data.getString("resource_id");
        String mediaType = data.getString("type");
        //
        ContentValues cvDownloading = new ContentValues();
        cvDownloading.put(MSG_MEDIASTATUS, 1);
        databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvDownloading,
                MSG_ROW, rowId);
        //download url using resourceID
        String access_token = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);
        Resource1v3Interface resource1v3Interface = RetrofitAPIClient.getClient().create(Resource1v3Interface.class);
        retrofit2.Call<Resource1v3Model> call = resource1v3Interface.getResource(resourceID, "Bearer " + access_token);
        try {
            Response<Resource1v3Model> response = call.execute();
            if(!response.isSuccessful()){
                ContentValues cvMSG = new ContentValues();
                cvMSG.put(MSG_MEDIASTATUS, -3);
                databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, rowId);
                return Result.FAILURE;
            }else{
                //only media type video has thumbnail
                if (mediaType.equals("video")) {
                    final String thumbnailBase64 = response.body().getThumbnail();
                    databaseHelper.convertBase64ToBimapAndSaveToFolder(thumbnailBase64, resourceID);
                }

                final String url = response.body().getResource_url();

                ContentValues cvMSG = new ContentValues();
                cvMSG.put(MSG_MSGINFOURL, url);
                databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, rowId);

                //download media base on url
                String id = databaseHelper.rdb.selectQuery().getMediaWorkerId(rowId);
                if(id != null){
                    chatHelper.startDownloadWorker(resourceID, url, rowId, mediaType);
                }
                return Result.SUCCESS;
            }
        } catch (IOException e) {
            ContentValues cvMSG = new ContentValues();
            cvMSG.put(MSG_MEDIASTATUS, -3);
            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, rowId);
            e.printStackTrace();
            return Result.FAILURE;
        }
    }
}
