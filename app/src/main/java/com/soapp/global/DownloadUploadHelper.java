package com.soapp.global;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;

import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

/**
 * Created by rlwt on 6/8/18.
 */

public class DownloadUploadHelper {

    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private final DecryptionHelper decryptionHelper = new DecryptionHelper();
    private Preferences preferences = Preferences.getInstance();
    //
    public boolean cancelDownloadManager(long downloadId, int rowId) {
        DownloadManager downloadManager = (DownloadManager) Soapp.getInstance().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        int ok = downloadManager.remove(downloadId);

        if (ok != -1) {
            ContentValues cv = new ContentValues();
            cv.put("MediaStatus", -3);

            databaseHelper.updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv, DatabaseHelper.MSG_ROW,
                    String.valueOf(rowId));

            return true;
        }
        return false;
    }

//name use resourceId, or use rowId, KIV [ryan]
//    public Long startDownloadManagerForMedia(Context context, String url, String name, String rowId, String type){
//        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//        switch (type) {
//            case "Audio":
//                request.setTitle(name)
//                        .setDescription("Downloading")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        .setDestinationInExternalPublicDir("/Soapp/SoappAudio", name);
//                break;
//            case "Video":
//                request.setTitle(name + ".mp4")
//                        .setDescription("Downloading")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        .setDestinationInExternalPublicDir("/Soapp/SoappVideo", name + ".mp4");
//                break;
//            case "Image":
//                request.setTitle(rowId + ".jpg")
//                        .setDescription("Downloading")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        .setDestinationInExternalPublicDir("/Soapp/SoappImages", rowId + ".jpg");
//                break;
//        }
//
//        final long enqueueId = downloadManager.enqueue(request);
//        DownloadManager.Query dlQuery = new DownloadManager.Query();
//        dlQuery.setFilterById(enqueueId);
////        ContentValues cvMSG = new ContentValues();
////        cvMSG.put("MsgUniqueId", enqueueId);
////
////        databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, rowId);
//
//        return enqueueId;
//    }

