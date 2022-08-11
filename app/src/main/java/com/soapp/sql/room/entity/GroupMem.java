package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 19/03/2018. */

@Entity(tableName = "GROUPMEM")
public class GroupMem implements GroupMemInterface {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "GroupRow")
    private Integer GrpMem_Id;

    private String RoomJid;

    private String MemberJid;

    private Integer Admin = 0;

    private String TextColor;

    //see Appointment entity for integer details
    //outgoing default = invited (2)
    private Integer Appt_Status = 2;

    private String Latitude;

    private String Longitude;

    public GroupMem() {
    }

    @Ignore
    public GroupMem(Integer GrpMem_Id, String roomJid, String memberJid, Integer admin, String textColor, Integer appt_Status, String latitude, String longitude) {
        this.GrpMem_Id = GrpMem_Id;
        RoomJid = roomJid;
        MemberJid = memberJid;
        Admin = admin;
        TextColor = textColor;
        Appt_Status = appt_Status;
        Latitude = latitude;
        Longitude = longitude;
    }

    @Ignore
    public GroupMem(int GrpMem_Id) {
        this.GrpMem_Id = GrpMem_Id;
    }

    public static GroupMem fromContentValues(ContentValues values) {

        final GroupMem groupMem = new GroupMem();

        if (values.containsKey("GroupRow")) {
            groupMem.GrpMem_Id = values.getAsInteger("GroupRow");
        }
        if (values.containsKey("RoomJid")) {
            groupMem.RoomJid = values.getAsString("RoomJid");
        }
        if (values.containsKey("MemberJid")) {
            groupMem.MemberJid = values.getAsString("MemberJid");
        }
        if (values.containsKey("Admin")) {
            groupMem.Admin = values.getAsInteger("Admin");
        }
        if (values.containsKey("TextColor")) {
            groupMem.TextColor = values.getAsString("TextColor");
        }
        if (values.containsKey("Appt_Status")) {
            groupMem.Appt_Status = values.getAsInteger("Appt_Status");
        }
        if (values.containsKey("Latitude")) {
            groupMem.Latitude = values.getAsString("Latitude");
        }
        if (values.containsKey("Longitude")) {
            groupMem.Longitude = values.getAsString("Longitude");
        }
        return groupMem;
    }

    @Override
    public Integer getGrpMem_Id() {
        return GrpMem_Id;
    }

    public void setGrpMem_Id(Integer grpMem_Id) {
        this.GrpMem_Id = grpMem_Id;
    }

    @Override
    public String getRoomJid() {
        return RoomJid;
    }

    public void setRoomJid(String roomJid) {
        RoomJid = roomJid;
    }

    @Override
    public String getMemberJid() {
        return MemberJid;
    }

    public void setMemberJid(String memberJid) {
        MemberJid = memberJid;
    }

    @Override
    public Integer getAdmin() {
        return Admin;
    }

    public void setAdmin(Integer admin) {
        Admin = admin;
    }

    @Override
    public String getTextColor() {
        return TextColor;
    }

    public void setTextColor(String textColor) {
        TextColor = textColor;
    }

    @Override
    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    @Override
    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    @Override
    public Integer getAppt_Status() {
        return Appt_Status;
    }

    public void setAppt_Status(Integer appt_Status) {
        Appt_Status = appt_Status;
    }
}
