package com.soapp.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

    private static final String PREFS_NAME = "SO_APP";

    private static Preferences sInstance;

    public Preferences() {
        super();
    }

    public static synchronized Preferences getInstance() {
        if (sInstance == null) {
            sInstance = new Preferences();
        }
        return sInstance;
    }

    public void save(Context context, String strPrefName, String strData) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(strPrefName, strData);
        editor.apply();
    }

    public void saveint(Context context, String strPrefName, int strData) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putInt(strPrefName, strData);
        editor.apply();
    }

    public int getIntValue(Context context, String strPrefName) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(strPrefName, -1);
    }


    public String getValue(Context context, String strPrefName) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return settings.getString(strPrefName, "nil");
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    public void removeValue(Context context, String strPrefName) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(strPrefName);
        editor.apply();
    }

}
