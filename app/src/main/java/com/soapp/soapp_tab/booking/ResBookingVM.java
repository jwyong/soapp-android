package com.soapp.soapp_tab.booking;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.joiners.BookingDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

public class ResBookingVM extends ViewModel {

    private MediatorLiveData<BookingDetails> bookingInfo;

    private String bookingId, sharedjid;

    public ResBookingVM() {

//        bookingInfo = new MediatorLiveData<>();
//
//        bookingInfo.setValue(null);
//
//        LiveData<List<BookingList>> bookdetail = Soapp.getDatabase().selectQuery().getResBookingDetails(getBookingId());
//        bookingInfo.addSource(bookdetail, bookingInfo::setValue);
    }

    public void init() {
        bookingInfo = new MediatorLiveData<>();

        bookingInfo.setValue(null);

        LiveData<BookingDetails> bookdetail = Soapp.getDatabase().selectQuery().getResBookingDetails(getBookingId());
        bookingInfo.addSource(bookdetail, bookingInfo::setValue);
    }

    public MediatorLiveData<BookingDetails> getBookingInfo() {
        return bookingInfo;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getSharedjid() {
        return sharedjid;
    }

    public void setSharedjid(String sharedjid) {
        this.sharedjid = sharedjid;
    }
}
