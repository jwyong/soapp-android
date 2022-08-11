package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 21/11/2017. */

public class VersionControlModel {

    private int code, remaining_days;

    public VersionControlModel(int code, int remaining_days) {
        this.code = code;
        this.remaining_days = remaining_days;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRemaining_days() {
        return remaining_days;
    }

    public void setRemaining_days(int remaining_days) {
        this.remaining_days = remaining_days;
    }
}
