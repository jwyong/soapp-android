package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 27/09/2017. */

public class GetMemberListModel {

    private String type, user_jid, name, phone_number, resource_url, resource_version;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getResource_version() {
        return resource_version;
    }

    public void setResource_version(String resource_version) {
        this.resource_version = resource_version;
    }
}
