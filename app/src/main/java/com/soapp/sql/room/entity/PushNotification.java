package com.soapp.sql.room.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class PushNotification {

    @PrimaryKey(autoGenerate = true)
    private Integer pnRow;

    private String senderJid;
    private String groupJid;
    private String apptId;
    private String senderName;
    private String pushBody;
    private String pushType;
    private Integer isGroup = 0;



    public PushNotification() {
    }

    @Ignore
    public PushNotification(String senderJid, String groupJid, String senderName, String pushBody, String pushType, int isGroup , String apptId) {
        this.senderJid = senderJid;
        this.groupJid = groupJid;
        this.senderName = senderName;
        this.pushBody = pushBody;
        this.pushType = pushType;
        this.isGroup = isGroup;
        this.apptId = apptId;
    }

    public Integer getPnRow() {
        return pnRow;
    }

    public void setPnRow(Integer pnRow) {
        this.pnRow = pnRow;
    }

    public String getSenderJid() {
        return senderJid;
    }

    public void setSenderJid(String senderJid) {
        this.senderJid = senderJid;
    }

    public String getGroupJid() {
        return groupJid;
    }

    public void setGroupJid(String groupJid) {
        this.groupJid = groupJid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getPushBody() {
        return pushBody;
    }

    public void setPushBody(String pushBody) {
        this.pushBody = pushBody;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public Integer getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Integer isGroup) {
        this.isGroup = isGroup;
    }

    public String getApptId() {
        return apptId;
    }

    public void setApptId(String apptId) {
        this.apptId = apptId;
    }

    @Override
    public String toString() {
        return "PushNotification{" +
                "pnRow=" + pnRow +
                ", senderJid='" + senderJid + '\'' +
                ", groupJid='" + groupJid + '\'' +
                ", senderName='" + senderName + '\'' +
                ", pushBody='" + pushBody + '\'' +
                ", pushType='" + pushType + '\'' +
                ", isGroup=" + isGroup +
                '}';
    }
}
