package com.soapp.WorkManager.media.upload.video;

import android.content.Context;
import android.content.Intent;

import com.iceteck.soappcompressor.videocompression.MediaController;
import com.soapp.camera.Util;
import com.soapp.global.ChatHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.io.File;

import androidx.work.Data;
import androidx.work.Worker;

public class CompressVideoWorker extends Worker{

    DatabaseHelper databaseHelper;
    public String destinationPath = GlobalVariables.VIDEO_SENT_PATH;
    Context context;
    MediaController mediaController;
    ChatHelper chatHelper;

    public CompressVideoWorker(){
        databaseHelper = DatabaseHelper.getInstance();
        context = Soapp.getInstance().getApplicationContext();
        mediaController = MediaController.getInstance();
        chatHelper = new ChatHelper();
        mediaController.setStopCompress(false);
    }

    @Override
    public Result doWork() {
        Data data = getInputData();
        String rowId = data.getString("row_id");
        String videoPath = data.getString("video_path");
        String videoNameWOExtension = data.getString("video_name_w_o_extension");
        String jidstr = data.getString("jid_str");
        int issender = data.getInt("issender",-1);
        String uniqueId = data.getString("unique_id");
        //group_display_name could be null if come from indi
        String group_display_name = data.getString("group_display_name");

        databaseHelper.updateMsgMediaStatusReturn(-7, rowId);

        try {
//            String[] result_1 = SiliCompressor.with(context).compressVideo(Uri.parse(videoPath), destinationPath, videoNameWOExtension);
            File videoFile = new File (videoPath);
            boolean converted = false;
            if(videoFile.length() > 1000000){
                converted = mediaController.convertVideo(videoPath, new File(destinationPath), 0, 0, 0, videoNameWOExtension, new MediaController.CompressProgressListener() {
                    @Override
                    public void onProgress(float percent) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("outgoingCompressVideo"+rowId);
                        broadcastIntent.putExtra("percent", percent);
                        context.sendBroadcast(broadcastIntent);
                    }
                });
            }else{
                converted = true;
            }

//            if (!result_1[1].isEmpty() && databaseHelper.checkExistingOfUmcompressedVideo()) {
            boolean isVideo = chatHelper.checkIsVideo(context, destinationPath+"/"+videoNameWOExtension+".mp4");

            if(databaseHelper.checkExistingOfUmcompressedVideo()){
                databaseHelper.updateMsgMediaStatusReturn(9, rowId);
                chatHelper.compressNextVideo();
            }
//            if(result_1[1].equals("ok")){
            if(converted){
                String video_path_1 = destinationPath + "/" + videoNameWOExtension + ".mp4";
                if(!isVideo){
                    File copiedFile = Util.ryanCopyFile(new File(videoPath), new File(video_path_1));
                    video_path_1 = videoPath;
                }
                String video_thumb_path = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + videoNameWOExtension + ".jpg";
                String orient = "horizontal";
                if (issender == 19) {
                    orient = "vertical";
                }
                chatHelper.startUploadVideoWorker(video_path_1, rowId, video_thumb_path, jidstr, group_display_name, orient, uniqueId);
                return Result.SUCCESS;
            }else{
                databaseHelper.updateMsgMediaStatusReturn(7, rowId);
                return Result.FAILURE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            databaseHelper.updateMsgMediaStatusReturn(7, rowId);
            return Result.FAILURE;
        }
    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
        if(mediaController != null) {
            if (!mediaController.isStopCompress()) {
                mediaController.setStopCompress(true);
            }
        }
    }
}

