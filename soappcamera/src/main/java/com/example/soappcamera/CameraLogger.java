package com.example.soappcamera;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Created by Kirill on 10/25/2017.
 */

public class CameraLogger {
    public final static int LEVEL_VERBOSE = 0;
    public final static int LEVEL_WARNING = 1;
    public final static int LEVEL_ERROR = 2;

    @IntDef({LEVEL_VERBOSE, LEVEL_WARNING, LEVEL_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface LogLevel {}

    private static int level = LEVEL_ERROR;

    public static void setLogLevel(int logLevel) {
        level = logLevel;
    }

    static String lastMessage;
    static String lastTag;

    static CameraLogger create(String tag) {
        return new CameraLogger(tag);
    }

    private String mTag;

    private CameraLogger(String tag) {
        mTag = tag;
    }

    void i(String message) {
        if (should(LEVEL_VERBOSE)) {
            lastMessage = message;
            lastTag = mTag;
        }
    }

    void w(String message) {
        if (should(LEVEL_WARNING)) {
            lastMessage = message;
            lastTag = mTag;
        }
    }

    void e(String message) {
        if (should(LEVEL_ERROR)) {
            lastMessage = message;
            lastTag = mTag;
        }
    }

    private boolean should(int messageLevel) {
        return level <= messageLevel;
    }

    private String string(int messageLevel, Object... ofData) {
        String message = "";
        if (should(messageLevel)) {
            for (Object o : ofData) {
                message += String.valueOf(o);
                message += " ";
            }
        }
        return message.trim();
    }

    void i(Object... data) {
        i(string(LEVEL_VERBOSE, data));
    }

    void w(Object... data) {
        w(string(LEVEL_WARNING, data));
    }

    void e(Object... data) {
        e(string(LEVEL_ERROR, data));
    }

}
