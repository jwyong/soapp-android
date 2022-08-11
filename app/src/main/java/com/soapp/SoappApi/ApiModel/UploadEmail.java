package com.soapp.SoappApi.ApiModel;

/**
 * Created by chang on 01/10/2017.
 */

public class UploadEmail {

    private String jid;
    private String name;
    private String CountryCode;
    private String phone;
    private String email;
    private String message;

    public UploadEmail(String jid, String name, String CountryCode, String phone, String email, String message) {
        this.jid = jid;
        this.name = name;
        this.CountryCode = CountryCode;
        this.phone = phone;
        this.email = email;
        this.message = message;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getCountryCode() {
        return CountryCode;
    }

    public void setCountryCode(String countryCode) {
        CountryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


