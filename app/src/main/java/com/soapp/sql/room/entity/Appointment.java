package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 21/03/2018. */

@Entity(tableName = "APPOINTMENT")
public class Appointment implements ApptInterface {
    @PrimaryKey(autoGenerate = true)
    private Integer ApptRow;

    //new apptID = apptJID + currentTime long
    private String ApptID;

    private String ApptJid;

    //int to indicate expiry
    private Integer ApptExpiring = 0;

    private String ApptTitle;

    private Long ApptDate = 0L;

    private Long ApptTime = 0L;

    //reminder period (how many milis before)
    private Long ApptReminderTime = -1L;

    private Integer ApptNotiBadge = 0;

    private String ApptLocation;

    private String ApptLatitude;

    private String ApptLongitude;

    private String ApptMapURL;

    //appt additional details/description
    private String ApptDesc;

    //notification badges
    private Integer ApptNotiBadgeTitle = 0;
    private Integer ApptNotiBadgeDate = 0;
    private Integer ApptNotiBadgeLoc = 0;
    private Integer ApptNotiBadgeDesc = 0;

    //Sorting: Hosting > Going > Invited/Undecided > Not Going
    //0 = Hosting (not the same as admin)
    //1 = Going
    //2 = Invited/Undecided
    //3 = Not Going
    //outgoing default = hosting (0)
    private Integer Self_Status = 0;

    //outgoing default = invited (2)
    private Integer Friend_Status = 2;

    private String ApptResID;

    private String ApptResImgURL;

    //for room to initialise DB
    public Appointment() {
    }

    //for our manual use (list/etc)
    @Ignore
    public Appointment(String jid, String apptTitle, long apptDate, long apptTime, String apptLocation,
                       String apptLatitude, String apptLongitude, String apptMapURL, Integer friend_Status,
                       String apptResID, Integer self_Status, String apptResImgURL, Integer apptNotiBadge, String apptID,
                       int apptNotiBadgeTitle, int apptNotiBadgeDate, int apptNotiBadgeLoc, int apptNotiBadgeDesc,
                       int apptExpiring, String apptDesc, long apptReminderTime) {
        ApptJid = jid;
        ApptID = apptID;

        //expiration
        ApptExpiring = apptExpiring;

        ApptTitle = apptTitle;
        ApptDate = apptDate;
        ApptTime = apptTime;

        //reminder
        ApptReminderTime = apptReminderTime;

        ApptLocation = apptLocation;
        ApptLatitude = apptLatitude;
        ApptLongitude = apptLongitude;
        ApptMapURL = apptMapURL;
        ApptDesc = apptDesc;
        Friend_Status = friend_Status;
        ApptResID = apptResID;
        Self_Status = self_Status;
        ApptResImgURL = apptResImgURL;

        //overall appt noti badge (for title, date/time, loc) - value = 0 and 1
        ApptNotiBadge = apptNotiBadge;

        //columns for appt notifications
        ApptNotiBadgeTitle = apptNotiBadgeTitle;
        ApptNotiBadgeDate = apptNotiBadgeDate;
        ApptNotiBadgeLoc = apptNotiBadgeLoc;
        ApptNotiBadgeDesc = apptNotiBadgeDesc;
    }

    public static Appointment fromContentValues(ContentValues values) {
        final Appointment appointment = new Appointment();

        if (values.containsKey("ApptRow")) {
            appointment.ApptRow = values.getAsInteger("ApptRow");
        }
        if (values.containsKey("ApptJid")) {
            appointment.ApptJid = values.getAsString("ApptJid");
        }
        if (values.containsKey("ApptTitle")) {
            appointment.ApptTitle = values.getAsString("ApptTitle");
        }
        if (values.containsKey("ApptDate")) {
            appointment.ApptDate = values.getAsLong("ApptDate");
        }
        if (values.containsKey("ApptTime")) {
            appointment.ApptTime = values.getAsLong("ApptTime");
        }
        if (values.containsKey("ApptLocation")) {
            appointment.ApptLocation = values.getAsString("ApptLocation");
        }
        if (values.containsKey("ApptLatitude")) {
            appointment.ApptLatitude = values.getAsString("ApptLatitude");
        }
        if (values.containsKey("ApptLongitude")) {
            appointment.ApptLongitude = values.getAsString("ApptLongitude");
        }
        if (values.containsKey("ApptMapURL")) {
            appointment.ApptMapURL = values.getAsString("ApptMapURL");
        }
        if (values.containsKey("Friend_Status")) {
            appointment.Friend_Status = values.getAsInteger("Friend_Status");
        }
        if (values.containsKey("ApptResID")) {
            appointment.ApptResID = values.getAsString("ApptResID");
        }
        if (values.containsKey("Self_Status")) {
            appointment.Self_Status = values.getAsInteger("Self_Status");
        }
        if (values.containsKey("ApptResImgURL")) {
            appointment.ApptResImgURL = values.getAsString("ApptResImgURL");
        }

        //20180406
        if (values.containsKey("ApptNotiBadge")) {
            appointment.ApptNotiBadge = values.getAsInteger("ApptNotiBadge");
        }
        if (values.containsKey("ApptID")) {
            appointment.ApptID = values.getAsString("ApptID");
        }

        //20180717
        if (values.containsKey("ApptNotiBadgeTitle")) {
            appointment.ApptNotiBadgeTitle = values.getAsInteger("ApptNotiBadgeTitle");
        }
        if (values.containsKey("ApptNotiBadgeDate")) {
            appointment.ApptNotiBadgeDate = values.getAsInteger("ApptNotiBadgeDate");
        }
        if (values.containsKey("ApptNotiBadgeLoc")) {
            appointment.ApptNotiBadgeLoc = values.getAsInteger("ApptNotiBadgeLoc");
        }
        if (values.containsKey("ApptNotiBadgeDesc")) {
            appointment.ApptNotiBadgeDesc = values.getAsInteger("ApptNotiBadgeDesc");
        }
        if (values.containsKey("ApptExpiring")) {
            appointment.ApptExpiring = values.getAsInteger("ApptExpiring");
        }
        if (values.containsKey("ApptDesc")) {
            appointment.ApptDesc = values.getAsString("ApptDesc");
        }

        //20180726 reminder
        if (values.containsKey("ApptReminderTime")) {
            appointment.ApptReminderTime = values.getAsLong("ApptReminderTime");
        }

        return appointment;
    }

