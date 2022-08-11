package com.soapp.SoappApi.ApiModel;

/**
 * Created by rlwt on 5/7/18.
 */

public class AuthenticateModel {
    //body
    String grant_type, user_id, device_id, password, client_id, client_secret;

    //response
    String access_token;

    public AuthenticateModel(String grant_type, String user_id, String device_id, String password, String client_id, String client_secret) {
        this.grant_type = grant_type;
        this.user_id = user_id;
        this.device_id = device_id;
        this.password = password;
        this.client_id = client_id;
        this.client_secret = client_secret;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
