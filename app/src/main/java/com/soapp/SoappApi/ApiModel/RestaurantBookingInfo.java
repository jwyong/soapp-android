package com.soapp.SoappApi.ApiModel;

/* Created by chang on 21/09/2017. */

public class RestaurantBookingInfo {

    private String booking_type, booking_date, booking_time, pax, /*nullable*/
            promotion_id,
            restaurant_id, /*nullable*/
            special_request, booking_id;


    public RestaurantBookingInfo(String booking_type, String booking_date, String booking_time,
                                 String pax, String promotion_id, String restaurant_id, String
                                         special_request) {
        this.booking_type = booking_type;
        this.booking_date = booking_date;
        this.booking_time = booking_time;
        this.pax = pax;
        this.promotion_id = promotion_id;
        this.restaurant_id = restaurant_id;
        this.special_request = special_request;
    }

    public RestaurantBookingInfo(String booking_type, String booking_date, String booking_time, String pax, String promotion_id, String restaurant_id, String special_request, String booking_id) {
        this.booking_type = booking_type;
        this.booking_date = booking_date;
        this.booking_time = booking_time;
        this.pax = pax;
        this.promotion_id = promotion_id;
        this.restaurant_id = restaurant_id;
        this.special_request = special_request;
        this.booking_id = booking_id;
    }

    public String getBooking_type() {
        return booking_type;
    }

    public void setBooking_type(String booking_type) {
        this.booking_type = booking_type;
    }

    public String getBooking_date() {
        return booking_date;
    }

    public void setBooking_date(String booking_date) {
        this.booking_date = booking_date;
    }

    public String getBooking_time() {
        return booking_time;
    }

    public void setBooking_time(String booking_time) {
        this.booking_time = booking_time;
    }

    public String getPax() {
        return pax;
    }

    public void setPax(String pax) {
        this.pax = pax;
    }

    public String getPromotion_id() {
        return promotion_id;
    }

    public void setPromotion_id(String promotion_id) {
        this.promotion_id = promotion_id;
    }

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String getSpecial_request() {
        return special_request;
    }

    public void setSpecial_request(String special_request) {
        this.special_request = special_request;
    }

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }
}