//    public class DownloaderAsyncTaskWithProgressBar extends AsyncTask<Void, Integer, Boolean> {
//
//        Context activity;
//        ProgressBar progressbar;
//        String url;
//        String name;
//        String rowId;
//        String type;
//        String enqueueId;
//        ImageView imageView;
//        TextView textView;
//
//        private Handler mHandler = new Handler();
//
//        public DownloaderAsyncTaskWithProgressBar(Context context, TextView textView, ImageView imageView, ProgressBar progressbar, String url, String name, String rowId, String type) {
//            this.activity = context;
//            this.progressbar = progressbar;
//            this.url = url;
//            this.name = name;
//            this.rowId = rowId;
//            this.type = type;
//            this.imageView = imageView;
//            this.textView = textView;
//        }
//
////        Context context, String url, String name, String rowId, String orient, String type
//        @Override
//        protected void onPreExecute() {
//            long enqueueId = startDownloadManagerForMedia(this.activity, this.url, this.name, this.rowId, this.type);
//            if(enqueueId > 0){
//                this.enqueueId = String.valueOf(enqueueId);
//            }
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//            this.textView.setText(values[0]+"");
//            this.mHandler.post(new Runnable(){
//                @Override
//                public void run() {
//                    progressbar.setProgress((values[0]/100));
//                }
//            });
//        }
//
//        @Override
//        protected void onPostExecute(Boolean finish) {
//            if(finish) {
//                this.progressbar.setVisibility(View.GONE);
//            }
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            this.imageView.setImageResource(R.drawable.ic_chatlog_cancel_grey_new);
//            boolean downloading = true;
//            while (downloading && !isCancelled()) {
////                Context context, final String enqueueId, final String rowId, final String type
//                String[] ok = checkDownloadProgress(this.activity, this.enqueueId, this.rowId, this.type);
//                publishProgress(Integer.valueOf(ok[1]));
//                if (ok[0].equals("false")) {
//                    if (this.type.equals("Image")) {
//                        File wrongnameImageFile = new File(GlobalVariables.IMAGES_PATH + this.name + ".jpg");
//
//                        // decrypt image
//                        final File GdecryptImgFile = decryptionHelper.decryptImg(wrongnameImageFile, this.name);
//
//                        String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.IMAGES_PATH, "IMG");
//                        File file = new File(GlobalVariables.IMAGES_PATH, fileName);
//                        boolean renamedCompleted = GdecryptImgFile.renameTo(file);
//                        if (renamedCompleted) {
//                            ContentValues cv = new ContentValues();
//                            cv.put(MSG_MSGINFOURL, fileName);
//
//                            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv, MSG_ROW,
//                                    this.name);
//
//                            this.activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
//
//                            return true;
//                        }
//                    } else if (this.type.equals("Video")) {
//                        File wrongnameVideoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + this.name + ".jpg");
//                        File wrongnameVideoFile = new File(GlobalVariables.VIDEO_PATH, this.name + ".mp4");
//
//                        // decrypt video
//                        final File GdecryptVideoFile = decryptionHelper.decryptVideo(wrongnameVideoFile, this.name);
//
//                        String fileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.VIDEO_THUMBNAIL_PATH + "/", "VID");
//                        String renamedFileName = fileName.substring(0, fileName.indexOf("."));
//
//                        File videoThumbnailFile = new File(GlobalVariables.VIDEO_THUMBNAIL_PATH, renamedFileName + ".jpg");
//                        File videoFile = new File(GlobalVariables.VIDEO_PATH, renamedFileName + ".mp4");
//
//                        boolean renamedThumbCompleted = wrongnameVideoThumbnailFile.renameTo(videoThumbnailFile);
//                        boolean renamedVideoCompleted = wrongnameVideoFile.renameTo(videoFile);
//                        if (renamedThumbCompleted && renamedVideoCompleted) {
//                            ContentValues cv = new ContentValues();
//                            cv.put(MSG_MSGINFOURL, renamedFileName);
//
//                            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv, MSG_ROW, this.rowId);
//
//                            this.activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                                    Uri.fromFile(videoFile)));
//
//                            return true;
//                        }
//                    } else if (this.type.equals("Audio")) {
//                        File wrongNameAudioFile = new File(GlobalVariables.AUDIO_PATH + this.name);
//
//                        // decrypt audio
//                        final File GdecryptaudioFile = decryptionHelper.decryptAudio(wrongNameAudioFile, this.name);
//
//                        String correctAudioFileName = ImageHelper.getFileNameForSavingMedia(GlobalVariables.AUDIO_PATH, "AUD");
//                        File correctFile = new File(GlobalVariables.AUDIO_PATH, correctAudioFileName);
//                        boolean renamedCompleted = GdecryptaudioFile.renameTo(correctFile);
//                        if (renamedCompleted) {
//                            ContentValues cv = new ContentValues();
//                            cv.put(MSG_MSGINFOURL, correctAudioFileName);
//
//                            databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cv, MSG_ROW,
//                                    this.name);
//                            return true;
//                        }
//                    }
//                    downloading = false;
//                }
//            }
//            return false;
//        }
//    }

//    public String[] checkDownloadProgress(Context context, final String enqueueId, final String rowId, final String type) {
//        String statusText = "";
//        String reasonText = "";
//        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        DownloadManager.Query dlQuery = new DownloadManager.Query();
//        dlQuery.setFilterById(Long.parseLong(enqueueId));
//        Cursor cursor = downloadManager.query(dlQuery);
//
//        if (cursor.moveToFirst()) {
//            int bytes_downloaded = cursor.getInt(cursor
//                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//            final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
//            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
//            int status = cursor.getInt(columnIndex);
//
//            switch (status) {
//                case DownloadManager.STATUS_SUCCESSFUL:
//                    statusText = "STATUS_SUCCESSFUL";
//                    break;
//            }
//
//            if (statusText.equals("STATUS_SUCCESSFUL")) {
//                cursor.close();
//                if (dl_progress == 100) {
//                    ContentValues cvMSG = new ContentValues();
//                    cvMSG.put("MediaStatus", dl_progress);
//
//                    databaseHelper.updateRDB1Col(MSG_TABLE_NAME, cvMSG, MSG_ROW, rowId);
//                }
//                return new String[]{"false", String.valueOf(dl_progress), statusText, reasonText};
//            } else {
//                cursor.close();
//                return new String[]{"true", String.valueOf(dl_progress), statusText, reasonText};
//            }
//        } else {
//            return new String[]{"true", "0", statusText, reasonText};
//        }
//    }
}
