package com.soapp.WorkManager.media.upload.image;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.soapp.SoappApi.ApiModel.FileModel;
import com.soapp.SoappApi.Interface.UploadChatImage;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.camera.Util;
import com.soapp.global.EncryptionHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UploadImageRequestBody;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;

public class UploadImageWorker extends Worker implements UploadImageRequestBody.UploadCallBack {
    Preferences preferences;
    EncryptionHelper encryptionHelper;
    SingleChatStanza singleChatStanza;
    DatabaseHelper databaseHelper;
    Call<FileModel> call;
    Context context;
    String rowId;

    //
    public UploadImageWorker() {
        preferences = Preferences.getInstance();
        encryptionHelper = new EncryptionHelper();
        singleChatStanza = new SingleChatStanza();
        databaseHelper = DatabaseHelper.getInstance();
        context = Soapp.getInstance().getApplicationContext();
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
        String group_display_name = data.getString("group_display_name");

        //
        rowId = row_id;
        Context itemView = Soapp.getInstance().getApplicationContext();
        //
        String worker_id = databaseHelper.rdb.selectQuery().getMediaWorkerId(row_id);

        if (worker_id == null) {
            onStopped(true);
            return Result.FAILURE;
        }
        //
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.MSG_MEDIASTATUS, 1);
        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv,
                DatabaseHelper.MSG_ROW, rowId);
        //
        final String self_username = preferences.getValue(itemView, GlobalVariables
                .STRPREF_USERNAME);
        String access_token = preferences.getValue(itemView, GlobalVariables.STRPREF_ACCESS_TOKEN);
        final File IndiReUploadfile = new File(path);
        final File reEncryptimg = encryptionHelper.encryptImage(IndiReUploadfile, null, path);
        final UploadImageRequestBody imageFileBody = new UploadImageRequestBody(reEncryptimg, this, unique_id);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "name.jpeg", imageFileBody);
        MultipartBody.Part type = MultipartBody.Part.createFormData("type", "photo");

        //build retrofit
        UploadChatImage client = RetrofitAPIClient.getClient().create(UploadChatImage.class);
        call = client.uploadfile("Bearer " + access_token,
                filePart, type);
        try {
            Response<FileModel> response = call.execute();
            if (!response.isSuccessful()) {

                ContentValues cv1 = new ContentValues();
                cv1.put(DatabaseHelper.MSG_MEDIASTATUS, -3);
                DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv1, DatabaseHelper.MSG_MSGUNIQUEID, unique_id);
                //
                databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
                //
                new MiscHelper().retroLogUnsuc(response, "uploadImage  ", "JAY");
                //
                return Result.FAILURE;
            } else {
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
                File copied_thumb_file = new File(Soapp.getInstance().getCacheDir() + rowId + ".jpg");
                try {
                    copied_thumb_file = Util.ryanCopyFile(IndiReUploadfile, copied_thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Bitmap img_thumb_bitmap = BitmapFactory.decodeFile(copied_thumb_file.getPath());

                Glide.with(context)
                        .asBitmap()
                        .load(copied_thumb_file.getPath())
                        .addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap img_thumb_bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                int img_thumb_bitmap_height = img_thumb_bitmap.getHeight();
                                int img_thumb_bitmap_width = img_thumb_bitmap.getWidth();

                                if (img_thumb_bitmap_width > img_thumb_bitmap_height) {
                                    img_thumb_bitmap = Bitmap.createBitmap(img_thumb_bitmap, img_thumb_bitmap.getWidth() / 2 - img_thumb_bitmap.getHeight() / 2, 0, img_thumb_bitmap.getHeight(), img_thumb_bitmap.getHeight());
                                } else {
                                    img_thumb_bitmap = Bitmap.createBitmap(img_thumb_bitmap, 0, img_thumb_bitmap.getHeight() / 2 - img_thumb_bitmap.getWidth() / 2, img_thumb_bitmap.getWidth(), img_thumb_bitmap.getWidth());
                                }

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                Bitmap.createScaledBitmap(img_thumb_bitmap, 240, 240, false).compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
                                byte[] byteArray = byteArrayOutputStream.toByteArray();
                                String image_thumb_base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                final String file_id = String.valueOf(response.body().getResource_id());
                                final String file_url = String.valueOf(response.body().getResource_url());

                                ContentValues cv2 = new ContentValues();
                                cv2.put(DatabaseHelper.MSG_MEDIASTATUS, 100);
                                cv2.put(DatabaseHelper.MSG_MSGLATITUDE, image_thumb_base64);

                                if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv2, DatabaseHelper.MSG_ROW, row_id);
                                    if (jid.length() == 12) {
                                        singleChatStanza.SoappImageStanza(file_id, jid, orient, file_url, self_username, "0", unique_id, image_thumb_base64);
                                    } else {
                                        String senderJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);
                                        new GroupChatStanza().GroupImage(jid, senderJid, self_username, orient, unique_id,
                                                file_id, file_url, group_display_name, "0", image_thumb_base64);
                                    }
                                } else {
                                    cv2.put(DatabaseHelper.MSG_MSGOFFLINE, 1);
                                    cv2.put(DatabaseHelper.MSG_CONTACTNAME, file_id);
                                    cv2.put(DatabaseHelper.MSG_CONTACTNUMBER, file_url);
                                    cv2.put(DatabaseHelper.MSG_MSGLONGITUDE, orient);
                                }
                                DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv2, DatabaseHelper.MSG_MSGUNIQUEID, unique_id);
                                return false;
                            }
                        })
                        .submit();
