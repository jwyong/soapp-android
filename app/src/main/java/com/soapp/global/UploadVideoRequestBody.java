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
 * Created by ryan on 12/01/2018.
 */

public class UploadVideoRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private File mFile;
    private String mPath;
    private UploadCallBack mListener;
    private String mUniqueid;

    public UploadVideoRequestBody(final File file, final UploadCallBack listener, final String uniqueId) {
        mFile = file;
        mListener = listener;
        mUniqueid = uniqueId;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("video/*");
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
            while ((read = in.read(buffer)) != -1) {
                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                sink.write(buffer, 0, read);
//                if (uploaded == fileLength) {
//                    ContentValues cv = new ContentValues();
//                    cv.put("MediaStatus", 100);
//                    new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_MESSAGE_URI, cv,
//                            "MsgRow = ?", new String[]{mUniqueid});
//                }
            }
        } finally {
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
            if (mTotal != 0) {
                mListener.onProgressUpdate((int) ((100l * mUploaded) / mTotal), mUniqueid);
            }

//            ContentValues cv = new ContentValues();
//            cv.put("status", (int)((100l * mUploaded) / mTotal));
//            new CursorLoaderProvider().update(CursorLoaderProvider.UPDATE_MESSAGE_URI, cv,
//                    "id = ?", new String[]{mUniqueid});
        }
    }
}
