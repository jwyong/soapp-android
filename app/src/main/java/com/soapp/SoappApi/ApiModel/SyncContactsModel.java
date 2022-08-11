package com.soapp.SoappApi.ApiModel;

/* Created by chang on 02/08/2017. */

public class SyncContactsModel {

    private String phone_number, resource_url, user_jid, name, image_data;

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public String getUser_jid() {
        return user_jid;
    }

    public void setUser_jid(String user_jid) {
        this.user_jid = user_jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_data() {
        return image_data;
    }

    public void setImage_data(String image_data) {
        this.image_data = image_data;
    }
}
