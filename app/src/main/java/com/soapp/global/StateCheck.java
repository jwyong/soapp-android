package com.soapp.global;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.CheckSmackHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/* Created by chang on 24/08/2017. */

public class StateCheck implements Application.ActivityLifecycleCallbacks {
    public static boolean foreground = false, paused = true;
    private static StateCheck instance;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable check;

    /**
     * Its not strictly necessary to use this method - _usually_ invoking
     * get with a Context gives us a path to retrieve the Application and
     * initialise, but sometimes (e.g. in test harness) the ApplicationContext
     * is != the Application, and the docs make no guarantees.
     *
     * @param application
     * @return an initialised Foreground instance
     */
    public static synchronized StateCheck init(Application application) {
        if (instance == null) {
            instance = new StateCheck();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static StateCheck get(Application application) {
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static StateCheck get(Context ctx) {
        if (instance == null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application) appCtx);
            }
            throw new IllegalStateException(
                    "Foreground is not initialised and " +
                            "cannot obtain the Application object");
        }
        return instance;
    }

    public static StateCheck get() {
        if (instance == null) {
            throw new IllegalStateException(
                    "Foreground is not initialised - invoke " +
                            "at least once with parameterised init/get");
        }
        return instance;
    }

    public static boolean isForeground() {
        return foreground;
    }

    public static boolean isBackground() {
        return !foreground;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        //set variables accordingly
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        //old push structure for android 6 and below
        DatabaseHelper.pushChatNotiCount = 0;
        DatabaseHelper.pushScheNotiCount = 0;
        DatabaseHelper.pushBookingNotiCount = 0;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground) {
            for (Listener l : listeners) {
                try {
                    l.onBecameForeground();
                    Log.d("wtf", "onActivityPaused: v" +
                            "" +
                            "" + isForeground());
                } catch (Exception e) {
                }
            }
        }

        //check stream on foreground, straight connect back when user comes into foreground
        Log.d("wtf", "onActivityResumed: ");

        CheckSmackHelper.getInstance().check();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (foreground && paused) {
            foreground = false;
            for (Listener l : listeners) {
                try {
                    l.onBecameBackground();
                    Log.d("wtf", "onActivityPaused: onBecameBackground" + isForeground());
                } catch (Exception e) {
                }
            }
        }
        Log.d("statecheck", "onActivityPaused: ");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public interface Listener {
        void onBecameForeground();

        void onBecameBackground();
    }
}