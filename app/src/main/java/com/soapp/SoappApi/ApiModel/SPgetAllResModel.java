package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 30/10/2017. */

public class SPgetAllResModel {

    private String id, image_data, name, phone_number, resource_url;
//    private byte[] image_data;

    public String getImage_data() {
        return image_data;
    }

    public void setImage_data(String image_data) {
        this.image_data = image_data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    ;

    public void setName(String name) {
        this.name = name;
    }

    ;

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
}
