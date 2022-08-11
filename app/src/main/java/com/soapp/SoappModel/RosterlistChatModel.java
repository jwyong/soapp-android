package com.soapp.SoappModel;

/* Created by chang on 24/07/2017. */

public class RosterlistChatModel {

    private int admin;
    private int appointmentcounter;
    private int appointmentcurrent;
    private int appointmentnotification;
    private int batchcounter;
    private int blockboolean;
    private boolean leaveroom;
    private int setdate;
    private long appointmentdate;
    private long appointmenttime;
    private long date;
    private String allmessage;
    private String appointmentbubble;
    private String appointmenttitle;
    private String displayname;
    private String jid;
    private String messagestatus;
    private String otherrecipient;
    private String phonename;
    private String phonenumber;
    private String tracktype;
    private String type;
    private String typingname;
    private byte[] profilephoto;
    private String photourl;

    public RosterlistChatModel(String jid) {
        this.jid = jid;
    }

    public RosterlistChatModel(String displayname, String allmessage, long date, byte[] profilephoto, String jid,
                               String messagestatus, int counter, String phonenumber, String phonename) {
        this.displayname = displayname;
        this.allmessage = allmessage;
        this.date = date;
        this.profilephoto = profilephoto;
        this.jid = jid;
        this.messagestatus = messagestatus;
        this.batchcounter = counter;
        this.phonenumber = phonenumber;
        this.phonename = phonename;
    }

    public RosterlistChatModel(String displayname, String allmessage, long date, byte[] profilephoto, String jid,
                               String messagestatus, int counter, String otherrecipient, int admin,
                               String typingname, boolean leaveRoom) {
        this.displayname = displayname;
        this.allmessage = allmessage;
        this.date = date;
        this.profilephoto = profilephoto;
        this.jid = jid;
        this.messagestatus = messagestatus;
        this.batchcounter = counter;
        this.otherrecipient = otherrecipient;
        this.admin = admin;
        this.typingname = typingname;
        this.leaveroom = leaveRoom;

    }

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public int getAppointmentcounter() {
        return appointmentcounter;
    }

    public void setAppointmentcounter(int appointmentcounter) {
        this.appointmentcounter = appointmentcounter;
    }

    public int getAppointmentcurrent() {
        return appointmentcurrent;
    }

    public void setAppointmentcurrent(int appointmentcurrent) {
        this.appointmentcurrent = appointmentcurrent;
    }

    public int getAppointmentnotification() {
        return appointmentnotification;
    }

    public void setAppointmentnotification(int appointmentnotification) {
        this.appointmentnotification = appointmentnotification;
    }

    public int getBatchcounter() {
        return batchcounter;
    }

    public void setBatchcounter(int batchcounter) {
        this.batchcounter = batchcounter;
    }

    public int getBlockboolean() {
        return blockboolean;
    }

    public void setBlockboolean(int blockboolean) {
        this.blockboolean = blockboolean;
    }

    public boolean getLeaveroom() {
        return leaveroom;
    }

    public void setLeaveroom(boolean leaveroom) {
        this.leaveroom = leaveroom;
    }

    public int getSetdate() {
        return setdate;
    }

    public void setSetdate(int setdate) {
        this.setdate = setdate;
    }

    public long getAppointmentdate() {
        return appointmentdate;
    }

    public void setAppointmentdate(long appointmentdate) {
        this.appointmentdate = appointmentdate;
    }

    public long getAppointmenttime() {
        return appointmenttime;
    }

    public void setAppointmenttime(long appointmenttime) {
        this.appointmenttime = appointmenttime;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAllmessage() {
        return allmessage;
    }

    public void setAllmessage(String allmessage) {
        this.allmessage = allmessage;
    }

    public String getAppointmentbubble() {
        return appointmentbubble;
    }

    public void setAppointmentbubble(String appointmentbubble) {
        this.appointmentbubble = appointmentbubble;
    }

    public String getAppointmenttitle() {
        return appointmenttitle;
    }

    public void setAppointmenttitle(String appointmenttitle) {
        this.appointmenttitle = appointmenttitle;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getMessagestatus() {
        return messagestatus;
    }

    public void setMessagestatus(String messagestatus) {
        this.messagestatus = messagestatus;
    }

    public String getOtherrecipient() {
        return otherrecipient;
    }

    public void setOtherrecipient(String otherrecipient) {
        this.otherrecipient = otherrecipient;
    }

    public String getPhonename() {
        return phonename;
    }

    public void setPhonename(String phonename) {
        this.phonename = phonename;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getTracktype() {
        return tracktype;
    }

    public void setTracktype(String tracktype) {
        this.tracktype = tracktype;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypingname() {
        return typingname;
    }

    public void setTypingname(String typingname) {
        this.typingname = typingname;
    }

    public byte[] getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(byte[] profilephoto) {
        this.profilephoto = profilephoto;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}

