package com.soapp.chat_class.single_chat;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.GeneralSelectQuery;
import com.soapp.sql.room.joiners.IndiChatLogDB;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class IndiChatVM extends ViewModel {
    LiveData<PagedList<IndiChatLogDB>> messagelist;
    private String jid;

    private MediatorLiveData<Integer> getTypingStatus;

    public IndiChatVM() {
        getTypingStatus = new MediatorLiveData<>();
    }

    public IndiChatVM(String jid) {
        this.jid = jid;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public void init(GeneralSelectQuery selectQuery) {
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPrefetchDistance(15)
                        .setPageSize(30)
                        .setInitialLoadSizeHint(30)
                        .build();

//        messagelist = (new LivePagedListBuilder(selectQuery.getIndiChatLogPaging(jid), pagedListConfig))
//                .build();

        messagelist = new LivePagedListBuilder<>(selectQuery.getIndiChatLogPaging(jid), pagedListConfig)
                .build();
    }

    public void getChatInfo() {
        getTypingStatus.addSource(Soapp.getDatabase().selectQuery().getTypingStatus(jid), integer -> getTypingStatus.setValue(integer));
    }

    public MediatorLiveData<Integer> getGetTypingStatus() {
        return getTypingStatus;
    }

    public void setGetTypingStatus(MediatorLiveData<Integer> getTypingStatus) {
        this.getTypingStatus = getTypingStatus;
    }
}
