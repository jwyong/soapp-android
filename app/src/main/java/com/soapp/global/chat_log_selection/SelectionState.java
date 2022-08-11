package com.soapp.global.chat_log_selection;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

public class SelectionState extends SelectionTracker.SelectionPredicate<Long> {
    private boolean canSetStateForKey, canSetStateAtPosition, canSelectMultiple;

    public SelectionState(boolean canSetStateForKey, boolean canSetStateAtPosition, boolean canSelectMultiple) {
        this.canSetStateForKey = canSetStateForKey;
        this.canSetStateAtPosition = canSetStateAtPosition;
        this.canSelectMultiple = canSelectMultiple;
    }

    @Override
    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
        return canSetStateForKey;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        return canSetStateAtPosition;
    }

    @Override
    public boolean canSelectMultiple() {
        return canSelectMultiple;
    }
}