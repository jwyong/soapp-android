package com.soapp.SoappApi.ApiModel;

/* Created by chang on 02/08/2017. */

public class GetUserRepo {

    private String access_token;
    private String jid;
    private String name;
    private String resource_url;
    private String phone_number;
    private String image_data;

    public String getAccess_tokenl() {
        return access_token;
    }

    public void setAccess_tokenl(String access_tokenl) {
        this.access_token = access_tokenl;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
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

    public String getImage_data() {
        return image_data;
    }

    public void setImage_data(String image_data) {
        this.image_data = image_data;
    }
}
