package com.soapp.SoappModel;

/* Created by Soapp on 19/08/2017. */

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;

import java.util.List;

public class ApptCalendarModel {
    private List<CalendarDay> goingArray;
    private List<CalendarDay> undecArray;
    private List<CalendarDay> notGoingArray;

    //Full Appt (0)
    public ApptCalendarModel(List<CalendarDay> goingArray, List<CalendarDay> undecArray, List<CalendarDay> notGoingArray) {
        this.goingArray = goingArray;
        this.undecArray = undecArray;
        this.notGoingArray = notGoingArray;
    }

    public List<CalendarDay> getGoingArray() {
        return goingArray;
    }

    public void setGoingArray(List<CalendarDay> goingArray) {
        this.goingArray = goingArray;
    }

    public List<CalendarDay> getUndecArray() {
        return undecArray;
    }

    public void setUndecArray(List<CalendarDay> undecArray) {
        this.undecArray = undecArray;
    }

    public List<CalendarDay> getNotGoingArray() {
        return notGoingArray;
    }

    public void setNotGoingArray(List<CalendarDay> notGoingArray) {
        this.notGoingArray = notGoingArray;
    }
}
