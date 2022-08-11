package com.soapp.global;

/*Created by Soapp on 04/11/2017. */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.soapp.R;
import com.soapp.SoappModel.ApptCalendarModel;
import com.soapp.chat_class.group_chat.details.GroupChatDetail;
import com.soapp.chat_class.share_contact.ShareContactActivity;
import com.soapp.food.FoodTabActivity;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.CalendarDay;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.DayViewFacade;
import com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.MaterialCalendarView;
import com.soapp.global.DateTime.DateTimePickerHelper.TimeHelper.AbstractWheel;
import com.soapp.global.DateTime.DateTimePickerHelper.TimeHelper.adapters.ArrayWheelAdapter;
import com.soapp.global.DateTime.DateTimePickerHelper.TimeHelper.adapters.NumericWheelAdapter;
import com.soapp.global.DateTime.DateTimePickerHelper.TimeHelper.adapters.NumericWheelAdapterMin;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator2;
import com.soapp.global.DateTime.DateTimePickerHelper.decorators.EventDecorator3;
import com.soapp.global.MediaGallery.GalleryMainActivity;
import com.soapp.home.Home;
import com.soapp.schedule_class.group_appt.GroupScheLog;
import com.soapp.schedule_class.new_appt.NewGrpAppt.NewGrpApptActivity;
import com.soapp.schedule_class.new_appt.NewIndiExistApptActivity;
import com.soapp.settings_tab.Profile;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Appointment;
import com.soapp.xmpp.GroupChatHelper.GroupChatStanza;
import com.soapp.xmpp.SingleChatHelper.SingleChatStanza;
import com.soapp.xmpp.SmackHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import static android.view.Gravity.CENTER;

public class UIHelper {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Preferences preferences = Preferences.getInstance();

    int calYear, calMonth, calDay, hours, Minutes, ampm, getcalYear, getHours, getMinutes, getAmpm, countClick;
    long apptTime, apptDate, dateMillis, timeMillis;
    Date date_Select, time_select, getDate_Select, getTime_select;
    Calendar calendar;
    private boolean check_agreed, checkBox_agreed2;

    //date/time picker for appt and booking etc
    public void dateTimePicker(Context context, String from, String jid, String apptID, String grpName,
                               List<ApptCalendarModel> apptCalList, String selectDate, String selectTime) {
        LayoutInflater dialog_li = LayoutInflater.from(context);
        View View_dialog_datetime = dialog_li.inflate(R.layout.dialog_datetimepicker, null);
        AlertDialog.Builder dialog_datetime = new AlertDialog.Builder(context);
        dialog_datetime.setView(View_dialog_datetime);

        //year view on top of calendar
        TextView YearView = View_dialog_datetime.findViewById(R.id.YearView);

        //day and month view on top of calendar
        TextView MonthDate = View_dialog_datetime.findViewById(R.id.MonthDate);

        calendar = Calendar.getInstance();

        if (!selectDate.equals("0")) {
            //set select date/time
            date_Select = new Date(Long.parseLong(selectDate));
            time_select = new Date(Long.parseLong(selectTime));
            calendar.setTime(date_Select);
            calendar.setTime(time_select);

            getDate_Select = date_Select;

            dateMillis = calendar.getTimeInMillis();
            timeMillis = calendar.getTimeInMillis();

            getcalYear = calendar.get(calendar.YEAR);
            getHours = calendar.get(calendar.HOUR_OF_DAY);
            getMinutes = calendar.get(calendar.MINUTE);
            getAmpm = calendar.get(calendar.AM_PM);

            calYear = calendar.YEAR;
            calMonth = calendar.MONTH;
            calDay = calendar.DAY_OF_MONTH;

            hours = calendar.HOUR;
            Minutes = calendar.MINUTE;
            ampm = calendar.AM_PM;

        } else {

            getcalYear = calendar.get(calendar.YEAR);
            getHours = calendar.get(Calendar.HOUR_OF_DAY);
            getMinutes = calendar.get(Calendar.MINUTE);
            getAmpm = calendar.get(Calendar.AM_PM);

            calYear = Calendar.YEAR;
            calMonth = Calendar.MONTH;
            calDay = Calendar.DAY_OF_MONTH;

            hours = Calendar.HOUR;
            Minutes = Calendar.MINUTE;
            ampm = Calendar.AM_PM;

            getDate_Select = calendar.getTime();

            dateMillis = calendar.getTimeInMillis();
            timeMillis = calendar.getTimeInMillis();

        }

        //set text to year and date
        SimpleDateFormat simpleDate = new SimpleDateFormat("EEE , dd MMM", Locale.getDefault());
        YearView.setText(String.valueOf(getcalYear));
        MonthDate.setText(simpleDate.format(getDate_Select));

        // this class is set Today for date
        class TodayDecorator1 implements DayViewDecorator {
            private final CalendarDay today;
            private final Drawable backgroundDrawable;

            private TodayDecorator1() {
                today = CalendarDay.today();

                //set background for TODAY
                backgroundDrawable = context.getResources().getDrawable(R.drawable.xml_oval_border_pd3);
            }

            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return today.equals(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(backgroundDrawable);
            }
        }

        // time function
        int minvalue = 0;
        int maxvalue = 11;

        //scroller wheels for hour, minute and am/pm
        AbstractWheel hoursWheel = View_dialog_datetime.findViewById(R.id.hour);
        hoursWheel.setViewAdapter(new NumericWheelAdapter(context, minvalue, maxvalue, "%02d"));
        hoursWheel.setCyclic(true);

        AbstractWheel minsWheel = View_dialog_datetime.findViewById(R.id.mins);
        minsWheel.setViewAdapter(new NumericWheelAdapterMin(context, 0, 59, "%02d"));
        minsWheel.setCyclic(true);

        AbstractWheel amPmWheel = View_dialog_datetime.findViewById(R.id.ampm);
        ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<>(context, new String[]{"AM", "PM"});
        amPmWheel.setViewAdapter(ampmAdapter);
        amPmWheel.setCyclic(false);

        //set values to wheels
        hoursWheel.setCurrentItem(getHours);
        minsWheel.setCurrentItem(getMinutes);
        amPmWheel.setCurrentItem(getAmpm);

        //set on change listeners for all wheel
        hoursWheel.addChangingListener((wheel, oldValue, newValue) -> calendar.set(hours, newValue));
        minsWheel.addChangingListener((wheel, oldValue, newValue) -> calendar.set(Minutes, newValue));
        amPmWheel.addChangingListener((wheel, oldValue, newValue) -> calendar.set(ampm, newValue));

        //set calendar view params
        final MaterialCalendarView widget = View_dialog_datetime.findViewById(R.id.calendarView);
        widget.setTileHeightDp(30);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE);
        widget.state().edit().setMinimumDate(CalendarDay.today()).commit();
        widget.setCurrentDate(date_Select);
        widget.setSelectedDate(date_Select);

