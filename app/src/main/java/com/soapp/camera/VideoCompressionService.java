package com.soapp.camera;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.widget.Toast;

import com.iceteck.soappcompressor.SiliCompressor;
import com.iceteck.soappcompressor.videocompression.MediaController;
import com.soapp.R;
import com.soapp.SoappApi.APIGlobalVariables;
import com.soapp.SoappApi.ApiModel.FileModel;
import com.soapp.SoappApi.Interface.UploadChatVideo;
import com.soapp.SoappApi.RetrofitAPIClient;
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
import java.net.URISyntaxException;

import androidx.annotation.Nullable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static com.soapp.sql.DatabaseHelper.MSG_TABLE_NAME;

/**
 * Created by rlwt on 3/7/18.
 */

public class VideoCompressionService extends Service implements UploadVideoRequestBody.UploadCallBack {

    public String videoPath = "VIDEOPATH";
    public String uniqueId = "UNIQUEID";
    public String rowId = "ROWID";
    public String destinationPath = GlobalVariables.VIDEO_SENT_PATH;
    public String videoNameWOExtension = "VIDEONAMEEXTENSION";
    public int startID;
    public int issender;
    String jidStr;
    GroupChatStanza groupChatStanza = new GroupChatStanza();
    SingleChatStanza singleChatStanza = new SingleChatStanza();
    String[] continueCompressVideo;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    public VideoCompressionService() {

    }

