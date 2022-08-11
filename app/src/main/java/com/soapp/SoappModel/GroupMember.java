package com.soapp.SoappModel;

/* Created by chang on 13/08/2017. */

public class GroupMember {

    private String displayname;
    private String jid;
    private byte[] profilephoto;
    private String phonenumber;
    private String affiliation;
    private int status;

    public GroupMember(String affiliation) {
        this.affiliation = affiliation;
    }

    public GroupMember(String affiliation, String displayname, String jid, String phonenumber, byte[] profilephoto) {
        this.displayname = displayname;
        this.jid = jid;
        this.phonenumber = phonenumber;
        this.profilephoto = profilephoto;
        this.affiliation = affiliation;
    }

    public GroupMember(String affiliation, String displayname, String jid, String phonenumber, byte[] profilephoto, int status) {
        this.displayname = displayname;
        this.jid = jid;
        this.phonenumber = phonenumber;
        this.profilephoto = profilephoto;
        this.affiliation = affiliation;
        this.status = status;
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

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
