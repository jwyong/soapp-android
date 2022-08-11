package com.soapp.sql.room.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;

import com.soapp.sql.DatabaseHelper;

@Entity
public class MediaWorker {

    @PrimaryKey
    private int MediaWorkerMessageRowID;

    private String MediaWorkerWorkerEnqueueID;

    public MediaWorker(){}

    public static MediaWorker fromContentValues(ContentValues values) {
        final MediaWorker mediaWorker = new MediaWorker();

        if (values.containsKey(DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID)) {
            mediaWorker.MediaWorkerMessageRowID = values.getAsInteger(DatabaseHelper.MEDIA_WORKER_MESSAGE_ROW_ID);
        }

        if (values.containsKey(DatabaseHelper.MEDIA_WORKER_WORKER_ENQUEUE_ID)) {
            mediaWorker.MediaWorkerWorkerEnqueueID = values.getAsString(DatabaseHelper.MEDIA_WORKER_WORKER_ENQUEUE_ID);
        }

        return mediaWorker;
    }

    public int getMediaWorkerMessageRowID() { return MediaWorkerMessageRowID; }

    public void setMediaWorkerMessageRowID(int mediaWorkerMessageRowID) { MediaWorkerMessageRowID = mediaWorkerMessageRowID; }

    public String getMediaWorkerWorkerEnqueueID() { return MediaWorkerWorkerEnqueueID; }

    public void setMediaWorkerWorkerEnqueueID(String mediaWorkerWorkerEnqueueID) { MediaWorkerWorkerEnqueueID = mediaWorkerWorkerEnqueueID; }

}
