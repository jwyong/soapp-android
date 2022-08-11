package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 21/03/2018. */

@Entity(tableName = "APPOINTMENT_HIST")
public class AppointmentHist implements ApptHistInterface {
    @PrimaryKey(autoGenerate = true)
    private Integer ApptH_Row;

    //new apptID = apptJID + currentTime long
    private String ApptH_ID;

    private String ApptH_Jid;

    private String ApptH_Title;

    private Long ApptH_Date = 0L;

    private Long ApptH_Time = 0L;

    private String ApptH_Location;

    private String ApptH_Latitude;

    private String ApptH_Longitude;

    private String ApptH_MapURL;

    //appt additional details/description
    private String ApptH_Desc;

    //Sorting: Hosting > Going > Invited/Undecided > Not Going
    //0 = Hosting (not the same as admin)
    //1 = Going
    //2 = Invited/Undecided
    //3 = Not Going
    //outgoing default = hosting (0)
    private Integer ApptH_Self_Status = 0;

    //outgoing default = invited (2)
    private Integer ApptH_Friend_Status = 2;

    private String ApptH_ResID;

    private String ApptH_ResImgURL;

    //column to determine if appt was deleted or just expired (by which jid)
    private String ApptH_isDeleted;

    //for room to initialise DB
    public AppointmentHist() {
    }

    //for our manual use (list/etc)
    @Ignore
    public AppointmentHist(String jid, String apptTitle, long apptDate, long apptTime, String apptLocation,
                           String apptLatitude, String apptLongitude, String apptMapURL, Integer friend_Status,
                           String apptResID, Integer self_Status, String apptResImgURL, String apptID,
                           String apptDesc, String isDeleted) {
        ApptH_Jid = jid;
        ApptH_ID = apptID;
        ApptH_Title = apptTitle;
        ApptH_Date = apptDate;
        ApptH_Time = apptTime;
        ApptH_Location = apptLocation;
        ApptH_Latitude = apptLatitude;
        ApptH_Longitude = apptLongitude;
        ApptH_MapURL = apptMapURL;
        ApptH_Desc = apptDesc;
        ApptH_Friend_Status = friend_Status;
        ApptH_ResID = apptResID;
        ApptH_Self_Status = self_Status;
        ApptH_ResImgURL = apptResImgURL;
        ApptH_isDeleted = isDeleted;
    }

    public static AppointmentHist fromContentValues(ContentValues values) {
        final AppointmentHist appointment = new AppointmentHist();

        if (values.containsKey("ApptH_Row")) {
            appointment.ApptH_Row = values.getAsInteger("ApptH_Row");
        }
        if (values.containsKey("ApptH_Jid")) {
            appointment.ApptH_Jid = values.getAsString("ApptH_Jid");
        }
        if (values.containsKey("ApptH_Title")) {
            appointment.ApptH_Title = values.getAsString("ApptH_Title");
        }
        if (values.containsKey("ApptH_Date")) {
            appointment.ApptH_Date = values.getAsLong("ApptH_Date");
        }
        if (values.containsKey("ApptH_Time")) {
            appointment.ApptH_Time = values.getAsLong("ApptH_Time");
        }
        if (values.containsKey("ApptH_Location")) {
            appointment.ApptH_Location = values.getAsString("ApptH_Location");
        }
        if (values.containsKey("ApptH_Latitude")) {
            appointment.ApptH_Latitude = values.getAsString("ApptH_Latitude");
        }
        if (values.containsKey("ApptH_Longitude")) {
            appointment.ApptH_Longitude = values.getAsString("ApptH_Longitude");
        }
        if (values.containsKey("ApptH_MapURL")) {
            appointment.ApptH_MapURL = values.getAsString("ApptH_MapURL");
        }
        if (values.containsKey("ApptH_Friend_Status")) {
            appointment.ApptH_Friend_Status = values.getAsInteger("ApptH_Friend_Status");
        }
        if (values.containsKey("ApptH_ResID")) {
            appointment.ApptH_ResID = values.getAsString("ApptH_ResID");
        }
        if (values.containsKey("ApptH_Self_Status")) {
            appointment.ApptH_Self_Status = values.getAsInteger("ApptH_Self_Status");
        }
        if (values.containsKey("ApptH_ResImgURL")) {
            appointment.ApptH_ResImgURL = values.getAsString("ApptH_ResImgURL");
        }
        if (values.containsKey("ApptH_ID")) {
            appointment.ApptH_ID = values.getAsString("ApptH_ID");
        }
        if (values.containsKey("ApptH_Desc")) {
            appointment.ApptH_Desc = values.getAsString("ApptH_Desc");
        }
        if (values.containsKey("ApptH_isDeleted")) {
            appointment.ApptH_isDeleted = values.getAsString("ApptH_isDeleted");
        }

        return appointment;
    }

