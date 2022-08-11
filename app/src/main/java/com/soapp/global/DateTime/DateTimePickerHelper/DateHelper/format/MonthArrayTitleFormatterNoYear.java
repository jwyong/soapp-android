package com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.format;

import android.text.SpannableStringBuilder;

import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;

/**
 * Created by jason on 19/04/2018.
 */


public class MonthArrayTitleFormatterNoYear implements TitleFormatter {

    private final CharSequence[] monthLabels;

    /**
     * Format using an array of month labels
     *
     * @param monthLabels an array of 12 labels to use for months, starting with January
     */
    public MonthArrayTitleFormatterNoYear(CharSequence[] monthLabels) {
        if (monthLabels == null) {
            throw new IllegalArgumentException("Label array cannot be null");
        }
        if (monthLabels.length < 12) {
            throw new IllegalArgumentException("Label array is too short");
        }
        this.monthLabels = monthLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence format(CalendarDay day) {
        return new SpannableStringBuilder()
                .append(monthLabels[day.getMonth()])
                .append(" ");
    }
}