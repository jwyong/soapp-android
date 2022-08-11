package com.soapp.global.DateTime.DateTimePickerHelper.decorators;

import android.graphics.drawable.Drawable;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by rlwt on 6/5/18.
 */

public class EventDecoratorCurrentDate implements DayViewDecorator {

    private final Drawable drawable;
    private final HashSet<CalendarDay> dates;


    public EventDecoratorCurrentDate(Collection<CalendarDay> dates, Drawable drawable) {
        //this.color = color;
        this.dates = new HashSet<>(dates);

        this.drawable = drawable;

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {

        view.setBackgroundDrawable(drawable);

    }
}
