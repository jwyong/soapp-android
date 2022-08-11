package com.soapp.schedule_class.single_appt;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

/* Created by Jayyong on 29/04/2018. */


public class IndiScheLogVM extends ViewModel {

    private MediatorLiveData<List<Appointment>> mObservableIndiScheLog;
    private String jid;

    public IndiScheLogVM() {
        mObservableIndiScheLog = new MediatorLiveData<>();
    }

    public void getData() {
        LiveData<List<Appointment>> appList = Soapp.getDatabase().selectQuery().load_scheLog(jid);
        mObservableIndiScheLog.addSource(appList, value -> mObservableIndiScheLog.setValue(value));
    }

    public MediatorLiveData<List<Appointment>> getmObservableIndiScheLog() {
        return mObservableIndiScheLog;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }
}
