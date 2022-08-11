package com.soapp.global.chat_log_selection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

public class KeyProvider extends ItemKeyProvider<Long> {

    public KeyProvider() {
        super(ItemKeyProvider.SCOPE_MAPPED);
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        return (long) position;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        long value = key;
        return (int) value;
    }
}