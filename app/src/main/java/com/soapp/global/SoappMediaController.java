package com.soapp.global;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.MediaController;

public class SoappMediaController extends MediaController {
    Activity activity;

    public SoappMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
    }

    public SoappMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
        activity = (Activity) context;
    }

    public SoappMediaController(Context context) {
        super(context);
        activity = (Activity) context;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.KEYCODE_SOFT_LEFT){
            activity.finish();
            return true;
        }

        return false;

//        return super.dispatchKeyEvent(event);
    }
}
