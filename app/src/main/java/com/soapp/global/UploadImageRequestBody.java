package com.soapp.global;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by ryan on 18/01/2018.
 */

public class UploadImageRequestBody extends RequestBody {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private File mFile;
    private String mPath;
    private UploadImageRequestBody.UploadCallBack mListener;
    private String mRowId;

    public UploadImageRequestBody(final File file, final UploadImageRequestBody.UploadCallBack listener, final String row_id) {
        mFile = file;
        mListener = listener;
        mRowId = row_id;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
//            ContentValues cv = new ContentValues();
//            cv.put("MediaStatus", 1);
//            new DatabaseHelper().updateRDB1Col(DatabaseHelper.MSG_TABLE_NAME, cv,
//                    DatabaseHelper.MSG_ROW, mRowId);

            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new UploadImageRequestBody.ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
                if (uploaded == fileLength) {
//                    ContentValues cv = new ContentValues();
//                    cv.put("MediaStatus", 100);
//                    new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_MESSAGE_URI, cv,
//                            "MsgUniqueId = ?", new String[]{mUniqueid});
                }
            }
        }
        finally {
            in.close();
        }
    }

    public interface UploadCallBack {
        void onProgressUpdate(int percentage, String uniqueId);

        void onError();

        void onFinish(String uniqueId);
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long updated, long total) {
            mUploaded = updated;
            mTotal = total;
        }

        @Override
        public void run() {
//            Log.i("diao","progress: "+(int) ((100l * mUploaded) / mTotal));
//            mListener.onProgressUpdate((int) ((100l * mUploaded) / mTotal), mUniqueid);
//            ContentValues cv = new ContentValues();
//            cv.put("status", (int)((100l * mUploaded) / mTotal));
//            new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_MESSAGE_URI, cv,
//                    "id = ?", new String[]{mUniqueid});
        }
    }
}
