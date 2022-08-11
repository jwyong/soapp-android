package com.soapp.SoappModel;

/* Created by chang on 31/07/2017. */

public class MessageModel {

    private int chatmarker;
    private int contact;
    private int issender;
    private long date;
    private String contactname;
    private String contactnumber;
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
    private int selected;

    public MessageModel(String media, int selected) {
        this.media = media;
        this.selected = selected;

    }

    //text (msg, appt) (6)
    public MessageModel(int chatmarker, int issender, long date, String id,
                        String jidstr, String text) {
        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.text = text;
    }

    //contact (7)
    public MessageModel(int chatmarker, int issender, long date, String
            contactname, String contactnumber, String id, String jidstr) {
        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.contactname = contactname;
        this.contactnumber = contactnumber;
        this.id = id;
        this.jidstr = jidstr;
    }

    //map (8)
    public MessageModel(int chatmarker, int issender, long date, String id, String jidstr, String
            latitude, String longtitude, String map) {
        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.map = map;
    }

    //media (9) (image, video, audio)
    public MessageModel(int chatmarker, int issender, long date, String id, String jidstr, String
            media, int play, String video, String voice) {

        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.media = media;
        this.play = play;
        this.video = video;
        this.voice = voice;
    }

    //media (11) (restaurant)
    public MessageModel(int chatmarker, int issender, long date, String id, String jidstr, String
            media, String restauranttitle, String infoid, String video, String voice, String contactname) {

        this.chatmarker = chatmarker;
        this.issender = issender;
        this.date = date;
        this.id = id;
        this.jidstr = jidstr;
        this.media = media;
        this.restauranttitle = restauranttitle;
        this.video = video;
        this.voice = voice;
        this.infoid = infoid;
        this.contactname = contactname;
    }

    //Full (16)
    public MessageModel(int chatmarker, int contact, int issender, long date, String contactname,
                        String contactnumber, String id, String infoid,
                        String jidstr, String latitude, String longtitude, String map, String
                                media, String otherjid, String restauranttitle, String text,
                        String video, String voice, int play) {

        this.chatmarker = chatmarker;
        this.contact = contact;
        this.issender = issender;
        this.date = date;
        this.contactname = contactname;
        this.contactnumber = contactnumber;
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


    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
