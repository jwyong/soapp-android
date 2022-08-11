package com.soapp.SoappApi.ApiModel;

/**
 * Created by rlwt on 5/7/18.
 */

public class RegisterModel {
    //body
    String sms_code, token, password, description;

    //response
    String user_id, device_id, xmpp_password;

    public RegisterModel(String sms_code, String token, String password, String description) {
        this.sms_code = sms_code;
        this.token = token;
        this.password = password;
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getXmpp_password() {
        return xmpp_password;
    }

    public void setXmpp_password(String xmpp_password) {
        this.xmpp_password = xmpp_password;
    }
}
