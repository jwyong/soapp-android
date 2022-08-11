package com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.format;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarUtils;

/**
 * Supply labels for a given day of the week
 */
public interface WeekDayFormatter {
    /**
     * Default implementation used by {@linkplain com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.MaterialCalendarView}
     */
    public static final WeekDayFormatter DEFAULT = new CalendarWeekDayFormatter(CalendarUtils.getInstance());

    /**
     * Convert a given day of the week into a label
     *
     * @param dayOfWeek the day of the week as returned by {@linkplain java.util.Calendar#get(int)} for {@linkplain java.util.Calendar#DAY_OF_YEAR}
     * @return a label for the day of week
     */
    CharSequence format(int dayOfWeek);
}
