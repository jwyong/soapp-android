package com.soapp.WorkManager.media.upload.video;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.FileModel;
import com.soapp.SoappApi.Interface.UploadChatVideo;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.camera.Util;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UploadVideoRequestBody;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.work.Data;
import androidx.work.Worker;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class UploadVideoWorker extends Worker implements UploadVideoRequestBody.UploadCallBack {

    DatabaseHelper databaseHelper;
    Preferences preferences;
    Context itemView;
    SingleChatStanza singleChatStanza;
    GroupChatStanza groupChatStanza;
    Call<FileModel> call;
    String rowId;

    public UploadVideoWorker() {
        databaseHelper = DatabaseHelper.getInstance();
        preferences = Preferences.getInstance();
        itemView = Soapp.getInstance().getApplicationContext();
        singleChatStanza = new SingleChatStanza();
        groupChatStanza = new GroupChatStanza();
    }

    @Override
    public Result doWork() {
        Data data = getInputData();
        String videoPath = data.getString("video_path");
        rowId = data.getString("row_id");
        String videoThumbnailPath = data.getString("video_thumbnail_path");
        String jid = data.getString("jid");
        String videoOrient = data.getString("video_orient");
        String uniqueId = data.getString("unique_id");
        String group_display_name = data.getString("group_display_name");
        //
        databaseHelper.updateMsgMediaStatusReturn(1, rowId);
        //
        String access_token = preferences.getValue(itemView, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final File videoFile = new File(videoPath);

//        final File ReEncryptvideo = encryptionHelper.encryptVideo(videoFile, videoPath);

        final UploadVideoRequestBody videoFileBody = new UploadVideoRequestBody(videoFile, this, rowId);
        final File videoThumbnailFile = new File(videoThumbnailPath);
        final String self_username = preferences.getValue(itemView, GlobalVariables
                .STRPREF_USERNAME);
        RequestBody videoThumbnailRB = RequestBody.create(APIGlobalVariables.JPEG, videoThumbnailFile);
        MultipartBody.Part videofilePart = MultipartBody.Part.createFormData("file", "name.mp4", videoFileBody);
        MultipartBody.Part type = MultipartBody.Part.createFormData("type", "video");
        MultipartBody.Part videoThumbnailPart = MultipartBody.Part.createFormData("thumbnail", "name.jpg", videoThumbnailRB);

        //build retrofit
        UploadChatVideo client = RetrofitAPIClient.getClient().create(UploadChatVideo.class);
        call = client.uploadvideo("Bearer " + access_token,
                videofilePart, type, videoThumbnailPart);

        try {
            Response<FileModel> response = call.execute();
            if (!response.isSuccessful()) {
                databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
                new MiscHelper().retroLogUnsuc(response, "reUploadVideo ", "JAY");
                return Result.FAILURE;
            } else {
                final String file_id = String.valueOf(response.body().getResource_id());
                final String file_url = String.valueOf(response.body().getResource_url());
                File copied_thumb_file = new File(Soapp.getInstance().getCacheDir() + rowId + ".jpg");
                try {
                    copied_thumb_file = Util.ryanCopyFile(videoThumbnailFile, copied_thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap video_thumb_bitmap = BitmapFactory.decodeFile(copied_thumb_file.getPath());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                video_thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String video_thumb_base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                //
                ContentValues cv = new ContentValues();
                cv.put("MediaStatus", 100);
                cv.put("MsgLatitude", video_thumb_base64);

                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                    if (jid.length() == 12) {
                        singleChatStanza.SoappVideoStanza(file_id, jid, videoOrient, file_url, self_username, uniqueId, video_thumb_base64);
                    } else {
                        String senderJid = preferences.getValue(itemView, GlobalVariables.STRPREF_USER_ID);
                        groupChatStanza.GroupVideo(jid, senderJid, group_display_name, videoOrient, uniqueId, file_id, self_username, video_thumb_base64);
                    }
                } else {
                    cv.put(DatabaseHelper.MSG_MSGOFFLINE, 1);
                    cv.put(DatabaseHelper.MSG_CONTACTNAME, file_id);
                    cv.put(DatabaseHelper.MSG_CONTACTNUMBER, file_url);
                    cv.put(DatabaseHelper.MSG_MSGLONGITUDE, videoOrient);
                }
                databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW, rowId);
                return Result.SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
            new MiscHelper().retroLogFailure(e, "reUploadVideo ", "JAY");
            return Result.FAILURE;
        }
    }

    @Override
    public void onProgressUpdate(int percentage, String uniqueId) {
        if(percentage % 9 == 0) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("outgoingUploadVideo" + rowId);
            broadcastIntent.putExtra("percent", (float) percentage);
            itemView.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish(String uniqueId) {

    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
        Log.i("mad", getClass().getSimpleName() + " onStopped");
        if (call != null) {
            call.cancel();
            databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, rowId);
        } else {
            databaseHelper.updateMsgMediaStatusReturn(-3, rowId);
        }
    }
}
