package com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.format;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;

import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;

/**
 * Supply labels for a given day. Default implementation is to format using a {@linkplain SimpleDateFormat}
 */
public interface DayFormatter {

    /**
     * Default implementation used by {@linkplain com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.MaterialCalendarView}
     */
    public static final DayFormatter DEFAULT = new DateFormatDayFormatter();

    /**
     * Format a given day into a string
     *
     * @param day the day
     * @return a label for the day
     */
    @NonNull
    String format(@NonNull CalendarDay day);
}
