package com.soapp.soapp_tab.bookinglist;

import android.app.Application;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.joiners.BookingList;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class BookingListVM extends AndroidViewModel {

    LiveData<List<BookingList>> booklist;
    private MediatorLiveData<List<BookingList>> bookList;
    private String jid;
    private String resID;
    private MediatorLiveData<String> stringLiveData;
    private MediatorLiveData<Boolean> isSearching;


    public BookingListVM(@NonNull Application application) {
        super(application);

        bookList = new MediatorLiveData<>();
        isSearching = new MediatorLiveData<>();
        stringLiveData = new MediatorLiveData<>();

        isSearching.setValue(false);


    }

    public void init() {
        booklist = Soapp.getDatabase().selectQuery().getResBookings();

        if (isSearching.getValue()) {


            List<BookingList> searchlist = new ArrayList<>();
            if (stringLiveData.getValue() != null && !stringLiveData.getValue().isEmpty()) {
//                if (booklist.getValue() != null && booklist.getValue().size() > 0) {
//                    for (BookingList blist : booklist.getValue()
//                            ) {
//                        if (blist.getRestaurant() != null) {
//                            if (blist.getRestaurant().getResTitle() != null &&
//                                    blist.getRestaurant().getResTitle().toLowerCase().contains(stringLiveData.getValue())) {
//                                searchlist.add(blist);
//                            }
//                            //todo see if can search for friend name or date
//                        }
//                    }
                searchlist = Soapp.getDatabase().selectQuery().searchResBookings(stringLiveData.getValue());
                bookList.setValue(searchlist);
            }
        } else {
            bookList.setValue(booklist.getValue());
        }

    }

    public MediatorLiveData<List<BookingList>> getBookList() {
        return bookList;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getResID() {
        return resID;
    }

    public void setResID(String resID) {
        this.resID = resID;
    }

    public MediatorLiveData<String> getStringLiveData() {
        return stringLiveData;
    }

    public void setStringLiveData(String stringLiveData) {
        this.stringLiveData.setValue(stringLiveData);
    }

    public MediatorLiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    public void setIsSearching(Boolean isSearching) {
        this.isSearching.setValue(isSearching);
    }

    public void addSource() {
        booklist = Soapp.getDatabase().selectQuery().getResBookings();
        bookList.addSource(booklist, bookList::setValue);
    }

    public void removeSource() {
        bookList.removeSource(booklist);
    }
}
