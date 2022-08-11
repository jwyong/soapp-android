package com.soapp.soapp_tab.favourite;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.Restaurant;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

public class FavVM extends ViewModel {

    private MediatorLiveData<List<Restaurant>> resData;

    public FavVM() {


        resData = new MediatorLiveData<>();
        resData.setValue(null);
        LiveData<List<Restaurant>> mresData = Soapp.getInstance().getRepository().getResFavList();
        resData.addSource(mresData, resData::setValue);
    }

    public MediatorLiveData<List<Restaurant>> getResData() {
        return resData;
    }

    public void setResData(MediatorLiveData<List<Restaurant>> resData) {
        this.resData = resData;
    }
}

