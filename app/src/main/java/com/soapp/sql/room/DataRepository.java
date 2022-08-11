package com.soapp.sql.room;

import com.soapp.sql.room.entity.Restaurant;
import com.soapp.sql.room.joiners.Applist;
import com.soapp.sql.room.joiners.ChatTabList;
import com.soapp.sql.room.joiners.SharingList;

import java.util.List;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

/* Created by ibrahim on 16/03/2018. */

//LiveData repo for the 5 main tabs
public class DataRepository {

    private static DataRepository sInstance;

    private final SoappDatabase mDatabase;

    private String jid;

    //schetab
    private MediatorLiveData<List<Applist>> mObservableAppointment;

    //res favourites
    private MediatorLiveData<List<Restaurant>> resFavList;

    private MediatorLiveData<List<ChatTabList>> chatTabList;

    private MediatorLiveData<List<SharingList>> sharinglist;


    private DataRepository(final SoappDatabase database) {
        mDatabase = database;

        //schetab
        mObservableAppointment = new MediatorLiveData<>();
        mObservableAppointment.addSource(mDatabase.selectQuery().getScheTab(), applists -> {
            mObservableAppointment.postValue(applists);
        });

        //res favourites
        resFavList = new MediatorLiveData<>();
        resFavList.addSource(mDatabase.selectQuery().getFavourites(), s -> {
            resFavList.postValue(s);
        });

        //chattab
        chatTabList = new MediatorLiveData<>();
        chatTabList.addSource(mDatabase.selectQuery().getChattabList(), chatTabLists -> {
            chatTabList.setValue(chatTabLists);
        });

        sharinglist = new MediatorLiveData<>();
        sharinglist.addSource(mDatabase.selectQuery().getSharing(), sharingLists -> {
            sharinglist.postValue(sharingLists);
        });

    }

    public static DataRepository getInstance(final SoappDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of products from the database and get notified when the data changes.
     */

    public MutableLiveData<List<Applist>> getScheTab() {
        return mObservableAppointment;
    }

    public MediatorLiveData<List<Restaurant>> getResFavList() {
        return resFavList;
    }

    public MediatorLiveData<List<ChatTabList>> getChatTabList() {
        return chatTabList;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public MediatorLiveData<List<SharingList>> getSharinglist() {
        return sharinglist;
    }

}
