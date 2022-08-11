package com.soapp.SoappApi.ApiModel;

/* Created by Soapp on 01/03/2018. */

public class ResDetPromoTypeModel {

    private String promotion_id, title, date_start, date_end, discount, details_1, details_2,
            resource_url, qr_code_id, created_at;

    public String getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(String promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate_start() {
        return date_start;
    }

    public void setDate_start(String date_start) {
        this.date_start = date_start;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDetails_1() {
        return details_1;
    }

    public void setDetails_1(String details_1) {
        this.details_1 = details_1;
    }

    public String getDetails_2() {
        return details_2;
    }

    public void setDetails_2(String details_2) {
        this.details_2 = details_2;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public String getQr_code_id() {
        return qr_code_id;
    }

    public void setQr_code_id(String qr_code_id) {
        this.qr_code_id = qr_code_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