        //set selected date colour
        widget.setSelectionColor(context.getResources().getColor(R.color.primaryDark3));

        widget.removeDecorators();
        widget.addDecorator(new TodayDecorator1());

        //set apppointment sche going , hosting & notgoing
        if (apptCalList != null) {
            widget.addDecorator(new EventDecorator(apptCalList.get(0).getGoingArray(),
                    context.getResources().getColor(R.color.primaryDark4)));
            widget.addDecorator(new EventDecorator2(apptCalList.get(0).getUndecArray(),
                    context.getResources().getColor(R.color.orange)));
            widget.addDecorator(new EventDecorator3(apptCalList.get(0).getNotGoingArray(),
                    context.getResources().getColor(R.color.red)));
        }

        widget.setOnDateChangedListener((widget1, date, selected) -> {
            SimpleDateFormat DateSel = new SimpleDateFormat("EEE , dd MMM", Locale.getDefault());

            //set value for calendar
            calendar.set(calYear, date.getYear());
            calendar.set(calMonth, date.getMonth());
            calendar.set(calDay, date.getDay());

            //set year and date to top of calendar
            YearView.setText(String.valueOf(date.getYear()));
            MonthDate.setText(DateSel.format(date.getDate()));
        });

        //ok button
        dialog_datetime.setPositiveButton(R.string.ok_label, (dialog, which) -> {
            //get date + time long (this will be used as time as well)
            apptTime = calendar.getTimeInMillis();

            //format date + time to 2018-07-23 12:42:32 for "timestamp" format for API server
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String apptDateTimeStr = dateTimeFormat.format(apptTime);

            //format time to 10:00 AM based on time long
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            String apptTimeStr = timeFormat.format(apptTime);

            //set 0 to hour, minute, second and milli to get date out
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            //convert date calendar to long
            apptDate = calendar.getTimeInMillis();

            //format date to "Sat, 12 March 2018"
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
            String apptDateStr = dateFormat.format(apptDate);

            switch (from) {
                case "NewIndiAppt": //new indi appt
                    //set public static values
                    NewIndiExistApptActivity.apptDate = apptDate;
                    NewIndiExistApptActivity.apptTime = apptTime;

                    NewIndiExistApptActivity.schelog_date_long.setText(apptDateStr);
                    NewIndiExistApptActivity.schelog_time.setText(apptTimeStr);
                    break;

                case "NewGrpAppt": //new grp indi appt
                    //set public static values
                    NewGrpApptActivity.apptDate = apptDate;
                    NewGrpApptActivity.apptTime = apptTime;

                    NewGrpApptActivity.schelog_date_long.setText(apptDateStr);
                    NewGrpApptActivity.schelog_time.setText(apptTimeStr);
                    break;

                case "IndiScheLog": //existing indi appt
                    //set self status to "Host" for stanza
                    String pushMsg = context.getString(R.string.appt_date_changed);
                    String uniqueID = UUID.randomUUID().toString();
                    String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                    new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "", "",
                            "", "", "", "", apptDateStr, apptTimeStr,
                            "", "", uniqueID, "", "", apptID,
                            selfUsername, "appointment", "0", "2");


                    Appointment appt = databaseHelper.getAllApptDet(apptID);

                    databaseHelper.outgoingApptDateTime(jid, apptID, apptDate, apptTime, uniqueID);
                    databaseHelper.scheduleLocalNotification(apptID);

                    break;

                case "GrpScheLog": //existing indi appt
                    //set self status to "Host" for stanza
                    String pushMsgGrp = context.getString(R.string.appt_date_changed);
                    String uniqueIDGrp = UUID.randomUUID().toString();
                    String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                    new GroupChatStanza().GroupAppointment(jid, selfJid, uniqueIDGrp, "", "",
                            "", "", "", apptDateStr, apptTimeStr, "",
                            "", "", apptID, grpName, pushMsgGrp, "appointment",
                            apptDateTimeStr, "2");

                    databaseHelper.outgoingApptDateTime(jid, apptID, apptDate, apptTime, uniqueIDGrp);
                    databaseHelper.scheduleLocalNotification(apptID);

                    break;
            }
        }).setNegativeButton(R.string.cancel, null);

        AlertDialog datetime_dialog = dialog_datetime.create();
        datetime_dialog.show();
    }

    //alert dialog with edittext (for grp title and others) later will see can change out static or not
    public void editTextAlertDialog(Context context, String alertDialogTitle, String editText, String from,
                                    String jid, String apptID, boolean cancelable, Runnable positivebtn) {
        Dialog schelogTitle = new Dialog(context);

        LayoutInflater dialogTviewlayout = LayoutInflater.from(context);
        View dialogView = dialogTviewlayout.inflate(R.layout.dialog_twobtns, null);

        schelogTitle.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        schelogTitle.requestWindowFeature(Window.FEATURE_NO_TITLE);
        schelogTitle.setContentView(dialogView);
        schelogTitle.setCancelable(cancelable);
        schelogTitle.create();

        TextView dialogTitle = schelogTitle.findViewById(R.id.dialog2Title);
        TextView dialogMessage = schelogTitle.findViewById(R.id.dialog2Message);
        EditText dialog_edit_text = schelogTitle.findViewById(R.id.alert_dialog_edit_text);
        TextView dialog_negative = schelogTitle.findViewById(R.id.dialog_negative2);
        TextView dialog_positive = schelogTitle.findViewById(R.id.dialog_positive2);
        ImageView dialog_delete_btn = schelogTitle.findViewById(R.id.delete_btn);
        TextView dialog_num_editTxt = schelogTitle.findViewById(R.id.num_editTxt);

        dialogTitle.setVisibility(View.VISIBLE);
        dialog_delete_btn.setVisibility(View.VISIBLE);
        dialog_num_editTxt.setVisibility(View.VISIBLE);
        dialog_edit_text.setVisibility(View.VISIBLE);

        dialogTitle.setText(alertDialogTitle);
        dialog_edit_text.setText(editText);
        dialog_edit_text.requestFocus();
        schelogTitle.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog_positive.setTextColor(context.getResources().getColor(R.color.white));
        dialog_negative.setTextColor(context.getResources().getColor(R.color.primaryDark2));

        dialog_positive.setText(R.string.ok_label);
        dialog_negative.setText(R.string.cancel);

        dialog_delete_btn.setOnClickListener(v -> {
            dialog_edit_text.setText("");
        });

        int count1 = 24 - dialog_edit_text.length();
        dialog_num_editTxt.setText(String.valueOf(count1));

        dialog_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int countTxt = 24 - s.length();
                dialog_num_editTxt.setText(String.valueOf(countTxt));

            }
        });

        //set OnclickListener
        dialog_negative.setOnClickListener(v -> schelogTitle.dismiss());

        dialog_positive.setOnClickListener(v -> {
            String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);
            String editTextStr = dialog_edit_text.getText().toString().trim();

            switch (from) {
                case "IndiScheHolder":
                    if (editTextStr.length() != 0) {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            String pushMsg = context.getString(R.string.appt_title_changed);
                            String uniqueID = UUID.randomUUID().toString();

                            new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, editTextStr, "",
                                    "", "", "", "", "", "",
                                    "", "", uniqueID, "", "", apptID,
                                    selfUsername, "appointment", "0", "1");

                            databaseHelper.outgoingApptTitle(jid, apptID, editTextStr, uniqueID);
                            schelogTitle.dismiss();

                        } else {
                            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case "GrpScheHolder":
                    //send stanza for appt title
                    if (editTextStr.length() != 0) {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            String pushMsg = context.getString(R.string.appt_title_changed);
                            String uniqueID = UUID.randomUUID().toString();
                            String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                            new GroupChatStanza().GroupAppointment(jid, selfJid, uniqueID, editTextStr, "",
                                    "", "", "", "", "", "",
                                    "", "", apptID, GroupScheLog.roomName, pushMsg,
                                    "appointment", "", "1");

                            databaseHelper.outgoingApptTitle(jid, apptID, editTextStr, uniqueID);
                            schelogTitle.dismiss();

                        } else {
                            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case "GrpChatDetail":
                    if (positivebtn != null) {
                        if (editTextStr.length() != 0) {
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                //set new grp name from edittext

                                GroupChatDetail.newGrpName = editTextStr;
                                positivebtn.run();
                                schelogTitle.dismiss();

                            } else {
                                Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    break;

                case "Profile":
                    if (positivebtn != null) {
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            Profile.displayName = editTextStr;
                            positivebtn.run();
                            schelogTitle.dismiss();

                        } else {
                            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                default:
                    if (positivebtn != null) {
                        if (editTextStr.length() != 0) {
                            if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                                //set new grp name from edittext

                                GlobalVariables.string1 = editTextStr;
                                positivebtn.run();

                                schelogTitle.dismiss();

                            } else {
                                Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    break;
            }
        });

        schelogTitle.show();
    }

    //show popup for navigation (waze, gmaps, grab (soon), uber (soon))
    public void showNavigationPopup(final Context context, final String Lat, final String Long,
                                    int x, int y) {
        Activity activity = (Activity) context;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout viewGroup = activity.findViewById(R.id.popup_schedule);
        View layout = layoutInflater.inflate(R.layout.popup_scehdule_navigate, viewGroup);
        ImageButton wazeSchImgBtn = layout.findViewById(R.id.popup_schedule_waze);
        ImageButton gmSchImgBtn = layout.findViewById(R.id.popup_schedule_gm);
        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.START | Gravity.TOP, x, y);

        wazeSchImgBtn.setOnClickListener(view -> {
            if (Lat != null && !Lat.equals("")) {
                try {
                    String uri = "waze://?ll=" + Lat + "," + Long + "&navigate=yes";
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
                    context.startActivity(intent);
                }
            }
            popup.dismiss();
        });

        gmSchImgBtn.setOnClickListener(view -> {
            if (Lat != null && !Lat.equals("")) {
                try {
                    // Create a Uri from an intent string. Use the result to create an Intent.
                    Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + Lat + "," + Long);

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
            popup.dismiss();
        });
    }

    public void showChatLogPopup(final Activity activity, final String jid) {
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = activity.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.popup_chat, viewGroup);

        //get width of screen
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        //get popup width in pixels (margin = 20dp on both sides based on chatlogs xml design)
        float density = activity.getResources().getDisplayMetrics().density;
        float margin = 40 * density;
        int popupWidth = screenWidth - Math.round(margin);

        final PopupWindow popup = new PopupWindow(layout, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //get height of bottom bar

        //set height from bottom = 55dp (based on chatlog xml
        int y = (int) (65 * density);

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.BOTTOM, 0, y + getSoftButtonsBarHeight(activity));

        //Action when click
        //camera button
        ImageView cameraBtn = layout.findViewById(R.id.chat_popup_camera);
        cameraBtn.setOnClickListener(v -> {

            if (PackageManager.PERMISSION_DENIED == activity.checkCallingOrSelfPermission(Manifest.permission.CAMERA) ||
                    PackageManager.PERMISSION_DENIED == activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new PermissionHelper().CheckPermissions(activity, 1001, R.string.permission_txt);
                return;
            } else {
                Intent cameraintent = new CameraHelper().startCameraIntent(activity);

                cameraintent.putExtra("jid", jid);
                cameraintent.putExtra("from", "chat");

                activity.startActivity(cameraintent);
            }
            popup.dismiss();
        });

        //gallery button
        ImageView galleryBtn = layout.findViewById(R.id.chat_popup_medialib);
        galleryBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int permission3 = ActivityCompat.checkSelfPermission(activity, Manifest
                        .permission
                        .READ_EXTERNAL_STORAGE);
                if (permission3 == PackageManager.PERMISSION_DENIED) {
                    // We don't have permission so prompt the user
                    String[] PERMISSIONS_GALLERY = {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    };
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_GALLERY, 2);

                } else {
                    Intent intent = new Intent(activity, GalleryMainActivity.class);

                    intent.putExtra("title", "Select media");
                    intent.putExtra("mode", 1);
                    intent.putExtra("maxSelection", 8);
                    intent.putExtra("from", "chatmedia");
                    intent.putExtra("jid", jid);

                    activity.startActivity(intent);
                }
                popup.dismiss();
            }
        });

        //appointment btn - start new appt
        ImageView indiAppt = layout.findViewById(R.id.chat_popup_appoint);
        indiAppt.setOnClickListener(v -> {
            Intent intentShare = new Intent(activity, NewIndiExistApptActivity.class);
            intentShare.putExtra("jid", jid);

            activity.startActivity(intentShare);
            popup.dismiss();
        });

        //share contact
        ImageView shareContact = layout.findViewById(R.id.chat_popup_shareContact);
        shareContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PackageManager.PERMISSION_DENIED == activity.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {
                    new PermissionHelper().CheckPermissions(activity, 1007, R.string.permission_txt);
                    preferences.save(activity, "contactPermission", "shareContact");
                    preferences.save(activity, "dwn", jid);
                } else {

                    Intent ShareContact = new Intent(activity, ShareContactActivity.class);
                    ShareContact.putExtra("jid", jid);
                    activity.startActivity(ShareContact);
                }
                popup.dismiss();
            }
        });

        //share location
        ImageView shareLocation = layout.findViewById(R.id.chat_popup_shareLocation);
        shareLocation.setOnClickListener(v -> {
            Intent ShareMap = new Intent(activity, MapGps.class);

            //specify from
            ShareMap.putExtra("from", "chatLog");

            ShareMap.putExtra("jid", jid);
            activity.startActivity(ShareMap);
            popup.dismiss();
        });

        //share restaurant
        ImageView chat_popup_shareRes = layout.findViewById(R.id.chat_popup_shareRes);

        if (Home.showAllTabs) {
            chat_popup_shareRes.setOnClickListener(v -> {
                Intent ShareRes = new Intent(activity, FoodTabActivity.class);

                activity.startActivity(ShareRes);
                popup.dismiss();
            });
        } else {
            chat_popup_shareRes.setVisibility(View.INVISIBLE);
        }
    }

    //setting up dimensions for the popup selection for gallery or camera
    public void showCameraGalleryPopup(final Activity activity, final String fromCamera, final String
            fromGallery) {
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = activity.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_camera_gallery, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);

        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.BOTTOM, 0, getSoftButtonsBarHeight(activity));

        //Camera Button and check for permission
        final ImageView camera = layout.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE);
                int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                        .CAMERA);

                if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED) {
                    // We don't have permission so prompt the user

                    String[] PERMISSIONS_CAMERA_STORAGE = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    };

                    ActivityCompat.requestPermissions(activity, PERMISSIONS_CAMERA_STORAGE, 1);
                } else {
                    Intent cameraintent = new CameraHelper().startCameraIntent(activity);

                    cameraintent.putExtra("from", fromCamera);

                    activity.startActivity(cameraintent);
                }
                popup.dismiss();
            }
        });

        //Gallery Button and Check for permission
        ImageView popup_gallery_icon = layout.findViewById(R.id.popup_gallery_icon);
        popup_gallery_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission3 = ActivityCompat.checkSelfPermission(activity, Manifest.permission
                        .READ_EXTERNAL_STORAGE);

                if (permission3 == PackageManager.PERMISSION_DENIED) {
                    // We don't have permission so prompt the user
                    String[] PERMISSIONS_GALLERY = {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    };

                    ActivityCompat.requestPermissions(activity, PERMISSIONS_GALLERY, 2);
                } else {

//                    Intent cameraintent = new Intent(activity, ImageCropPreviewActivity.class);
//                    cameraintent.putExtra("from", fromGallery);
//                    activity.startActivity(cameraintent);
                    if (fromGallery.equals("UserProfileMedia") || fromGallery.equals("ProfilePicMedia") || fromGallery.equals("createGroupMedia") || fromGallery.equals("GroupDetailMedia")) {

                        Intent intent = new Intent(activity, GalleryMainActivity.class);
                        intent.putExtra("title", "Select media");
                        intent.putExtra("mode", 2);
                        intent.putExtra("maxSelection", 1);
                        intent.putExtra("from", fromGallery);
                        activity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(activity, GalleryMainActivity.class);
                        intent.putExtra("title", "Select media");
                        intent.putExtra("mode", 1);
//                        intent.putExtra("mode", 2);
                        intent.putExtra("maxSelection", 8);
                        intent.putExtra("from", fromGallery);
                        activity.startActivity(intent);
                    }

                }
                popup.dismiss();
            }

        });
    }

