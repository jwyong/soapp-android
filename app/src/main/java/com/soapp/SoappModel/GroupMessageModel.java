package com.soapp.SoappModel;

/* Created by chang on 12/08/2017. */

public class GroupMessageModel {

    private int chatmarker;
    private int contact;
    private int issender;
    private long date;
    private String contactname;
    private String contactnumber;
    private String displayname;
    private String id;
    private String infoid;
    private String jidstr;
    private String latitude;
    private String longtitude;
    private String map;
    private String media;
    private String otherjid;
    private String restauranttitle;
    private String text;
    private String video;
    private String voice;
    private int play;
    private int color;

    //text (msg, appt) (8)
    public GroupMessageModel(int chatmarker, int issender, String text, long date, String id,
                             String jidstr, String displayname, int color) {
        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.text = text;
        this.displayname = displayname;
        this.color = color;
    }

    //contact (9)
    public GroupMessageModel(int chatmarker, int issender, String jidstr, long date, String
            contactname, String contactnumber, String id, String displayname, int color) {
        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.contactname = contactname;
        this.contactnumber = contactnumber;
        this.id = id;
        this.jidstr = jidstr;
        this.displayname = displayname;
        this.color = color;
    }

    //map (10)
    public GroupMessageModel(int chatmarker, int issender, long date, String id, String jidstr,
                             String latitude, String longtitude, String map,
                             String displayname, int color) {

        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.map = map;
        this.displayname = displayname;
        this.color = color;
    }

    //media (11) (image, video, audio)
    public GroupMessageModel(int chatmarker, int issender, long date, String id, String jidstr, String
            media, int play, String video, String voice, String displayname, int color) {

        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.media = media;
        this.play = play;
        this.video = video;
        this.voice = voice;
        this.displayname = displayname;
        this.color = color;
    }

    //restaurant (12)
    public GroupMessageModel(int chatmarker, int issender, long date, String id, String jidstr, String
            media, String restauranttitle, String infoid, String video, String voice, String displayname, int color) {

        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.infoid = infoid;
        this.jidstr = jidstr;
        this.media = media;
        this.restauranttitle = restauranttitle;
        //extras
        this.video = video;
        this.voice = voice;
        this.displayname = displayname;
        this.color = color;
    }

    //Full (18)
    public GroupMessageModel(int chatmarker, int contact, int issender, long date, String contactname,
                             String contactnumber, String displayname, String id, String infoid,
                             String jidstr, String latitude, String longtitude, String map, String
                                     media, String otherjid, String restauranttitle, String text,
                             String video, String voice, int play, int color) {

        this.chatmarker = chatmarker;
        this.contact = contact;
        this.issender = issender;
        this.date = date;
        this.contactname = contactname;
        this.contactnumber = contactnumber;
        this.displayname = displayname;
        this.id = id;
        this.infoid = infoid;
        this.jidstr = jidstr;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.map = map;
        this.media = media;
        this.otherjid = otherjid;
        this.restauranttitle = restauranttitle;
        this.text = text;
        this.video = video;
        this.voice = voice;
        this.play = play;
        this.color = color;
    }

    public int getChatmarker() {
        return chatmarker;
    }

    public void setChatmarker(int chatmarker) {
        this.chatmarker = chatmarker;
    }

    public int getContact() {
        return contact;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public int getIssender() {
        return issender;
    }

    public void setIssender(int issender) {
        this.issender = issender;
    }

    public long getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
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

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfoid() {
        return infoid;
    }

    public void setInfoid(String infoid) {
        this.infoid = infoid;
    }

    public String getJidstr() {
        return jidstr;
    }

    public void setJidstr(String jidstr) {
        this.jidstr = jidstr;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getOtherjid() {
        return otherjid;
    }

    public void setOtherjid(String otherjid) {
        this.otherjid = otherjid;
    }

    public String getRestauranttitle() {
        return restauranttitle;
    }

    public void setRestauranttitle(String restauranttitle) {
        this.restauranttitle = restauranttitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public int getPlay() {
        return play;
    }

    public void setPlay(int play) {
        this.play = play;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}

