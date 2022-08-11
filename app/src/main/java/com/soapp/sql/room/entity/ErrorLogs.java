package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class ErrorLogs {

    @PrimaryKey(autoGenerate = true)
    private Integer errorID;

    private String errorTitle;
    private String errorDesc;
    private String createdTimeStamp;

    public ErrorLogs() {
    }

    @Ignore
    public ErrorLogs(String errorTitle, String errorDesc, String createdTimeStamp) {
        this.errorTitle = errorTitle;
        this.errorDesc = errorDesc;
        this.createdTimeStamp = createdTimeStamp;
    }

    public static ErrorLogs fromContentValues(ContentValues values) {

        final ErrorLogs logs = new ErrorLogs();

        if (values.containsKey("errorTitle")) {
            logs.errorTitle = values.getAsString("errorTitle");
        }
        if (values.containsKey("errorDesc")) {
            logs.errorDesc = values.getAsString("errorDesc");
        }
        if (values.containsKey("createdTimeStamp")) {
            logs.createdTimeStamp = values.getAsString("createdTimeStamp");
        }

        return logs;
    }

    public Integer getErrorID() {
        return errorID;
    }

    public void setErrorID(Integer errorID) {
        this.errorID = errorID;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }
}