//    //    bottom sheet dialog for 2 buttons
//    public void bottomDialog2Btns(Activity activity, int btn1Title, Runnable btn1Function, int btn2Title, Runnable btn2Function) {
//        View viewInflate = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_2_btns, null);
//
//        BottomSheetDialog btmSheetDialog = new BottomSheetDialog(activity);
//        btmSheetDialog.setContentView(viewInflate);
//
//        Button bottom_sheet_btn1 = viewInflate.findViewById(R.id.bottom_sheet_btn1);
//        bottom_sheet_btn1.setText(activity.getString(btn1Title));
//        bottom_sheet_btn1.setOnClickListener(view -> {
//            btn1Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn2 = viewInflate.findViewById(R.id.bottom_sheet_btn2);
//        bottom_sheet_btn2.setText(activity.getString(btn2Title));
//        bottom_sheet_btn2.setOnClickListener(view -> {
//            btn2Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        btmSheetDialog.show();
//    }

//    public void bottomDialog3Btns(Activity activity, int btn1Title, Runnable btn1Function, int btn2Title,
//                                  Runnable btn2Function, int btn3Title, Runnable btn3Function) {
//        View viewInflate = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_3_btns, null);
//
//        BottomSheetDialog btmSheetDialog = new BottomSheetDialog(activity);
//        btmSheetDialog.setContentView(viewInflate);
//
//        Button bottom_sheet_btn1 = viewInflate.findViewById(R.id.bottom_sheet_btn1);
//        bottom_sheet_btn1.setText(activity.getString(btn1Title));
//        bottom_sheet_btn1.setOnClickListener(view -> {
//            btn1Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn2 = viewInflate.findViewById(R.id.bottom_sheet_btn2);
//        bottom_sheet_btn2.setText(activity.getString(btn2Title));
//        bottom_sheet_btn2.setOnClickListener(view -> {
//            btn2Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn3 = viewInflate.findViewById(R.id.bottom_sheet_btn3);
//        bottom_sheet_btn3.setText(activity.getString(btn3Title));
//        bottom_sheet_btn3.setOnClickListener(view -> {
//            btn3Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        btmSheetDialog.show();
//    }

