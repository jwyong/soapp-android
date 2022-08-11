package com.soapp.global.chat_log_selection;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class SelectionDetails extends ItemDetailsLookup.ItemDetails<Long> {

    public long position;

    public SelectionDetails() {
    }

    @Override
    public int getPosition() {
        return (int) position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return position;
    }

    @Override
    public boolean inSelectionHotspot(@NonNull MotionEvent e) {
        return true;
    }
}
