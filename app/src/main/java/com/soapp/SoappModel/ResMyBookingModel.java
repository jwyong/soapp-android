package com.soapp.SoappModel;

/* Created by chang on 29/09/2017. */

public class ResMyBookingModel {
    private String bookerJID, bookingQRCode, resJID, pax, promo, resImgURL, resLat,
            resLon, resLoc, resTitle, resState, resPhone;
    private int bookingStatus, fav;
    private long bookingDate, bookingTime;

    public ResMyBookingModel(String bookerJID, String bookingQRCode, String resJID, String pax,
                             String promo, String resImgURL, String resLat,
                             String resLon, String resLoc, String resTitle, String resState, int
                                     bookingStatus, int fav, long bookingDate, long bookingTime,
                             String resPhone) {

        this.bookerJID = bookerJID;
        this.bookingQRCode = bookingQRCode;
        this.resJID = resJID;
        this.pax = pax;
        this.promo = promo;
        this.resImgURL = resImgURL;
        this.resLat = resLat;
        this.resLon = resLon;
        this.resLoc = resLoc;
        this.resTitle = resTitle;
        this.resState = resState;
        this.resPhone = resPhone;
        this.bookingStatus = bookingStatus;
        this.fav = fav;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
    }

    public String getBookerJID() {
        return bookerJID;
    }

    public void setBookerJID(String bookerJID) {
        this.bookerJID = bookerJID;
    }

    public String getBookingQRCode() {
        return bookingQRCode;
    }

    public void setBookingQRCode(String bookingQRCode) {
        this.bookingQRCode = bookingQRCode;
    }

    public String getResJID() {
        return resJID;
    }

    public void setResJID(String resJID) {
        this.resJID = resJID;
    }

    public String getPax() {
        return pax;
    }

    public void setPax(String pax) {
        this.pax = pax;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public String getResImgURL() {
        return resImgURL;
    }

    public void setResImgURL(String resImgURL) {
        this.resImgURL = resImgURL;
    }

    public String getResLat() {
        return resLat;
    }

    public void setResLat(String resLat) {
        this.resLat = resLat;
    }

    public String getResLon() {
        return resLon;
    }

    public void setResLon(String resLon) {
        this.resLon = resLon;
    }

    public String getResLoc() {
        return resLoc;
    }

    public void setResLoc(String resLoc) {
        this.resLoc = resLoc;
    }

    public String getResTitle() {
        return resTitle;
    }

    public void setResTitle(String resTitle) {
        this.resTitle = resTitle;
    }

    public String getResState() {
        return resState;
    }

    public void setResState(String resState) {
        this.resState = resState;
    }

    public int getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(int bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public int getFav() {
        return fav;
    }

    public void setFav(int fav) {
        this.fav = fav;
    }

    public long getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(long bookingDate) {
        this.bookingDate = bookingDate;
    }

    public long getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(long bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getResPhone() {
        return resPhone;
    }

    public void setResPhone(String resPhone) {
        this.resPhone = resPhone;
    }
}
