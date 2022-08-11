package com.soapp.WorkManager.media.upload.audio;

import android.content.ContentValues;
import android.content.Context;

import com.soapp.SoappApi.ApiModel.FileModel;
import com.soapp.SoappApi.Interface.UploadChatImage;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.File;
import java.io.IOException;

import androidx.work.Data;
import androidx.work.Worker;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class UploadAudioWorker extends Worker{

    Context mContext;
    Preferences preferences;
    EncryptionHelper encryptionHelper;
    DatabaseHelper databaseHelper;
    SingleChatStanza singleChatStanza;
    GroupChatStanza groupChatStanza;
    Call<FileModel> call;
    //
    String rowId;

    public UploadAudioWorker(){
        mContext = Soapp.getInstance().getApplicationContext();
        preferences = Preferences.getInstance();
        encryptionHelper = new EncryptionHelper();
        databaseHelper = DatabaseHelper.getInstance();
        singleChatStanza = new SingleChatStanza();
        groupChatStanza = new GroupChatStanza();
    }

    @Override
    public Result doWork() {

        Data data = getInputData();
        String audioPath = data.getString("audio_path");
        rowId = data.getString("row_id");
        String uniqueId = data.getString("unique_id");
        String jid = data.getString("jid");
        String group_display_name = data.getString("group_display_name");

        databaseHelper.updateMsgMediaStatusReturn(1, rowId);

        String access_token = preferences.getValue(mContext, GlobalVariables.STRPREF_ACCESS_TOKEN);

        String path = GlobalVariables.AUDIO_SENT_PATH + audioPath;
        File UnencryptAudiofile = new File(path);

        final File encryptaudio = encryptionHelper.encryptAudio(UnencryptAudiofile, path);

        RequestBody audio = RequestBody.create(MediaType.parse("m4a/*"), encryptaudio);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "name.m4a", audio);
        MultipartBody.Part type = MultipartBody.Part.createFormData("type", "audio");

        //build retrofit
        UploadChatImage client = RetrofitAPIClient.getClient().create(UploadChatImage.class);
        call = client.uploadfile("Bearer " + access_token, filePart,
                type);
        try {
            Response<FileModel> response = call.execute();
            if(!response.isSuccessful()){
                databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
                return Result.FAILURE;
            }else{
                final String file_id = String.valueOf(response.body().getResource_id());
                final String file_url = String.valueOf(response.body()
                        .getResource_url());

                String username = preferences.getValue(mContext,
                        GlobalVariables.STRPREF_USERNAME);

                ContentValues cv = new ContentValues();
                cv.put("MediaStatus", 100);
                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    if(jid.length() == 12){
                        singleChatStanza.SoappAudioStanza(file_id, jid, file_url, username, uniqueId);
                    }else{
                        String user_id = preferences.getValue(mContext, GlobalVariables.STRPREF_USER_ID);
                        groupChatStanza.GroupAudio(jid, user_id, username, file_id, uniqueId, file_url, group_display_name);
                    }
                } else {
                    cv.put(DatabaseHelper.MSG_CONTACTNAME, file_id);
                    cv.put(DatabaseHelper.MSG_CONTACTNUMBER, file_url);
                    cv.put(DatabaseHelper.MSG_MSGOFFLINE, 1);
                }

                databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW,
                        rowId);

                return Result.SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
            return Result.FAILURE;
        }
    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
        if(call != null){
            call.cancel();
        }else{
            databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
        }
        databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, rowId);
    }
}
