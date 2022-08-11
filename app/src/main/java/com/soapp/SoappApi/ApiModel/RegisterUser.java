package com.soapp.SoappApi.ApiModel;

/**
 * Created by chang on 03/08/2017.
 */

public class RegisterUser {

    //part 1
    private String prefix;
    private String phone;
    private String token;
    private int resend;

    //part 2
    public RegisterUser(String prefix, String phone, Integer resend) {
        this.prefix = prefix;
        this.phone = phone;
        this.resend = resend;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

//    public String getGrant_type() {
//        return grant_type;
//    }
//
//    public void setGrant_type(String grant_type) {
//        this.grant_type = grant_type;
//    }
//
//    public String getClient_secret() {
//        return client_secret;
//    }
//
//    public void setClient_secret(String client_secret) {
//        this.client_secret = client_secret;
//    }
//
//    public String getClient_id() {
//        return client_id;
//    }
//
//    public void setClient_id(String client_id) {
//        this.client_id = client_id;
//    }


}
