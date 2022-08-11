package com.soapp.schedule_class.group_appt;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.Appointment;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

/* Created by Jayyong on 29/04/2018. */


public class GroupScheLogVM extends ViewModel {

    private MediatorLiveData<List<Appointment>> mObservableGrpScheLog;
    private String jid;

    public GroupScheLogVM() {
        mObservableGrpScheLog = new MediatorLiveData<>();
    }

    public void getData() {
        LiveData<List<Appointment>> appList = Soapp.getDatabase().selectQuery().load_scheLog(jid);
        mObservableGrpScheLog.addSource(appList, value -> mObservableGrpScheLog.setValue(value));
    }

    public MediatorLiveData<List<Appointment>> getmObservableGrpScheLog() {
        return mObservableGrpScheLog;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }
}
