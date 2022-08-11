package com.soapp.sql.room.joiners;

import com.soapp.sql.room.entity.Booking;
import com.soapp.sql.room.entity.Restaurant;

import androidx.room.Embedded;

public class BookingList {

    @Embedded
    private Booking booking;

    @Embedded
    private Restaurant restaurant;

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
