package com.soapp.SoappApi.ApiModel;

public class ErrorModelParameter {

    private String description, app_version, app_type, request, error, created_at;

    public ErrorModelParameter(String description, String app_version, String app_type, String request, String error, String created_at) {
        this.description = description;
        this.app_version = app_version;
        this.app_type = app_type;
        this.request = request;
        this.error = error;
        this.created_at = created_at;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_type() {
        return app_type;
    }

    public void setApp_type(String app_type) {
        this.app_type = app_type;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
