package com.soapp.global;

/*Created by Soapp on 04/11/2017. */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.soapp.sql.DatabaseHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Response;

public class MiscHelper {
    //log response unsuccessful + send to crashlytics
    public void retroLogUnsuc(Response response, String retroDesc, String TAG) {
        try {
            String error = retroDesc + " unsuc =  " + response.errorBody().string();
            Log.e(TAG, error);

            DatabaseHelper.getInstance().saveLogsToDb(retroDesc, error, System.currentTimeMillis());
        } catch (IOException ignore) {
        }
    }

    //log response failure + send to crashlytics
    public void retroLogFailure(Throwable throwable, String retroDesc, String TAG) {
        String error = retroDesc + " failed = " + throwable.getMessage();
        Log.e(TAG, error);

        DatabaseHelper.getInstance().saveLogsToDb(retroDesc, throwable.toString(), System.currentTimeMillis());
    }

    //get color for group displayname
    public static String getColorForGrpDisplayName(int i) {
        String color;
        if (i <= 25) {
            color = GlobalVariables.grpColors[i];
        } else if (i <= 51) {
            color = GlobalVariables.grpColors[i - 26];
        } else if (i <= 77) {
            color = GlobalVariables.grpColors[i - 52];
        } else if (i <= 103) {
            color = GlobalVariables.grpColors[i - 78];
        } else if (i <= 129) {
            color = GlobalVariables.grpColors[i - 104];
        } else if (i <= 155) {
            color = GlobalVariables.grpColors[i - 130];
        } else if (i <= 181) {
            color = GlobalVariables.grpColors[i - 156];
        } else if (i <= 207) {
            color = GlobalVariables.grpColors[i - 182];
        } else {
            color = GlobalVariables.grpColors[i - 208];
        }
        return color;
    }

    //[JAY] get remaining days in appt room
    public static String getRemainingTime(final long dateLong) {

        long currentTime = System.currentTimeMillis();
        long seconds = (dateLong - currentTime) / 1000;

        if (seconds > 30 * 86400) { //more than 30 days
            return "More than a month";
        } else if (seconds > 14 * 86400) {
            return "More than 2 weeks";
        } else if (seconds > 7 * 86400) {
            return "More than a week";
        } else {
            int dateMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.ENGLISH).format
                    (dateLong));
            int dateDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.ENGLISH).format
                    (dateLong));
            int currentMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.ENGLISH).format
                    (currentTime));
            int currentDay = Integer.parseInt(new SimpleDateFormat("dd", Locale.ENGLISH).format
                    (currentTime));
            int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.ENGLISH).format
                    (currentTime));
            int dayDiff;

            if (dateMonth == currentMonth) {
                if (dateDay == currentDay) {
                    return "Today";
                } else if (seconds < 0) { //negative, appointment already past
                    return "Appointment Past";
                } else {
                    dayDiff = dateDay - currentDay;
                }

            } else {
                switch (currentMonth) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 10:
                    case 12: //months with 31 days
                        dayDiff = dateDay + 31 - currentDay;
                        break;

                    case 2: //Feb
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, currentYear);
                        if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) <= 365) { //non-leap year
                            dayDiff = dateDay + 28 - currentDay;
                        } else {
                            dayDiff = dateDay + 29 - currentDay;
                        }
                        break;

                    default: //Other months
                        dayDiff = dateDay + 30 - currentDay;
                        break;
                }

            }
            switch (dayDiff) {
                case 7:
                    return "In a week";

                case 6:
                    return "In 6 days";

                case 5:
                    return "In 5 days";

                case 4:
                    return "In 4 days";

                case 3:
                    return "In 3 days";

                case 2:
                    return "In 2 days";

                case 1:
                    return "Tomorrow";

                default:
                    return "";
            }
        }
    }

    //get density of screen (ldpi, hdpi, etc)
    public String getDeviceDensity(Context context) {
        float dpi = context.getResources().getDisplayMetrics().density;

        if (dpi <= 0.75) {
            return "ldpi";

        } else if (dpi <= 1.0) {
            return "mdpi";

        } else if (dpi <= 1.5) {
            return "hdpi";

        } else if (dpi <= 2.0) {
            return "xhdpi";

        } else if (dpi <= 3.0) {
            return "xxhdpi";

        } else {
            return "xxxhdpi";
        }
    }

    public Integer getDeviceAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    public Integer getChatLogImageSize(Context context) {
        float dpi = context.getResources().getDisplayMetrics().density;
        int display = context.getResources().getDisplayMetrics().widthPixels;

        double result = (0.65 * display) - (30 * dpi);

        if (result > 720) {
            return 720;
        } else {
            return ((int) result);
        }
    }

    public Integer getPixelSizeforChattabProfile(Context context, int dpSize) {
        float dpi = context.getResources().getDisplayMetrics().density;
        double result = dpSize * dpi;

        return ((int) result);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    //send coords to waze app - go to app store download if no
    public void openWazeApp(Context context, String latitude, String longitude) {
        if (latitude != null && !latitude.equals("")) {
            try {
                String uri = "waze://?ll=" + latitude + "," + longitude + "&navigate=yes";
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            } catch (ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
                context.startActivity(intent);
            }
        }
    }

    //send coords to waze app - go to app store download if no
    public void openGmapsApp(Context context, String latitude, String longitude) {
        if (latitude != null && !latitude.equals("")) {
            try {
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                context.startActivity(mapIntent);
            } catch (ActivityNotFoundException ex) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
                context.startActivity(intent);
            }
        }
    }

    //google maps URL
    public String getGmapsStaticURL(String mapLat, String mapLon) {
        return "https://maps.googleapis.com/maps/api/staticmap?center=" + mapLat + "," + mapLon +
                "&zoom=16" +
                "&size=400x200" +
                "&maptype=roadmap" +
                "&format=png" +
                "&visual_refresh=true" +
                "&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + mapLat + "," + mapLon +
                "&key=AIzaSyAhnRJcwQhHiPRQRCF1eg5OClxByUHjK1k";
    }

}