//    public void bottomDialog4Btns(Activity activity, int btn1Title, Runnable btn1Function, int btn2Title,
//                                  Runnable btn2Function, int btn3Title, Runnable btn3Function,
//                                  int btn4Title, Runnable btn4Function) {
//        View viewInflate = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_4_btns, null);
//
//        BottomSheetDialog btmSheetDialog = new BottomSheetDialog(activity);
//        btmSheetDialog.setContentView(viewInflate);
//
//        Button bottom_sheet_btn1 = viewInflate.findViewById(R.id.bottom_sheet_btn1);
//        bottom_sheet_btn1.setText(activity.getString(btn1Title));
//        bottom_sheet_btn1.setOnClickListener(view -> {
//            btn1Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn2 = viewInflate.findViewById(R.id.bottom_sheet_btn2);
//        bottom_sheet_btn2.setText(activity.getString(btn2Title));
//        bottom_sheet_btn2.setOnClickListener(view -> {
//            btn2Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn3 = viewInflate.findViewById(R.id.bottom_sheet_btn3);
//        bottom_sheet_btn3.setText(activity.getString(btn3Title));
//        bottom_sheet_btn3.setOnClickListener(view -> {
//            btn3Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        Button bottom_sheet_btn4 = viewInflate.findViewById(R.id.bottom_sheet_btn4);
//        bottom_sheet_btn4.setText(activity.getString(btn4Title));
//        bottom_sheet_btn4.setOnClickListener(view -> {
//            btn4Function.run();
//            btmSheetDialog.dismiss();
//        });
//
//        btmSheetDialog.show();
//    }

    public void bottomDialog5Btns(Context context, String btn1Title, Runnable btn1Function, String btn2Title,
                                  Runnable btn2Function, String btn3Title, Runnable btn3Function,
                                  String btn4Title, Runnable btn4Function, String btn5Title, Runnable btn5Function) {
        LayoutInflater bDialog5layout = LayoutInflater.from(context);
        View viewInflate = bDialog5layout.inflate(R.layout.bottom_sheet_dialog_5_btns, null);

        BottomSheetDialog btmSheetDialog = new BottomSheetDialog(context);
        btmSheetDialog.setContentView(viewInflate);

        Button bottom_sheet_btn1 = viewInflate.findViewById(R.id.bottom_sheet_btn1);
        Button bottom_sheet_btn2 = viewInflate.findViewById(R.id.bottom_sheet_btn2);
        Button bottom_sheet_btn3 = viewInflate.findViewById(R.id.bottom_sheet_btn3);
        Button bottom_sheet_btn4 = viewInflate.findViewById(R.id.bottom_sheet_btn4);
        Button bottom_sheet_btn5 = viewInflate.findViewById(R.id.bottom_sheet_btn5);

        if (btn1Function == null) {
            bottom_sheet_btn1.setVisibility(View.GONE);
        }

        if (btn2Function == null) {
            bottom_sheet_btn2.setVisibility(View.GONE);
        }

        if (btn3Function == null) {
            bottom_sheet_btn3.setVisibility(View.GONE);
        }

        if (btn4Function == null) {
            bottom_sheet_btn4.setVisibility(View.GONE);
        }

        if (btn5Function == null) {
            bottom_sheet_btn5.setVisibility(View.GONE);
        }


        bottom_sheet_btn1.setText(btn1Title);
        bottom_sheet_btn1.setOnClickListener(view -> {
            if (btn1Function != null) {
                btn1Function.run();
            }
            btmSheetDialog.dismiss();
        });

        bottom_sheet_btn2.setText(btn2Title);
        bottom_sheet_btn2.setOnClickListener(view -> {
            if (btn2Function != null) {
                btn2Function.run();
            }
            btmSheetDialog.dismiss();
        });

        bottom_sheet_btn3.setText(btn3Title);
        bottom_sheet_btn3.setOnClickListener(view -> {
            if (btn3Function != null) {
                btn3Function.run();
            }
            btmSheetDialog.dismiss();
        });

        bottom_sheet_btn4.setText(btn4Title);
        bottom_sheet_btn4.setOnClickListener(view -> {
            if (btn4Function != null) {
                btn4Function.run();
            }
            btmSheetDialog.dismiss();
        });

        bottom_sheet_btn5.setText(btn5Title);
        bottom_sheet_btn5.setOnClickListener(view -> {
            if (btn5Function != null) {
                btn5Function.run();
            }
            btmSheetDialog.dismiss();
        });

        btmSheetDialog.show();
    }

    //function to show dialog (1 button) WITH ANIMATION ** later check what this wan to change
    public void dialog1Btn(Context context, String title, String message, int
            positiveLabel, int positiveColor, final Runnable positiveFunction, boolean isCancellable, boolean ImageAnimation) {

        Dialog dialog1Custom = new Dialog(context);
        LayoutInflater dialog1layout = LayoutInflater.from(context);
        View dialog1View = dialog1layout.inflate(R.layout.dialog_onebtn, null);

        dialog1Custom.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1Custom.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1Custom.setContentView(dialog1View);
        dialog1Custom.setCancelable(isCancellable);
        dialog1Custom.create();

        TextView dialog1Title = dialog1Custom.findViewById(R.id.dialog1Title);
        TextView dialog1Message = dialog1Custom.findViewById(R.id.dialog1Message);
        AppCompatImageView animaImg = dialog1Custom.findViewById(R.id.animaImg);
        TextView dialog_positive1 = dialog1Custom.findViewById(R.id.dialog_positive1);

        // title
        if (title != null) {
            dialog1Title.setText(title);
            dialog1Title.setVisibility(View.VISIBLE);
        }

        // message
        dialog1Message.setText(message);

        // animation imageView
        if (ImageAnimation) {
            animaImg.setImageResource(R.drawable.xml_anim_bg);
            ((Animatable) animaImg.getDrawable()).start();
            animaImg.setVisibility(View.VISIBLE);
        } else {
            dialog1Message.setGravity(CENTER);
        }
        // positive button text & color
        dialog_positive1.setText(positiveLabel);
        dialog_positive1.setTextColor(context.getResources().getColor(positiveColor));

        // positive onclick
        dialog_positive1.setOnClickListener(v -> {
            if (positiveFunction != null) {
                positiveFunction.run();
            }
            dialog1Custom.dismiss();
        });


        dialog1Custom.show();
    }

    //function to show dialog (2 button) Textview only
    public void dialog2Btns(Context context, String title, String message, int positiveText,
                            int negativeText, int Txt_positiveColor, int Txt_negativeColor,
                            final Runnable positiveFunction,
                            final Runnable negativeFunction, boolean isCancellable) {

        Dialog dialog2Custom = new Dialog(context);
        LayoutInflater dialog2layout = LayoutInflater.from(context);
        View dialog2View = dialog2layout.inflate(R.layout.dialog_twobtns, null);

        dialog2Custom.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog2Custom.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2Custom.setContentView(dialog2View);
        dialog2Custom.setCancelable(isCancellable);
        dialog2Custom.create();

        TextView dialog2Title = dialog2Custom.findViewById(R.id.dialog2Title);
        TextView dialog2Message = dialog2Custom.findViewById(R.id.dialog2Message);
        TextView dialog_negative2 = dialog2Custom.findViewById(R.id.dialog_negative2);
        TextView dialog_positive2 = dialog2Custom.findViewById(R.id.dialog_positive2);

        if (title != null) {
            dialog2Title.setText(title);
//            dialog2Title.setVisibility(View.VISIBLE);
        }
        dialog2Message.setVisibility(View.VISIBLE);
        dialog2Message.setText(message);

        //btn negative set function
        dialog_negative2.setText(negativeText);
        dialog_negative2.setTextColor(context.getResources().getColor(Txt_negativeColor));
//        dialog_negative2.setBackgroundColor(context.getResources().getColor(Bg_negativeColor));

        //btn positive set function
        dialog_positive2.setText(positiveText);
        dialog_positive2.setTextColor(context.getResources().getColor(Txt_positiveColor));
//        dialog_positive2.setBackgroundColor(context.getResources().getColor(Bg_positiveColor));

        //set OnclickListener
        dialog_negative2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeFunction != null) {
                    negativeFunction.run();
                }
                dialog2Custom.dismiss();
            }
        });

        dialog_positive2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveFunction.run();
                dialog2Custom.dismiss();
            }
        });

        dialog2Custom.show();
    }

    //function to show dialog (2 button) 2 EditView for add contact
    public void dialog2Btns2Eview(Context context, String title, String phoneName, String phoneNumber,
                                  Runnable positiveFunction, Runnable negativeFunction, boolean isCancellable) {

        Dialog dialog2Custom2T = new Dialog(context);
        LayoutInflater dialog2layout = LayoutInflater.from(context);
        View dialog2View = dialog2layout.inflate(R.layout.dialog_twobtns, null);

        dialog2Custom2T.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog2Custom2T.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2Custom2T.setContentView(dialog2View);
        dialog2Custom2T.setCancelable(isCancellable);
        dialog2Custom2T.create();
        dialog2Custom2T.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        TextView dialog2Title = dialog2Custom2T.findViewById(R.id.dialog2Title);
        EditText dialog_phoneName = dialog2Custom2T.findViewById(R.id.phoneName);
        EditText dialog_phoneNumber = dialog2Custom2T.findViewById(R.id.phoneNumber);
        TextView dialog_negative2 = dialog2Custom2T.findViewById(R.id.dialog_negative2);
        TextView dialog_positive2 = dialog2Custom2T.findViewById(R.id.dialog_positive2);
        ImageView delete_btn = dialog2Custom2T.findViewById(R.id.delete_btn1);
        TextView num_editTxt = dialog2Custom2T.findViewById(R.id.num_editTxt1);

        dialog_phoneName.setVisibility(View.VISIBLE);
        dialog_phoneNumber.setVisibility(View.VISIBLE);
        delete_btn.setVisibility(View.VISIBLE);
        num_editTxt.setVisibility(View.VISIBLE);
        dialog_phoneName.setText(phoneName);
        dialog_phoneName.requestFocus();

        dialog_phoneNumber.setText(phoneNumber);

        int count = 24 - dialog_phoneName.length();
        num_editTxt.setText(String.valueOf(count));

        dialog_phoneName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int countTxt = 24 - s.length();
                num_editTxt.setText(String.valueOf(countTxt));

            }
        });

        // set title
        if (title != null) {
            dialog2Title.setText(title);
//            dialog2Title.setVisibility(View.VISIBLE);
        }

        delete_btn.setOnClickListener(v -> {
            dialog_phoneName.setText("");
        });

        //btn negative set function
        dialog_negative2.setText(R.string.cancel);
        dialog_negative2.setTextColor(context.getResources().getColor(R.color.primaryDark2));
        dialog_negative2.setBackgroundColor(context.getResources().getColor(R.color.white));

        //btn positive set function
        dialog_positive2.setText(R.string.ok_label);
        dialog_positive2.setTextColor(context.getResources().getColor(R.color.white));
        dialog_positive2.setBackgroundColor(context.getResources().getColor(R.color.primaryDark2));

        //set OnclickListener
        dialog_negative2.setOnClickListener(v -> {
            if (negativeFunction != null) {
                negativeFunction.run();
            }
            dialog2Custom2T.dismiss();
        });

        dialog_positive2.setOnClickListener(v -> {
            if (positiveFunction != null) {
                //set contactName and contactNumber to global vars
                GlobalVariables.string1 = dialog_phoneName.getText().toString();

                positiveFunction.run();
            }
            dialog2Custom2T.dismiss();
        });

        dialog2Custom2T.show();
    }

    //function to show dialog (2 button) ImageView + TextView
    public void dialog2BtnsITView(Context context, String title, String message, int positiveText,
                                  int negativeText, Runnable positiveFunction,
                                  Runnable negativeFunction, boolean isCancellable) {

        Dialog dialog2Custom = new Dialog(context);
        LayoutInflater dialog2layout = LayoutInflater.from(context);
        View dialog2View = dialog2layout.inflate(R.layout.dialog_twobtns, null);

        dialog2Custom.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog2Custom.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2Custom.setContentView(dialog2View);
        dialog2Custom.setCancelable(isCancellable);
        dialog2Custom.create();

        TextView dialog2Title = dialog2Custom.findViewById(R.id.dialog2Title);
        TextView dialog2Message = dialog2Custom.findViewById(R.id.dialog2Message);
        TextView dialog_negative2 = dialog2Custom.findViewById(R.id.dialog_negative2);
        TextView dialog_positive2 = dialog2Custom.findViewById(R.id.dialog_positive2);

        if (title != null) {
            dialog2Title.setText(title);
//            dialog2Title.setVisibility(View.VISIBLE);
        }
        dialog2Message.setText(message);

        //btn negative set function
        dialog_negative2.setText(negativeText);
        dialog_negative2.setTextColor(context.getResources().getColor(R.color.primaryDark2));
        dialog_negative2.setBackgroundColor(context.getResources().getColor(R.color.white));

        //btn positive set function
        dialog_positive2.setText(positiveText);
        dialog_positive2.setTextColor(context.getResources().getColor(R.color.white));
        dialog_positive2.setBackgroundColor(context.getResources().getColor(R.color.primaryDark2));

        //set OnclickListener
        dialog_negative2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeFunction != null) {
                    negativeFunction.run();
                }
                dialog2Custom.dismiss();
            }
        });

        dialog_positive2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveFunction.run();
                dialog2Custom.dismiss();
            }
        });

        dialog2Custom.show();


    }

    // function to show dialog (2 button) TextView , checkbox
    public void dialog2BtnsCheckobxView(Context context, String title, String message, Runnable positiveFunction, Runnable negativeFunction, Boolean isCancellable) {
        Dialog dialogCheckbox = new Dialog(context);
        LayoutInflater checkboxlayout = LayoutInflater.from(context);
        View checkboxView = checkboxlayout.inflate(R.layout.dialog_twobtns, null);

        dialogCheckbox.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogCheckbox.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCheckbox.setContentView(checkboxView);
        dialogCheckbox.setCancelable(isCancellable);
        dialogCheckbox.create();

        TextView checkBox_Title = dialogCheckbox.findViewById(R.id.dialog2Title);
        TextView checkBox_Message = dialogCheckbox.findViewById(R.id.dialogTextView_Check);
        CheckBox checkBox_Check = dialogCheckbox.findViewById(R.id.dialog_checkBox);
        TextView dialog_negative2 = dialogCheckbox.findViewById(R.id.dialog_negative2);
        TextView dialog_positive2 = dialogCheckbox.findViewById(R.id.dialog_positive2);

        checkBox_Title.setVisibility(View.VISIBLE);
        checkBox_Message.setVisibility(View.VISIBLE);
        checkBox_Check.setVisibility(View.VISIBLE);

        checkBox_Title.setText(title);
        checkBox_Message.setText(message);
        dialog_negative2.setText(R.string.cancel);
        dialog_positive2.setText(R.string.allow);
        dialog_negative2.setTextColor(setResources(context, R.color.black));
        dialog_positive2.setTextColor(context.getResources().getColor(R.color.white));

        checkBox_Check.setOnClickListener(v -> {
            if (checkBox_Check.isChecked()) {
                check_agreed = checkBox_Check.isChecked();
                getA_btn(context, check_agreed);
            } else {
                check_agreed = checkBox_Check.isChecked();
                getA_btn(context, check_agreed);
            }
        });


        dialog_negative2.setOnClickListener(v -> {
            if (negativeFunction != null) {
                negativeFunction.run();
            }
            dialogCheckbox.dismiss();
        });

        dialog_positive2.setOnClickListener(v -> {


            if (positiveFunction != null) {
                positiveFunction.run();
            }
            dialogCheckbox.dismiss();
        });

        dialogCheckbox.show();
    }

    //getResources global function
    public int setResources(Context context, Integer color) {
        return context.getResources().getColor(color);
    }

    //function to check if user already checked "don't ask again"
    private void getA_btn(Context context, boolean check_agreed) {
        if (check_agreed) {
            preferences.save(context, "pushWhitelistCheck", "true");
        } else {
            preferences.save(context, "pushWhitelistCheck", "false");
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight(FragmentActivity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // A method to find height of the navigation bar
    public int getNavigationHeight(FragmentActivity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getScreenWidth(Activity activity) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;

        return displayMetrics.widthPixels;
    }

    public void setStatusBarColor(Activity activity, boolean isBackgroundLight, int statusBgColor) {
        Window window = activity.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (isBackgroundLight) {
            //background white
            if (Build.VERSION.SDK_INT >= 23) {
                //more than 23 set text color to grey
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                //set bg color
                window.setStatusBarColor(ContextCompat.getColor(activity, statusBgColor));

            } else { // below 23 set bg

                window.setStatusBarColor(ContextCompat.getColor(activity, R.color.grey8));
            }

        } else { // if background is dark set bg color
            window.setStatusBarColor(ContextCompat.getColor(activity, statusBgColor));
        }
    }

    public void closeKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    //function for deleting appt (checks if host or not)
    public void deleteAppt(Context context, String jid, String apptID, String grpName, boolean needCheckHost) {
        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
            //action for when click on delete scheroom alertdialog "ok"
            Runnable deleteApptConfirmAction = () -> {
                String selfJid = preferences.getValue(context, GlobalVariables.STRPREF_USER_ID);

                String uniqueID = UUID.randomUUID().toString();
                int canDelete = databaseHelper.deleteApptID(jid, apptID, needCheckHost, selfJid, uniqueID, false);

                if (canDelete == 0) { //can't delete, toast user
                    Toast.makeText(context, R.string.appt_host_delete, Toast.LENGTH_SHORT).show();
                } else { //deleted, pubsub to others
                    String pushMsg = context.getString(R.string.appt_deleted);
                    String selfUsername = preferences.getValue(context, GlobalVariables.STRPREF_USERNAME);

                    if (jid.length() == 12) { //indi
                        //push goes to chat since is deleting appt
                        new SingleChatStanza().SoappAppointmentStanza(jid, pushMsg, "",
                                "", "", "", "", "",
                                "nil", "", "", "", uniqueID,
                                "", "", apptID, selfUsername, "chat",
                                "0", "0");
                    } else { //grp

                        //push goes to chat since deleting appt
                        new GroupChatStanza().GroupAppointment(jid, selfJid, uniqueID,
                                "", "", "", "",
                                "", "nil", "", "",
                                "", "", apptID, grpName, pushMsg,
                                "chat", "", "0");
                    }
                }
            };

            dialog2Btns(context, context.getString(R.string.sche_delete_title),
                    context.getString(R.string.sche_delete_msg), R.string.delete,
                    R.string.cancel, R.color.red, R.color.black, deleteApptConfirmAction, null, true);
        } else {
            Toast.makeText(context, R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
