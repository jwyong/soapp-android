package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 25/03/2018. */

@Entity(tableName = "MESSAGE")
public class Message implements MessageInterface {

    @PrimaryKey(autoGenerate = true)
    private Integer MsgRow;
    private String MsgJid;
    private String SenderJid;
    private Integer ChatMarker = 0;
    private Integer IsSender = 0;
    private Long MsgDate = 0L;
    private String MsgUniqueId;
    private String MsgData = "";
    private Integer MsgOffline = 0;

    //media: download enqueue ID (ALL media downloads)
    private Integer contact = 0;

    //media: download status
    private Integer MediaStatus = 0;

    //reply: uniqueID of replied msg
    //appt update msg: apptID
    //media - depends on outgoing or incoming
    private String MsgInfoId;

    //appt update msg: old appt details (title, loc, time in long)
    //media: depends on out/in
    private String MsgInfoUrl;

    //media: img/vid thumbnail
    private String MsgLatitude;
    private String MsgLongitude;

    //offline: resourceID
    private String contactname;

    //offline: resourceURL
    private String contactnumber;

    public Message() {
    }

    @Ignore
    public Message(Integer msgRow, String msgJid, String senderJid, Integer chatMarker, Integer isSender,
                   Long msgDate, String msgUniqueId, String msgData, String msgInfoId, String msgInfoUrl,
                   String msgLatitude, String msgLongitude, Integer mediaStatus, int msgOffline, Integer contact,
                   String contactname, String contactnumber) {
        MsgRow = msgRow;
        MsgJid = msgJid;
        SenderJid = senderJid;
        ChatMarker = chatMarker;
        IsSender = isSender;
        MsgDate = msgDate;
        MsgUniqueId = msgUniqueId;
        MsgData = msgData;
        MsgInfoId = msgInfoId;
        MsgInfoUrl = msgInfoUrl;
        MsgLatitude = msgLatitude;
        MsgLongitude = msgLongitude;
        MediaStatus = mediaStatus;
        MsgOffline = msgOffline;
        this.contact = contact;
        this.contactname = contactname;
        this.contactnumber = contactnumber;
    }

    @Ignore
    public Message(Integer IsSender, String MsgJid) {
        this.IsSender = IsSender;
        this.MsgJid = MsgJid;
    }

    @Ignore
    public Message(String media) {
        this.MsgInfoUrl = media;
    }

    public static Message fromContentValues(ContentValues values) {

        final Message message = new Message();

        if (values.containsKey("ChatMarker")) {
            message.ChatMarker = values.getAsInteger("ChatMarker");
        }
        if (values.containsKey("contact")) {
            message.contact = values.getAsInteger("contact");
        }
        if (values.containsKey("IsSender")) {
            message.IsSender = values.getAsInteger("IsSender");
        }

        if (values.containsKey("MsgDate")) {
            message.MsgDate = values.getAsLong("MsgDate");
        }
        if (values.containsKey("contactname")) {
            message.contactname = values.getAsString("contactname");
        }
        if (values.containsKey("contactnumber")) {
            message.contactnumber = values.getAsString("contactnumber");
        }
        if (values.containsKey("MsgUniqueId")) {
            message.MsgUniqueId = values.getAsString("MsgUniqueId");
        }
        if (values.containsKey("MsgInfoId")) {
            message.MsgInfoId = values.getAsString("MsgInfoId");
        }
        if (values.containsKey("MsgJid")) {
            message.MsgJid = values.getAsString("MsgJid");
        }
        if (values.containsKey("MsgLatitude")) {
            message.MsgLatitude = values.getAsString("MsgLatitude");
        }
        if (values.containsKey("MsgLongitude")) {
            message.MsgLongitude = values.getAsString("MsgLongitude");
        }
        if (values.containsKey("SenderJid")) {
            message.SenderJid = values.getAsString("SenderJid");
        }
        if (values.containsKey("MsgData")) {
            message.MsgData = values.getAsString("MsgData");
        }
        if (values.containsKey("MediaStatus")) {
            message.MediaStatus = values.getAsInteger("MediaStatus");
        }
        if (values.containsKey("MsgInfoUrl")) {
            message.MsgInfoUrl = values.getAsString("MsgInfoUrl");
        }
        if (values.containsKey("MsgOffline")) {
            message.MsgOffline = values.getAsInteger("MsgOffline");
        }
        return message;
    }

    public Integer getMsgRow() {
        return MsgRow;
    }

    public void setMsgRow(Integer msgRow) {
        this.MsgRow = msgRow;
    }

    public Integer getChatMarker() {
        return ChatMarker;
    }

    public void setChatMarker(Integer chatMarker) {
        this.ChatMarker = chatMarker;
    }

    public Integer getContact() { return contact; }

    public void setContact(Integer contact) {
        this.contact = contact;
    }

    public Integer getIsSender() {
        return IsSender;
    }

    public void setIsSender(Integer isSender) {
        this.IsSender = isSender;
    }

    public Long getMsgDate() {
        return MsgDate;
    }

    public void setMsgDate(Long msgDate) {
        this.MsgDate = msgDate;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getContactnumber() {
        return contactnumber;
    }

    public void setContactnumber(String contactnumber) {
        this.contactnumber = contactnumber;
    }


    public String getMsgUniqueId() {
        return MsgUniqueId;
    }

    public void setMsgUniqueId(String msgUniqueId) {
        this.MsgUniqueId = msgUniqueId;
    }

    public String getMsgInfoId() {
        return MsgInfoId;
    }

    public void setMsgInfoId(String msgInfoId) {
        this.MsgInfoId = msgInfoId;
    }

    public String getMsgJid() {
        return MsgJid;
    }

    public void setMsgJid(String msgJid) {
        this.MsgJid = msgJid;
    }

    public String getMsgLatitude() {
        return MsgLatitude;
    }

    public void setMsgLatitude(String msgLatitude) {
        this.MsgLatitude = msgLatitude;
    }

    public String getMsgLongitude() {
        return MsgLongitude;
    }

    public void setMsgLongitude(String msgLongitude) {
        this.MsgLongitude = msgLongitude;
    }

    public String getSenderJid() {
        return SenderJid;
    }

    public void setSenderJid(String senderJid) {
        this.SenderJid = senderJid;
    }

    public String getMsgData() {
        return MsgData;
    }

    public void setMsgData(String msgData) {
        this.MsgData = msgData;
    }

    public Integer getMediaStatus() {
        return MediaStatus;
    }

    public void setMediaStatus(Integer mediaStatus) {
        this.MediaStatus = mediaStatus;
    }

    public String getMsgInfoUrl() {
        return MsgInfoUrl;
    }

    public void setMsgInfoUrl(String msgInfoUrl) {
        MsgInfoUrl = msgInfoUrl;
    }

    public Integer getMsgOffline() {
        return MsgOffline;
    }

    public void setMsgOffline(Integer msgOffline) {
        MsgOffline = msgOffline;
    }
}
