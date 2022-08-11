package com.soapp.sql.room.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/* Created by ibrahim on 25/03/2018. */

@Entity(tableName = "BOOKING")
public class Booking {

    @PrimaryKey
    @NonNull
    private String BookingId;

    private String BookerJid;
    private String BookingResId;
    private Integer BookingStatus = 0;
    private String BookingQRCode;
    private Long BookingDate = 0L;
    private Long BookingTime = 0L;
    private String ResOwnerJid;
    private Integer BookingPax = 0;
    private String BookingPromo;
    private Integer BookingAttempts = 0;
    private String BookingSharedJid;

    public Booking() {
    }

    public static Booking fromContentValues(ContentValues values) {

        final Booking booking = new Booking();

        if (values.containsKey("BookingId")) {
            booking.BookingId = values.getAsString("BookingId");
        }
        if (values.containsKey("BookerJid")) {
            booking.BookerJid = values.getAsString("BookerJid");
        }
        if (values.containsKey("BookingResId")) {
            booking.BookingResId = values.getAsString("BookingResId");
        }
        if (values.containsKey("BookingStatus")) {
            booking.BookingStatus = values.getAsInteger("BookingStatus");
        }
        if (values.containsKey("BookingQRCode")) {
            booking.BookingQRCode = values.getAsString("BookingQRCode");
        }
        if (values.containsKey("BookingDate")) {
            booking.BookingDate = values.getAsLong("BookingDate");
        }
        if (values.containsKey("BookingTime")) {
            booking.BookingTime = values.getAsLong("BookingTime");
        }
        if (values.containsKey("ResOwnerJid")) {
            booking.ResOwnerJid = values.getAsString("ResOwnerJid");
        }
        if (values.containsKey("BookingPax")) {
            booking.BookingPax = values.getAsInteger("BookingPax");
        }
        if (values.containsKey("BookingPromo")) {
            booking.BookingPromo = values.getAsString("BookingPromo");
        }
        if (values.containsKey("BookingAttempts")) {
            booking.BookingAttempts = values.getAsInteger("BookingAttempts");
        }
        if (values.containsKey("BookingSharedJid")) {
            booking.BookingSharedJid = values.getAsString("BookingSharedJid");
        }
        return booking;
    }

    public String getBookingId() {
        return BookingId;
    }

    public void setBookingId(String bookingId) {
        BookingId = bookingId;
    }

    public String getBookerJid() {
        return BookerJid;
    }

    public void setBookerJid(String bookerJid) {
        BookerJid = bookerJid;
    }

    public String getBookingResId() {
        return BookingResId;
    }

    public void setBookingResId(String bookingResId) {
        BookingResId = bookingResId;
    }

    public Integer getBookingStatus() {
        return BookingStatus;
    }

    public void setBookingStatus(Integer bookingStatus) {
        BookingStatus = bookingStatus;
    }

    public String getBookingQRCode() {
        return BookingQRCode;
    }

    public void setBookingQRCode(String bookingQRCode) {
        BookingQRCode = bookingQRCode;
    }

    public Long getBookingDate() {
        return BookingDate;
    }

    public void setBookingDate(Long bookingDate) {
        BookingDate = bookingDate;
    }

    public Long getBookingTime() {
        return BookingTime;
    }

    public void setBookingTime(Long bookingTime) {
        BookingTime = bookingTime;
    }

    public String getResOwnerJid() {
        return ResOwnerJid;
    }

    public void setResOwnerJid(String resOwnerJid) {
        ResOwnerJid = resOwnerJid;
    }

    public Integer getBookingPax() {
        return BookingPax;
    }

    public void setBookingPax(Integer bookingPax) {
        BookingPax = bookingPax;
    }

    public String getBookingPromo() {
        return BookingPromo;
    }

    public void setBookingPromo(String bookingPromo) {
        BookingPromo = bookingPromo;
    }

    public Integer getBookingAttempts() {
        return BookingAttempts;
    }

    public void setBookingAttempts(Integer bookingAttempts) {
        BookingAttempts = bookingAttempts;
    }

    public String getBookingSharedJid() {
        return BookingSharedJid;
    }

    public void setBookingSharedJid(String bookingSharedJid) {
        BookingSharedJid = bookingSharedJid;
    }
}