    @Override
    public void onProgressUpdate(int percentage, String uniqueId) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish(String uniqueId) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startID = startId;
        if (intent == null) {
            if (!databaseHelper.checkForIsVideoCompressing()) {
                stopSelf(startId);
                return START_NOT_STICKY;
            }
            continueCompressVideo = databaseHelper.getOneCompressingVideo();
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            return START_STICKY;
        } else {
            videoPath = intent.getStringExtra("videoPath");
            rowId = intent.getStringExtra("rowId");
            uniqueId = intent.getStringExtra("uniqueId");
            destinationPath = intent.getStringExtra("destinationPath");
            videoNameWOExtension = intent.getStringExtra("videoNameWOExtension");
            jidStr = intent.getStringExtra("jid");
            issender = intent.getIntExtra("issender", -1);
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            return START_STICKY;
        }
    }

    public void reUploadVideo(final String rowId, final Context itemView, final String jid, String videoPath, String videoThumbnailPath, final String videoOrient) {
        //uploading dude
        ContentValues cvMSG = new ContentValues();
        cvMSG.put("MediaStatus", 1);
        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

        final Preferences preferences = Preferences.getInstance();
        //indi
        if (jid.length() == 12) {
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
            retrofit2.Call<FileModel> call = client.uploadvideo("Bearer " + access_token,
                    videofilePart, type, videoThumbnailPart);

            GlobalVariables.retrofitCallStack.put(rowId, call);

            call.enqueue(new retrofit2.Callback<FileModel>() {
                @Override
                public void onResponse(retrofit2.Call<FileModel> call, retrofit2.Response<FileModel> response) {
                    if (!response.isSuccessful()) {
                        ContentValues cvMSG = new ContentValues();
                        cvMSG.put("MediaStatus", -3);

                        new MiscHelper().retroLogUnsuc(response, "reUploadVideo ", "JAY");

                        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

                        Toast.makeText(itemView, R.string.onresponse_unsuccessful, Toast
                                .LENGTH_SHORT).show();
                        if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                            startNewThread();
                        } else {
                            stopSelf(startID);
                        }
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
                        singleChatStanza.SoappVideoStanza(file_id, jid, videoOrient, file_url, self_username, uniqueId, video_thumb_base64);

                        GlobalVariables.retrofitCallStack.remove(rowId);

                        ContentValues cvMSG = new ContentValues();
                        cvMSG.put("MsgLatitude", video_thumb_base64);
                        cvMSG.put("MediaStatus", 100);
                        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

                        if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                            startNewThread();
                        } else {
                            stopSelf(startID);
                        }
                        Toast.makeText(itemView, "video uploaded", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<FileModel> call, Throwable t) {
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put("MediaStatus", -3);
                    databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);
                    new MiscHelper().retroLogFailure(t, "reUploadVideo ", "JAY");
                    Toast.makeText(itemView, R.string.onfailure, Toast
                            .LENGTH_SHORT).show();
                    if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                        startNewThread();
                    } else {
                        stopSelf(startID);
                    }
                }

            });
        } else {
            String access_token = preferences.getValue(itemView, GlobalVariables.STRPREF_ACCESS_TOKEN);
            final File videoFile = new File(videoPath);

            // reEncrpyt video
//            final File GReEncrpytvideo = encryptionHelper.encryptVideo(videoFile, videoPath);

            final UploadVideoRequestBody videoFileBody = new UploadVideoRequestBody(videoFile, this, rowId);
            final File videoThumbnailFile = new File(videoThumbnailPath);

            RequestBody videoThumbnailRB = RequestBody.create(APIGlobalVariables.JPEG, videoThumbnailFile);

            MultipartBody.Part videofilePart = MultipartBody.Part.createFormData("file", "name.mp4", videoFileBody);
            MultipartBody.Part type = MultipartBody.Part.createFormData("type", "video");
            MultipartBody.Part videoThumbnailPart = MultipartBody.Part.createFormData("thumbnail", "name.jpg", videoThumbnailRB);

            //build retrofit
            UploadChatVideo client = RetrofitAPIClient.getClient().create(UploadChatVideo.class);
            retrofit2.Call<FileModel> call = client.uploadvideo("Bearer " + access_token, videofilePart, type, videoThumbnailPart);
            GlobalVariables.retrofitCallStack.put(rowId, call);

            call.enqueue(new retrofit2.Callback<FileModel>() {
                @Override
                public void onResponse(retrofit2.Call<FileModel> call, retrofit2.Response<FileModel> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "reUploadChatVideo ", "JAY");

                        ContentValues cvMSG = new ContentValues();
                        cvMSG.put("MediaStatus", -3);
                        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

                        Toast.makeText(itemView, R.string.onresponse_unsuccessful, Toast
                                .LENGTH_SHORT).show();
                        if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                            startNewThread();
                        } else {
                            stopSelf(startID);
                        }
                    } else {
                        File copied_thumb_file = new File(Soapp.getInstance().getCacheDir() + rowId + ".jpg");
                        String video_thumb_base64 = "";
                        try {
                            copied_thumb_file = Util.ryanCopyFile(videoThumbnailFile, copied_thumb_file);
                            Bitmap video_thumb_bitmap = BitmapFactory.decodeFile(copied_thumb_file.getPath());
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            video_thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            video_thumb_base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                            GlobalVariables.retrofitCallStack.remove(rowId);
                            String file_id = String.valueOf(response.body().getResource_id());
                            String senderJid = preferences.getValue(itemView,
                                    GlobalVariables.STRPREF_USER_ID);
                            String roomname = databaseHelper.getNameFromContactRoster(jid);
                            String self_username = preferences.getValue(itemView,
                                    GlobalVariables.STRPREF_USERNAME);

                            ContentValues cvMSG = new ContentValues();
                            cvMSG.put("MsgLatitude", video_thumb_base64);
                            cvMSG.put("MediaStatus", 100);
                            databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);
                                groupChatStanza.GroupVideo(jid, senderJid, roomname, videoOrient, uniqueId, file_id, self_username, video_thumb_base64);
                            } else {
                                cvMSG.put(DatabaseHelper.MSG_MSGOFFLINE, 1);
                                cvMSG.put(DatabaseHelper.MSG_CONTACTNAME, file_id);
                                cvMSG.put(DatabaseHelper.MSG_MSGLONGITUDE, videoOrient);
                            }
                            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW,
                                    rowId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ContentValues cvMSG = new ContentValues();
                        cvMSG.put("MsgLatitude", video_thumb_base64);
                        cvMSG.put("MediaStatus", 100);
                        databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);

                        Toast.makeText(itemView, "video uploaded", Toast.LENGTH_SHORT).show();
                        if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                            startNewThread();
                        } else {
                            stopSelf(startID);
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<FileModel> call, Throwable t) {
                    ContentValues cvMSG = new ContentValues();
                    cvMSG.put("MediaStatus", -3);
                    databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cvMSG, DatabaseHelper.MSG_ROW, rowId);
                    new MiscHelper().retroLogFailure(t, "reUploadChatVideo ", "JAY");

                    Toast.makeText(itemView, R.string.onfailure, Toast
                            .LENGTH_SHORT).show();
                    if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                        startNewThread();
                    } else {
                        stopSelf(startID);
                    }
                }

            });
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (continueCompressVideo != null) {
                VideoCompressThread v = new VideoCompressThread(continueCompressVideo[5], continueCompressVideo[0], destinationPath, continueCompressVideo[2], msg.arg1, continueCompressVideo[3], Integer.valueOf(continueCompressVideo[4]),continueCompressVideo[1]);
                v.start();
            } else {
                VideoCompressThread v = new VideoCompressThread(rowId, videoPath, destinationPath, videoNameWOExtension, msg.arg1, jidStr, issender, uniqueId);
                v.start();
            }
        }
    }

    class VideoCompressThread extends Thread {
        String videoPath;
        String destinationPath;
        String videoNameWOExtension;
        String uniqueId;
        int serviceId;
        String jidStr;
        String rowId;
        int issender;

        VideoCompressThread(String rowId, String videoPath, String destinationPath, String videoNameWOExtension, Integer serviceId, String jidstr, Integer issender, String uniqueId) {
            this.videoPath = videoPath;
            this.destinationPath = destinationPath;
            this.videoNameWOExtension = videoNameWOExtension;
            this.uniqueId = uniqueId;
            this.serviceId = serviceId;
            this.jidStr = jidstr;
            this.issender = issender;
            this.rowId = rowId;
        }

        public void run() {
            //compressing dude
            int ok = databaseHelper.updateMsgMediaStatusReturn(-7, rowId);

            if (ok != -1) {
                try {
                    String[] result = SiliCompressor.with(getApplicationContext()).compressVideo(Uri.parse(videoPath), destinationPath, videoNameWOExtension);
                    if (MediaController.forceStop) {
                        MediaController.forceStop = false;
                        if (databaseHelper.checkExistingOfUmcompressedVideo()) {
                            startNewThread();
                        } else {
                            stopSelf(startID);
                        }
                        return;
                    }
                    if (result[1].equals("not ok")) {

                    } else if (result[1].equals("ok")) {
                        videoPath = GlobalVariables.VIDEO_SENT_PATH + "/" + videoNameWOExtension + ".mp4";
                        String path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + videoNameWOExtension + ".jpg";
                        String orient = "horizontal";
                        if (issender == 19) {
                            orient = "vertical";
                        }
                        reUploadVideo(rowId, getApplicationContext(), jidStr, videoPath, path, orient);
                    }
                } catch (URISyntaxException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    public void startNewThread(){
        String[] result = databaseHelper.getOneQueueingVideo();
        String videoPathX = result[0];
        String videoNameWOExtensionX = result[1];
        String uniqueIdX = result[2];
        String jidStrX = result[3];
        int issenderX = Integer.valueOf(result[4]);
        String rowId = result[5];
        VideoCompressThread v = new VideoCompressThread(rowId, videoPathX, destinationPath, videoNameWOExtensionX, startID, jidStrX, issenderX, uniqueIdX);
        v.start();
    }
}
