package com.soapp.schedule_class.Schedulelist;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.joiners.Applist;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

/* Created by Jayyong on 20/04/2018. */


public class ScheduleVM extends ViewModel {
    private LiveData<List<Applist>> list;
    private MediatorLiveData<List<Applist>> mObservableAppointment;
    private MediatorLiveData<Boolean> isSearching;
    private MediatorLiveData<String> searchString;

    Observer<List<Applist>> observerable = new Observer<List<Applist>>() {
        @Override
        public void onChanged(@Nullable List<Applist> value) {
            mObservableAppointment.setValue(value);
        }
    };

    public ScheduleVM() {
        mObservableAppointment = new MediatorLiveData<>();
        isSearching = new MediatorLiveData<>();
        searchString = new MediatorLiveData<>();

        isSearching.setValue(false);
    }

    public void init() {
        if (isSearching.getValue()) {
            if (searchString.getValue() != null && !searchString.getValue().isEmpty()) {
                List<Applist> searchlist = Soapp.getDatabase().selectQuery().searchScheTab(searchString.getValue());
                mObservableAppointment.setValue(searchlist);
            }
        } else {
            list = Soapp.getInstance().getRepository().getScheTab();
            mObservableAppointment.setValue(list.getValue());
        }
    }

    public MediatorLiveData<List<Applist>> getmObservableAppointment() {
        return mObservableAppointment;
    }

    public void addSource() {
        list = Soapp.getInstance().getRepository().getScheTab();
        mObservableAppointment.addSource(list, observerable);
    }

    public void removeSource() {
        list = Soapp.getInstance().getRepository().getScheTab();
        mObservableAppointment.removeSource(list);
    }

    public void setmObservableAppointment(MediatorLiveData<List<Applist>> mObservableAppointment) {
        this.mObservableAppointment = mObservableAppointment;
    }

    public MediatorLiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    public void setIsSearching(boolean isSearching) {
        this.isSearching.setValue(isSearching);
    }

    public MediatorLiveData<String> getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString.setValue(searchString);
    }
}
