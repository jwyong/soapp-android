package com.soapp.base;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

/* Created by chang on 13/08/2017. */

public class BaseLinear extends LinearLayoutManager {

    private boolean isScrollEnabled = true;

    public BaseLinear(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }

}
