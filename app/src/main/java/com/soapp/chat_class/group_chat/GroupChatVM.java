package com.soapp.chat_class.group_chat;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.GeneralSelectQuery;
import com.soapp.sql.room.entity.ChatList;
import com.soapp.sql.room.joiners.GroupChatLogDB;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class GroupChatVM extends ViewModel {
    LiveData<PagedList<GroupChatLogDB>> messagelist;
    private String jid;

    private MediatorLiveData<ChatList> getGrpTypingStatus = new MediatorLiveData<>();

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public void init(GeneralSelectQuery selectQuery) {
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPrefetchDistance(15)
                        .setPageSize(30)
                        .setInitialLoadSizeHint(30)
                        .build();

        messagelist = new LivePagedListBuilder<>(selectQuery.getGrpChatLogPaging(jid), pagedListConfig)
                .build();
    }

    public MediatorLiveData<ChatList> getGetGrpTypingStatus() {
        return getGrpTypingStatus;
    }

    public void setGetGrpTypingStatus(MediatorLiveData<ChatList> getGrpTypingStatus) {
        this.getGrpTypingStatus = getGrpTypingStatus;
    }
}
