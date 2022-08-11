package com.soapp.SoappApi.ApiModel;

/**
 * Created by rlwt on 5/7/18.
 */

public class Resource1v3Model {

    private String resource_type, resource_id, resource_url, resource_version, thumbnail;

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
