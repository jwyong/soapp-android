package com.soapp.SoappModel;

/* Created by chang on 28/07/2017. */

public class RosterlistScheduleModel {

    private int admin; //grp admin
    private int appointmentcounter; //redundant (last time for noti badge counter
    private int appointmentnotification; //notification badge counter
    private int batchcounter; //chat notification badge
    private int blockboolean; //block other user
    private int leaveroom; //self has left room or not
    private int setdate; //self got set date/time or not
    private long appointmentdate;
    private long appointmenttime;
    private long date; //chat date (when updated)
    private String allmessage; //display msg
    private String appointmentbubble; //"Going" "Not Going"...
    private String appointmenttitle;
    private String displayname;
    private String jid;
    private String messagestatus; //typing... active... etc
    private String otherrecipient; //other receipient's name
    private String phonename;
    private String phonenumber;
    private String tracktype; //tracking type (emergency or normal)
    private String type; //"single"/"group" (can be used for other purposes in the future
    private String typingname;
    private byte[] profilephoto;

    public RosterlistScheduleModel(String displayname, String appointmenttitle, long
            appointmentdate, long appointmenttime, byte[] profilephoto, String appointmentbubble,
                                   String jid, int notification, String phonenumber,
                                   String phonename) {
        this.displayname = displayname;
        this.phonenumber = phonenumber;
        this.appointmenttitle = appointmenttitle;
        this.appointmentdate = appointmentdate;
        this.appointmenttime = appointmenttime;
        this.profilephoto = profilephoto;
        this.appointmentbubble = appointmentbubble;
        this.jid = jid;
        this.appointmentnotification = notification;
        this.phonename = phonename;
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

    public int getLeaveroom() {
        return leaveroom;
    }

    public void setLeaveroom(int leaveroom) {
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

    public void setDate(int date) {
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
}
