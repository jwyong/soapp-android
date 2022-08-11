package com.soapp.global.DateTime.DateTimePickerHelper.decorators;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewFacade;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.spans.DotSpan2;
import com.soapp.global.GlobalVariables;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator3 implements DayViewDecorator {

    private final int colors;
    private final HashSet<CalendarDay> dates;


    public EventDecorator3(Collection<CalendarDay> dates, int colors) {
        //this.color = color;
        this.dates = new HashSet<>(dates);

        this.colors = colors;

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {

        view.addSpan((new DotSpan2(3f * GlobalVariables.screenDensity, colors)));

    }
}
