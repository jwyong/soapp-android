package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MEDIAWORKER")
public class MediaWorker {

    @PrimaryKey
    @NonNull
    private String MediaWorkerMessageRowID;

    private String MediaWorkerWorkerEnqueueID;

    public MediaWorker(){}

    public static MediaWorker fromContentValues(ContentValues values) {
        final MediaWorker mediaWorker = new MediaWorker();

        if (values.containsKey("MediaWorkerMessageRowID")) {
            mediaWorker.MediaWorkerMessageRowID = values.getAsString("MediaWorkerMessageRowID");
        }

        if (values.containsKey("MediaWorkerWorkerEnqueueID")) {
            mediaWorker.MediaWorkerWorkerEnqueueID = values.getAsString("MediaWorkerWorkerEnqueueID");
        }

        return mediaWorker;
    }

    public String getMediaWorkerMessageRowID() { return MediaWorkerMessageRowID; }

    public void setMediaWorkerMessageRowID(String mediaWorkerMessageRowID) { MediaWorkerMessageRowID = mediaWorkerMessageRowID; }

    public String getMediaWorkerWorkerEnqueueID() { return MediaWorkerWorkerEnqueueID; }

    public void setMediaWorkerWorkerEnqueueID(String mediaWorkerWorkerEnqueueID) { MediaWorkerWorkerEnqueueID = mediaWorkerWorkerEnqueueID; }

}
