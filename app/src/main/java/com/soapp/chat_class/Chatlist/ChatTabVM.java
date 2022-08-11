package com.soapp.chat_class.Chatlist;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.joiners.ChatTabList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class ChatTabVM extends ViewModel {

    private LiveData<List<ChatTabList>> list;
    private MediatorLiveData<List<ChatTabList>> chatTabList;
    private Observer<List<ChatTabList>> observer = new Observer<List<ChatTabList>>() {
        @Override
        public void onChanged(@Nullable List<ChatTabList> value) {
            chatTabList.setValue(value);
        }
    };
    private MediatorLiveData<String> stringLiveData;
    private MediatorLiveData<Boolean> isSearching;

    public ChatTabVM() {
        chatTabList = new MediatorLiveData<>();
        isSearching = new MediatorLiveData<>();
        stringLiveData = new MediatorLiveData<>();

        isSearching.setValue(false);
    }

    public void init() {
        LiveData<List<ChatTabList>> list;
        list = Soapp.getInstance().getRepository().getChatTabList();

        if (isSearching.getValue()) {

            List<ChatTabList> searchlist = new ArrayList<>();
            if (stringLiveData.getValue() != null && !stringLiveData.getValue().isEmpty()) {
                for (ChatTabList clist : Objects.requireNonNull(list.getValue())
                        ) {
                    if (clist.getCR() != null) {
                        if (clist.getCR().getPhonename() != null &&
                                clist.getCR().getPhonename().toLowerCase().contains(stringLiveData.getValue())) {
                            searchlist.add(clist);
                        } else if (clist.getCR().getDisplayname() != null &&
                                clist.getCR().getDisplayname().toLowerCase().contains(stringLiveData.getValue())) {
                            searchlist.add(clist);
                        }
                    }
                }
                chatTabList.setValue(searchlist);
            }
        } else {
            chatTabList.setValue(list.getValue());
        }

    }


    MediatorLiveData<List<ChatTabList>> getChatTabList() {
        return chatTabList;
    }

    MediatorLiveData<String> getStringLiveData() {
        return stringLiveData;
    }

    void setStringLiveData(String stringLiveData) {
        this.stringLiveData.setValue(stringLiveData);
    }

    MediatorLiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    void setIsSearching(Boolean isSearching) {
        this.isSearching.setValue(isSearching);
    }

    void removeChatListSource() {
        chatTabList.removeSource(list);
    }

    void addChatListSource() {
        list = Soapp.getInstance().getRepository().getChatTabList();

        chatTabList.addSource(list, observer);
    }

}