    public Integer getApptRow() {
        return ApptRow;
    }

    public void setApptRow(Integer apptRow) {
        ApptRow = apptRow;
    }

    public String getApptID() {
        return ApptID;
    }

    public void setApptID(String apptID) {
        ApptID = apptID;
    }

    public String getApptJid() {
        return ApptJid;
    }

    public void setApptJid(String apptJid) {
        ApptJid = apptJid;
    }

    public String getApptTitle() {
        return ApptTitle;
    }

    public void setApptTitle(String apptTitle) {
        ApptTitle = apptTitle;
    }

    public Long getApptDate() {
        return ApptDate;
    }

    public void setApptDate(Long apptDate) {
        ApptDate = apptDate;
    }

    public Long getApptTime() {
        return ApptTime;
    }

    public void setApptTime(Long apptTime) {
        ApptTime = apptTime;
    }

    public String getApptLocation() {
        return ApptLocation;
    }

    public void setApptLocation(String apptLocation) {
        ApptLocation = apptLocation;
    }

    public String getApptLatitude() {
        return ApptLatitude;
    }

    public void setApptLatitude(String apptLatitude) {
        ApptLatitude = apptLatitude;
    }

    public String getApptLongitude() {
        return ApptLongitude;
    }

    public void setApptLongitude(String apptLongitude) {
        ApptLongitude = apptLongitude;
    }

    public String getApptMapURL() {
        return ApptMapURL;
    }

    public void setApptMapURL(String apptMapURL) {
        ApptMapURL = apptMapURL;
    }

    public Integer getFriend_Status() {
        return Friend_Status;
    }

    public void setFriend_Status(Integer friend_Status) {
        Friend_Status = friend_Status;
    }

    public String getApptResID() {
        return ApptResID;
    }

    public void setApptResID(String apptResID) {
        ApptResID = apptResID;
    }

    public Integer getSelf_Status() {
        return Self_Status;
    }

    public void setSelf_Status(Integer self_Status) {
        Self_Status = self_Status;
    }

    public String getApptResImgURL() {
        return ApptResImgURL;
    }

    public void setApptResImgURL(String apptResImgURL) {
        ApptResImgURL = apptResImgURL;
    }

    public Integer getApptNotiBadge() {
        return ApptNotiBadge;
    }

    public void setApptNotiBadge(Integer apptNotiBadge) {
        ApptNotiBadge = apptNotiBadge;
    }

    public Integer getApptNotiBadgeTitle() {
        return ApptNotiBadgeTitle;
    }

    public void setApptNotiBadgeTitle(Integer apptNotiBadgeTitle) {
        ApptNotiBadgeTitle = apptNotiBadgeTitle;
    }

    public Integer getApptNotiBadgeDate() {
        return ApptNotiBadgeDate;
    }

    public void setApptNotiBadgeDate(Integer apptNotiBadgeDate) {
        ApptNotiBadgeDate = apptNotiBadgeDate;
    }

    public Integer getApptNotiBadgeLoc() {
        return ApptNotiBadgeLoc;
    }

    public void setApptNotiBadgeLoc(Integer apptNotiBadgeLoc) {
        ApptNotiBadgeLoc = apptNotiBadgeLoc;
    }


    public String getApptDesc() {
        return ApptDesc;
    }

    public void setApptDesc(String apptDesc) {
        ApptDesc = apptDesc;
    }

    public Integer getApptNotiBadgeDesc() {
        return ApptNotiBadgeDesc;
    }

    public void setApptNotiBadgeDesc(Integer apptNotiBadgeDesc) {
        ApptNotiBadgeDesc = apptNotiBadgeDesc;
    }

    public Integer getApptExpiring() {
        return ApptExpiring;
    }

    public void setApptExpiring(Integer apptExpiring) {
        ApptExpiring = apptExpiring;
    }

    @Override
    public Long getApptReminderTime() {
        return ApptReminderTime;
    }

    public void setApptReminderTime(Long apptReminderTime) {
        ApptReminderTime = apptReminderTime;
    }
}
