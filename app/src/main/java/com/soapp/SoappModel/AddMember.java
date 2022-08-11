package com.soapp.SoappModel;

/* Created by chang on 11/08/2017. */

public class AddMember {
    private String displayname;
    private String jid;
    private byte[] profilephoto;
    private String phonenumber;

    public AddMember(String displayname, String jid, String phonenumber, byte[] profilephoto) {
        this.displayname = displayname;
        this.jid = jid;
        this.phonenumber = phonenumber;
        this.profilephoto = profilephoto;
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

    public byte[] getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(byte[] profilephoto) {
        this.profilephoto = profilephoto;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