//                    }
//                });
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //
            ContentValues cv3 = new ContentValues();
            cv3.put(DatabaseHelper.MSG_MEDIASTATUS, -3);
            DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv3, DatabaseHelper.MSG_MSGUNIQUEID, unique_id);
            //
            databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME, DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID, row_id);
            //
            return Result.FAILURE;
        }
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

//    public void uploadImage(Context itemView, final String rowId, String imagePath, final String orient, final String jid, final String uniqueID) {
//        ContentValues cv = new ContentValues();
//        cv.put("MediaStatus", 1);
//        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv,
//                DatabaseHelper.MSG_ROW, rowId);
//        //
//        final String self_username = preferences.getValue(itemView, GlobalVariables
//                .STRPREF_USERNAME);
//
//        String access_token = preferences.getValue(itemView, GlobalVariables.STRPREF_ACCESS_TOKEN);
//        final File IndiReUploadfile = new File(imagePath);
//        final File reEncryptimg = encryptionHelper.encryptImage(IndiReUploadfile, null, imagePath);
//        final UploadImageRequestBody imageFileBody = new UploadImageRequestBody(reEncryptimg, this, uniqueID);
//        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "name.jpeg", imageFileBody);
//        MultipartBody.Part type = MultipartBody.Part.createFormData("type", "photo");
//
//        //build retrofit
//        UploadChatImage client = RetrofitAPIClient.getClient().create(UploadChatImage.class);
//        Call<FileModel> call = client.uploadfile("Bearer " + access_token,
//                filePart, type);
//        call.enqueue(new Callback<FileModel>() {
//            @Override
//            public void onResponse(Call<FileModel> call, Response<FileModel> response) {
//                if (!response.isSuccessful()) {
//                    ContentValues cv = new ContentValues();
//                    cv.put("MEDIASTATUS", -3);
//                    new MiscHelper().retroLogUnsuc(response, "uploadImage  ", "JAY");
//
//                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_MSGUNIQUEID, uniqueID);
//                } else {
//                    new Handler(Looper.getMainLooper())
//                            .post(new Runnable() {
//                                @Override
//                                public void run() {
//
//                    File copied_thumb_file = new File(Soapp.getInstance().getCacheDir() + rowId + ".jpg");
//                    try {
//                        copied_thumb_file = Util.ryanCopyFile(IndiReUploadfile, copied_thumb_file);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Bitmap img_thumb_bitmap = BitmapFactory.decodeFile(copied_thumb_file.getPath());
//
//                    int img_thumb_bitmap_height = img_thumb_bitmap.getHeight();
//                    int img_thumb_bitmap_width = img_thumb_bitmap.getWidth();
//                    Bitmap centered_img_thumb_bitmap = img_thumb_bitmap;
//                    if (img_thumb_bitmap_width > img_thumb_bitmap_height) {
//                        centered_img_thumb_bitmap = Bitmap.createBitmap(img_thumb_bitmap, img_thumb_bitmap.getWidth() / 2 - img_thumb_bitmap.getHeight() / 2, 0, img_thumb_bitmap.getHeight(), img_thumb_bitmap.getHeight());
//                    } else {
//                        centered_img_thumb_bitmap = Bitmap.createBitmap(img_thumb_bitmap, 0, img_thumb_bitmap.getHeight() / 2 - img_thumb_bitmap.getWidth() / 2, img_thumb_bitmap.getWidth(), img_thumb_bitmap.getWidth());
//                    }
//
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                    Bitmap.createScaledBitmap(centered_img_thumb_bitmap, 240, 240, false).compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream);
//                    byte[] byteArray = byteArrayOutputStream.toByteArray();
//                    String image_thumb_base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
//                    final String file_id = String.valueOf(response.body().getResource_id());
//                    final String file_url = String.valueOf(response.body().getResource_url());
//
//                    ContentValues cv = new ContentValues();
//                    cv.put("MediaStatus", 100);
//                    cv.put("MsgLatitude", image_thumb_base64);
//
//                    if (MyXMPP.connection != null && MyXMPP.connection.isAuthenticated()) {
//                        DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_MSGUNIQUEID, uniqueID);
//                        singleChatStanza.SoappImageStanza(file_id, jid, orient, file_url, self_username, "0", uniqueID, image_thumb_base64);
//                    } else {
//                        cv.put(DatabaseHelper.MSG_MSGOFFLINE, 1);
//                        cv.put(DatabaseHelper.MSG_CONTACTNAME, file_id);
//                        cv.put(DatabaseHelper.MSG_CONTACTNUMBER, file_url);
//                        cv.put(DatabaseHelper.MSG_MSGLONGITUDE,orient);
//                    }
//                    DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_MSGUNIQUEID, uniqueID);
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onFailure(Call<FileModel> call, Throwable t) {
//                ContentValues cv = new ContentValues();
//                cv.put("MEDIASTATUS", -3);
//                new MiscHelper().retroLogFailure(t, "uploadImage  ", "JAY");
//                DatabaseHelper.getInstance().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_MSGUNIQUEID, uniqueID);
//            }
//        });
//    }


    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
        if(call != null && rowId != null){
            call.cancel();
            databaseHelper.deleteRDB1Col(DatabaseHelper.MEDIA_WORKER_TABLE_NAME,DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID,rowId);
        }
    }
}