    public Integer getApptH_Row() {
        return ApptH_Row;
    }

    public void setApptH_Row(Integer apptH_Row) {
        ApptH_Row = apptH_Row;
    }

    public String getApptH_ID() {
        return ApptH_ID;
    }

    public void setApptH_ID(String apptH_ID) {
        ApptH_ID = apptH_ID;
    }

    public String getApptH_Jid() {
        return ApptH_Jid;
    }

    public void setApptH_Jid(String apptH_Jid) {
        ApptH_Jid = apptH_Jid;
    }

    public String getApptH_Title() {
        return ApptH_Title;
    }

    public void setApptH_Title(String apptH_Title) {
        ApptH_Title = apptH_Title;
    }

    public Long getApptH_Date() {
        return ApptH_Date;
    }

    public void setApptH_Date(Long apptH_Date) {
        ApptH_Date = apptH_Date;
    }

    public Long getApptH_Time() {
        return ApptH_Time;
    }

    public void setApptH_Time(Long apptH_Time) {
        ApptH_Time = apptH_Time;
    }

    public String getApptH_Location() {
        return ApptH_Location;
    }

    public void setApptH_Location(String apptH_Location) {
        ApptH_Location = apptH_Location;
    }

    public String getApptH_Latitude() {
        return ApptH_Latitude;
    }

    public void setApptH_Latitude(String apptH_Latitude) {
        ApptH_Latitude = apptH_Latitude;
    }

    public String getApptH_Longitude() {
        return ApptH_Longitude;
    }

    public void setApptH_Longitude(String apptH_Longitude) {
        ApptH_Longitude = apptH_Longitude;
    }

    public String getApptH_MapURL() {
        return ApptH_MapURL;
    }

    public void setApptH_MapURL(String apptH_MapURL) {
        ApptH_MapURL = apptH_MapURL;
    }

    public String getApptH_Desc() {
        return ApptH_Desc;
    }

    public void setApptH_Desc(String apptH_Desc) {
        ApptH_Desc = apptH_Desc;
    }

    public Integer getApptH_Self_Status() {
        return ApptH_Self_Status;
    }

    public void setApptH_Self_Status(Integer apptH_Self_Status) {
        ApptH_Self_Status = apptH_Self_Status;
    }

    public Integer getApptH_Friend_Status() {
        return ApptH_Friend_Status;
    }

    public void setApptH_Friend_Status(Integer apptH_Friend_Status) {
        ApptH_Friend_Status = apptH_Friend_Status;
    }

    public String getApptH_ResID() {
        return ApptH_ResID;
    }

    public void setApptH_ResID(String apptH_ResID) {
        ApptH_ResID = apptH_ResID;
    }

    public String getApptH_ResImgURL() {
        return ApptH_ResImgURL;
    }

    public void setApptH_ResImgURL(String apptH_ResImgURL) {
        ApptH_ResImgURL = apptH_ResImgURL;
    }

    public String getApptH_isDeleted() {
        return ApptH_isDeleted;
    }

    public void setApptH_isDeleted(String apptH_isDeleted) {
        ApptH_isDeleted = apptH_isDeleted;
    }
}
