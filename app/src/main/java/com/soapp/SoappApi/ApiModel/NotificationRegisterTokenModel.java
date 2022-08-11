package com.soapp.SoappApi.ApiModel;

/**
 * Created by rlwt on 5/8/18.
 */

public class NotificationRegisterTokenModel {
    //body
    private String jid, token_type = "fcm", token;

    public NotificationRegisterTokenModel(String jid, String token_type, String token) {
        this.jid = jid;
        this.token_type = token_type;
        this.token = token;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", jid, token_type, token);
    }
}
