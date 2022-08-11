package com.soapp.global;

import android.os.Build;
import android.os.Environment;

import com.soapp.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public class GlobalVariables {
    //public static for global edittext
    public static String string1;
    public static long long1;

    //previous version code
    public static String prevVerCode = "prevVerCode";

    //ibrahim- for notification & noti channel
    public static final String chatNotiChnlID = "Chat";
    public static final String chatNotiGroup = "ChatMessage";
    public static final String apptNotiChnlID = "Appointment";
    public static final String apptNotiGroup = "AppointmentGrp";

    //error logging
    public static String APP_VERSION = BuildConfig.VERSION_NAME;
    public static String DEVICE_DESC = String.format("%s ANDROID %s", new MiscHelper().getDeviceName(), Build.VERSION.RELEASE);

    //screen density
    public static float screenDensity;

    //xmpp domain
    public static String xmppURL = "@xmpp.soappchat.com";

    //hosts for xmpp and pubsub
    public static String xmppHost = "xmpp.soappchat.com";
    public static String pubsubHost = "pubsub.xmpp.soappchat.com";

    //default appointment reminder alert in minutes
    public static int defaultReminderAlert = 60;

    //    public static String DB_PATH = Environment.getExternalStorageDirectory() + "/Soapp/Database";
    public static String TEMP_IMG_FILE = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappImages/Temp.jpg";

    public static String WALLPAPERS_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappWallpapers/";

    public static String IMAGES_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappImages/";

    public static String PROFILE_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappProfile/";

    public static String PROFILE_CROPPED_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappProfile/Cropped/";

    public static String IMAGES_SENT_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappImages/Sent";

    public static String AUDIO_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappAudio/";

    public static String AUDIO_SENT_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappAudio/Sent/";

    public static String VIDEO_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappVideo/";

    public static String VIDEO_SENT_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappVideo/Sent";

    public static String VIDEO_THUMBNAIL_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappVideo/Thumbnail";

    public static String VIDEO_SENT_THUMBNAIL_PATH = Environment.getExternalStorageDirectory() +
            "/Soapp/SoappVideo/Sent/Thumbnail";

    //string array of colors in HEX format - get from colors.xml
    public static String[] grpColors = {"#26D3CE", "#d6941a", "#f7b91b", "#AFDD2E", "#3fd647",
            "#5C58DE", "#184fcf", "#7d18cf", "#c018cf", "#f2188d", "#8A0E0E", "#f57614",
            "#777338", "#5f9b12", "#2abe36", "#229F82", "#0b8d8f", "#2d68ba", "#8609CE",
            "#C607CD", "#ed2dca", "#890c44", "#CE6F7F", "#8b6464", "#B6682C", "#000000"};

    // Terms and Condition
    public static String STRURL_TERMSANDCONDITION = "http://www.soappchat.com/terms-of-use.html";

    // Registrations
    public static String STRPREF_COUNTRY_CODE = "COUNTRY_CODE";
    public static String STRPREF_PHONE_NUMBER = "PHONE_NUMBER";
    public static String STRPREF_IS_REGISTERED = "IS_REGISTERED";
    public static String STRPREF_USER_TOKEN = "USER_TOKEN";
    public static String STRPREF_USER_ID = "USER_ID";
    public static String STRPREF_DEVICE_ID = "DEVICE_ID";
    public static String STRPREF_XMPP_PASSWORD = "XMPP_PASSWORD";
    public static String STRPREF_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static String STRPREF_TOKEN_TYPE = "TOKEN_TYPE";
    public static String STRPREF_EXPIRES_IN = "EXPIRES_IN";
    public static String STRPREF_CLIENT = "client_secret";
    public static String STRPREF_USERNAME = "USER_NAME";
    public static String STRPREF_LOGIN_STATUS = "LOGINSTATUS";
    public static String STRPREF_NOTIFICATION_TIME = "NOTIFICATION_TIME";
    public static String STRPREF_NOTIFICATION_TIMESTR = "NOTIFICATION_TIMESTR";
    public static String SARPREF_PATH = "20c1973b8cf1b78fc197c8e3e11bd2b27d6b1762";
    public static String STSPREF_PATH = "d9bb6e042e6c4328f181f2d48af5d1b7890c47d2";
    public static String STREREF_PATH = "f3fbc35274dd50029aa4e64089d53467ed7859a4";
    public static String STRPFEF_PATH = "1872e5722686279e88eac168d74ef37ac4ae564f";
    public static String STRPRDF_PATH = "42f061bf4505ff971fb601c2f36c116115f8d862";
    public static String STRPREG_PATH = "246b2e16d862f3e92e39aa28357846f3737a2db4";
    public static String STRPREF_PATH = "6fd8ph4c05t9v9yh9neu4pgdbbavif6n0zgp6nqd";
    public static String ATRPREF_PATH = "85d2a26c7cdb49919207e35bb12a4c80c824bced";
    public static String SDRPREF_PATH = "75bb9476d63d4712df20e81b8494f00796dc07d1";
    public static String STFPREF_PATH = "3e07cd2e87c3a2355d69689f8478597f24cf40cf";
    public static String STRURL_AUD_PATH = "audioPath";
    public static String GRANT_TYPE = "grant_type";
    public static String PASS = "password";
    public static String USERID = "user_id";
    public static String DEVICEID = "device_id";
    public static String CLIENTID = "client_id";

    public static final String FCM_TOKEN = "fcmToken";

    //Reward
    public static String SOAPP_POINTS = "soapp_points";
    public static String EXPERIENCE_POINTS = "experience_points";
    public static String QUEST_LVL = "quest_lvl";
    public static int QUEST_LVL_1_MAX_POINT = 350;

    //referrer
    public static String STR_SHARER = "SHARER";

    // AES 128 number key
    public static String ivKey = "SoaPPiV50499k3y0";
    public static String key = "sOApp50499eNc100"; // 128 bit key

    //for retrofit
    public static Map<String, Call> retrofitCallStack = new HashMap<>();
}
