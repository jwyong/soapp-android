package com.soapp.global.DateTime.DateTimePickerHelper.decorators;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewFacade;

import java.util.Calendar;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class HighlightWeekendsDecorator implements DayViewDecorator {

    private static final int color = Color.parseColor("#228BC34A");
    private final Calendar calendar = Calendar.getInstance();
    private final Drawable highlightDrawable;

    public HighlightWeekendsDecorator() {
        highlightDrawable = new ColorDrawable(color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
