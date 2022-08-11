package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 19/03/2018. */

@Entity(tableName = "GROUPMEM_STATUS")
public class GroupMem_Status implements GroupMemStatusInterface {
    @PrimaryKey(autoGenerate = true)
    private Integer GMSRow;

    private String GMSRoomJid;

    private String GMSMemberJid;

    private String GMSApptID;

    //GMSStatus:
    //0 = host
    //1 = going
    //2 = undecided
    //3 = not going
    private Integer GMSStatus = 2;

    public GroupMem_Status() {
    }

    @Ignore
    public GroupMem_Status(Integer GMSRow) {
        this.GMSRow = GMSRow;
    }

    @Ignore
    public GroupMem_Status(String GMSRoomJid, String GMSMemberJid) {
        this.GMSRoomJid = GMSRoomJid;
        this.GMSMemberJid = GMSMemberJid;
    }

    public static GroupMem_Status fromContentValues(ContentValues values) {

        final GroupMem_Status groupMem = new GroupMem_Status();

        if (values.containsKey("GMSRow")) {
            groupMem.GMSRow = values.getAsInteger("GMSRow");
        }
        if (values.containsKey("GMSRoomJid")) {
            groupMem.GMSRoomJid = values.getAsString("GMSRoomJid");
        }
        if (values.containsKey("GMSMemberJid")) {
            groupMem.GMSMemberJid = values.getAsString("GMSMemberJid");
        }
        if (values.containsKey("GMSApptID")) {
            groupMem.GMSApptID = values.getAsString("GMSApptID");
        }
        if (values.containsKey("GMSStatus")) {
            groupMem.GMSStatus = values.getAsInteger("GMSStatus");
        }

        return groupMem;
    }

    @Override
    public Integer getGMSRow() {
        return GMSRow;
    }

    public void setGMSRow(Integer GMSRow) {
        this.GMSRow = GMSRow;
    }

    public String getGMSRoomJid() {
        return GMSRoomJid;
    }

    public void setGMSRoomJid(String GMSRoomJid) {
        this.GMSRoomJid = GMSRoomJid;
    }

    public String getGMSMemberJid() {
        return GMSMemberJid;
    }

    public void setGMSMemberJid(String GMSMemberJid) {
        this.GMSMemberJid = GMSMemberJid;
    }

    public String getGMSApptID() {
        return GMSApptID;
    }

    public void setGMSApptID(String GMSApptID) {
        this.GMSApptID = GMSApptID;
    }

    public Integer getGMSStatus() {
        return GMSStatus;
    }

    public void setGMSStatus(Integer GMSStatus) {
        this.GMSStatus = GMSStatus;
    }
}
