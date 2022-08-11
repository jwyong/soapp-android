package com.soapp.schedule_class.new_appt.NewGrpAppt;

import com.soapp.setup.Soapp;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

/* Created by Jayyong on 30/04/2018. */


public class NewGrpApptVM extends ViewModel {

    private MediatorLiveData<List<ContactRoster>> mObservableNewGrpAppt;

    public NewGrpApptVM() {
        mObservableNewGrpAppt = new MediatorLiveData<>();
    }

    public void getData() {
        LiveData<List<ContactRoster>> cr_selected = Soapp.getDatabase().selectQuery().load_selectedGrpMemCR();
        mObservableNewGrpAppt.addSource(cr_selected, value -> mObservableNewGrpAppt.setValue(value));
    }

    public MediatorLiveData<List<ContactRoster>> getmObservableNewGrpAppt() {
        return mObservableNewGrpAppt;
    }
}
