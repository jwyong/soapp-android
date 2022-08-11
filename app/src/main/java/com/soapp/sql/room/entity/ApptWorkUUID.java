package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApptWorkUUID {

    @PrimaryKey(autoGenerate = true)
    private Integer ApptWorkRow;

//    @ForeignKey(entity = Appointment.class, parentColumns = "ApptID", childColumns = "ApptID" , onUpdate = ForeignKey.SET_DEFAULT)
    private String ApptID;

    private String reminderUUID;
    private String exactUUID;
    private String deleteUUID;


    public ApptWorkUUID() {
    }

    public ApptWorkUUID(String apptID, String reminderUUID, String exactUUID, String deleteUUID) {
        ApptID = apptID;
        this.reminderUUID = reminderUUID;
        this.exactUUID = exactUUID;
        this.deleteUUID = deleteUUID;
    }

    public static ApptWorkUUID fromContentValues(ContentValues values) {
        final ApptWorkUUID workUUID = new ApptWorkUUID();

        if (values.containsKey("ApptID")) {
            workUUID.ApptID = values.getAsString("ApptID");
        }
        if (values.containsKey("reminderUUID")) {
            workUUID.reminderUUID = values.getAsString("reminderUUID");
        }
        if (values.containsKey("exactUUID")) {
            workUUID.exactUUID = values.getAsString("exactUUID");
        }
        if (values.containsKey("deleteUUID")) {
            workUUID.deleteUUID = values.getAsString("deleteUUID");
        }
        return workUUID;
    }


    public Integer getApptWorkRow() {
        return ApptWorkRow;
    }

    public void setApptWorkRow(Integer apptWorkRow) {
        ApptWorkRow = apptWorkRow;
    }

    public String getApptID() {
        return ApptID;
    }

    public void setApptID(String apptID) {
        ApptID = apptID;
    }

    public String getReminderUUID() {
        return reminderUUID;
    }

    public void setReminderUUID(String reminderUUID) {
        this.reminderUUID = reminderUUID;
    }

    public String getExactUUID() {
        return exactUUID;
    }

    public void setExactUUID(String exactUUID) {
        this.exactUUID = exactUUID;
    }

    public String getDeleteUUID() {
        return deleteUUID;
    }

    public void setDeleteUUID(String deleteUUID) {
        this.deleteUUID = deleteUUID;
    }

    @Override
    public String toString() {
        return String.format("ApptWorkUUID() called with: apptID = [%s], reminderUUID = [%s], exactUUID = [%s], deleteUUID = [%s]", ApptID, reminderUUID, exactUUID, deleteUUID);
    }
}


