package com.soapp.food.food_detail.booking;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RestaurantBookingInfo;
import com.soapp.SoappApi.Interface.RestaurantBooking;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.ResMyBookingModel;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.DateTime.date.DatePickerDialog;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.soapp_tab.booking.ResBookingActivity;
import com.soapp.soapp_tab.bookinglist.ResBookingsController;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.SmackHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Response;

public class FoodDetailBookingFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    //card view
    Button btn_booking;

    //seekbar
    SeekBar sb_pax;

    EditText selectPax;
    TextView bookingDateTxt, bookingTimeTxt, titleNoPromo, tv_seek_bar_progress;
    CardView booking_date_btn, booking_time_btn;
    ImageView booking_profile_img, res_cam_btn;
    String resName, resPropic, resID, resJID, resLoc, resState, resLat, resLon, resPhone, dateStr,
            timeStr, thepaxnum;
    String bookingId;
    String paxTxt;
    Button booking_btn;
    List<ResMyBookingModel> resBookingList = new ArrayList<>();
    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    long bookingDate, bookingTime;
    private int mYear, mMonth, mDay, mHour, mMinute, pax;
    private DatePickerDialog dpd;
    private View view;
    private String[] numbers = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private UIHelper uiHelper = new UIHelper();

    private Runnable createBookingRunnable = new Runnable() {
        public void run() {
            String promotion_id = "";
            thepaxnum = selectPax.getText().toString();
            if (!thepaxnum.equals("")) {
                pax = Integer.parseInt(thepaxnum);
            }
            //API method
            new AsyncResBooking().execute(dateStr, timeStr, pax + "",
                    promotion_id, resID, "", 2 + "", "0");


        }
    };

    private Runnable retryBookingPositive = new Runnable() {
        public void run() {
            int ctbookingAttempts = databaseHelper.getBookingAttempts(bookingId);
            int countbookingAttempts = ctbookingAttempts - 1;

            String promotion_id = "";

            //API method
            new AsyncResBooking().execute(dateStr, timeStr, pax + "", promotion_id,
                    resID, "", countbookingAttempts + "", "1");
        }
    };
    private Runnable retryBookingReviewPositive = new Runnable() {
        public void run() {
//            finish();
        }
    };

    private Runnable goToBookingList = new Runnable() {
        public void run() {

//            databaseHelper.getBookingInfoBasedResID(resID);
            Intent intent = new Intent(getContext(), ResBookingsController.class);
            intent.putExtra("resid", resID);
            startActivity(intent);

//            String promotion_id = "";
//            thepaxnum = selectPax.getText().toString();
//            if (!thepaxnum.equals("")) {
//                pax = Integer.parseInt(thepaxnum);
//            }
//

        }
    };
    private Dialog dialog;
    private String mainCuisine;
    private String overallRating;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.food_detail_booking, container, false);

        titleNoPromo = view.findViewById(R.id.titleNoPromo);
        selectPax = view.findViewById(R.id.selectPax);

        //set onclick listeners to buttons
        booking_date_btn = view.findViewById(R.id.booking_date_btn);
        bookingDateTxt = view.findViewById(R.id.bookingDateTxt);
        booking_date_btn.setOnClickListener(this);
        booking_time_btn = view.findViewById(R.id.booking_time_btn);
        bookingTimeTxt = view.findViewById(R.id.bookingTimeTxt);
        booking_time_btn.setOnClickListener(this);
        btn_booking = view.findViewById(R.id.btn_booking);
        btn_booking.setOnClickListener(this);

        //text view
        tv_seek_bar_progress = view.findViewById(R.id.tv_seek_bar_progress);

        //seekbar
        sb_pax = view.findViewById(R.id.sb_pax);
        sb_pax.setMax(50);
        sb_pax.setProgress(2);
        pax = 2;
        tv_seek_bar_progress.setText(String.valueOf(2));
        sb_pax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i >= 2) {
                    pax = i;
                    tv_seek_bar_progress.setText(String.valueOf(i));
                } else {
                    seekBar.setProgress(2);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //get resID from intent extras
//        resID = getActivity().getIntent().getStringExtra("infoid");
//        resJID = getActivity().getIntent().getStringExtra("resJID");
//        resName = getActivity().getIntent().getStringExtra("restitle");
//        resPropic = getActivity().getIntent().getStringExtra("image_id");
//        resLoc = getActivity().getIntent().getStringExtra("resLoc");
//        resState = getActivity().getIntent().getStringExtra("resState");
//        resPhone = getActivity().getIntent().getStringExtra("resPhone");
//        resLon = getActivity().getIntent().getStringExtra("resLon");
//        resLat = getActivity().getIntent().getStringExtra("resLat");

        final GridView gridPax = view.findViewById(R.id.gridPax);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.food_detail_booking_gridview, numbers);
        gridPax.setSelected(true);
        gridPax.setAdapter(adapter);
        gridPax.setOnItemClickListener(this);

        resID = FoodDetailLog.resID;
        resJID = FoodDetailLog.ownerJID;
        resName = FoodDetailLog.resTitle;
        resPropic = FoodDetailLog.resPropic;
        resLoc = FoodDetailLog.resLoc;
        resState = FoodDetailLog.resState;
        resPhone = FoodDetailLog.foodDetailInfo[7];
        resLon = FoodDetailLog.foodDetailInfo[21];
        resLat = FoodDetailLog.foodDetailInfo[20];
        mainCuisine = FoodDetailLog.foodDetailInfo[2];
        overallRating = FoodDetailLog.foodDetailInfo[4];

        return view;
    }


    @Override
    public void onClick(View v) {
        Calendar c = Calendar.getInstance();

        switch (v.getId()) {
            case (R.id.booking_date_btn):
                // Get Current Date
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                dpd = DatePickerDialog.newInstance(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        dateStr = String.format(Locale.ENGLISH, "%02d",
                                (dayOfMonth)) + "/" + String.format(Locale.ENGLISH,
                                "%02d", (monthOfYear + 1)) + "/" + year % 100;
                        bookingDateTxt.setText(dateStr);
                    }
                }, mYear, mMonth, mDay);

//                dpd = DatePickerDialog.newInstance(getContext(), new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
//                        dateStr = String.format(Locale.ENGLISH, "%02d",
//                                (dayOfMonth)) + "/" + String.format(Locale.ENGLISH,
//                                "%02d", (monthOfYear + 1)) + "/" + year % 100;
//                        bookingDateTxt.setText(dateStr);
//                    }
//                } ,mYear, mMonth, mDay);

                dpd.setMinDate(c);
                dpd.setThemeDark(false);
                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
                break;

            case (R.id.booking_time_btn):
                // Get Current Time
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.style_time_picker_dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        timeStr = String.format(Locale.ENGLISH, "%01d:%02d %s",
                                hourOfDay % 12, minute, hourOfDay <= 12 ? "AM" : "PM");

                        bookingTimeTxt.setText(timeStr);

                    }
                }, mHour, mMinute, false);


                timePickerDialog.show();
//                timePickerDialog.setIcon();
                timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primaryDark2));
                timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(15);
                timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryDark2));
                timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(15);
                break;

            case (R.id.btn_booking):
                if (dateStr == null || timeStr == null) { //no time selected
//
//                    String msgTxt = "test test tes test tes test test test test test";
//                    new UIHelper().dialog1Btn(getContext()
//                            , 0
//                            , getString(R.string.booking_no_date_msg)
//                            , R.string.ok_label
//                            , R.color.primaryDark3
//                            , null
//                            , true ,false);

                    uiHelper.dialog1Btn(getContext(), getString(R.string.booking_no_date_title),
                            getString(R.string.booking_no_date_msg), R.string.ok_label,
                            R.color.black, null, true, false);

                } else if (pax == 0) {
                    uiHelper.dialog1Btn(getContext(), getString(R.string.booking_no_guest_title),
                            getString(R.string.booking_no_guest_msg), R.string.ok_label,
                            R.color.black, null, true, false);

                } else { //got time selected
                    long dateTimeDiff;

                    DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yy h:mm a", Locale
                            .ENGLISH);
                    try {
                        Date convertedDateTime = dateTimeFormat.parse(dateStr + " " + timeStr);
                        dateTimeDiff = convertedDateTime.getTime() - System.currentTimeMillis();
                    } catch (ParseException e) {
                        dateTimeDiff = 0;
                    }

                    if (dateTimeDiff < 10800000) { //selected booking time is less than 3 hours away
                        uiHelper.dialog1Btn(getContext(), getString(R.string.booking_confirmation),
                                getString(R.string.booking_confirmation_3_hours), R.string.ok_label,
                                R.color.black, null, true, false);

                    } else { //selected booking time ok
                        if (SmackHelper.getXMPPConnection() != null && SmackHelper.getXMPPConnection().isAuthenticated()) {
                            //get booking date in long
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                            try {
                                Date convertedDate = dateFormat.parse(dateStr);
                                bookingDate = convertedDate.getTime();

                            } catch (ParseException e) {
                                bookingDate = -1;
                            }

                            //get booking time in long
                            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                            timeStr.replace("am", "AM").replace("pm", "PM");
                            try {
//                                timeStr.toUpperCase();
                                Date convertedDate = timeFormat.parse(timeStr);
                                bookingTime = convertedDate.getTime();


                            } catch (ParseException e) {
                                bookingTime = -1;
                            }

                            if (pax == 1) {
                                paxTxt = "guest";
                            } else {
                                paxTxt = "guests";
                            }

                            //check if res booking already exists in sqlite
                            if (databaseHelper.getBookingExistBasedOnResID(resID) == 0) { //not exists, create new
                                // set the msg
                                String dialogMsg = "You are about to book for " + pax + " " + paxTxt + " at " + resName + " on " + dateStr + " " + timeStr;
                                uiHelper.dialog2Btns(getContext()
                                        , null    // can be null
                                        , dialogMsg             // string message
                                        , R.string.ok_label     // int negative text
                                        , R.string.cancel       // int positive text
                                        , R.color.primaryDark3  // int negative text color
                                        , R.color.white         // int positive text color
                                        , createBookingRunnable // runnable positive function
                                        , null   // runnable negative function
                                        , true);
                            } else {
                                // set the msg
                                String dialogMsg = "You are about to book for " + pax + " " + paxTxt + " at " + resName + " on " + dateStr + " " + timeStr;
                                uiHelper.dialog2Btns(getContext()
                                        , null    // can be null
                                        , dialogMsg             // string message
                                        , R.string.ok_label     // int negative text
                                        , R.string.cancel       // int positive text
                                        , R.color.primaryDark3  // int negative text color
                                        , R.color.white         // int positive text color
                                        , createBookingRunnable // runnable positive function
                                        , null   // runnable negative function
                                        , true);

                            }
                        } else {
                            Toast.makeText(getContext(), R.string.xmpp_waiting_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    void editBooking() {
        { //booking exists, warn user on remaining attempt (total 2 attempts only)
            //check remaining booking attempts
            int bookingAttempts = databaseHelper.getBookingAttempts(bookingId);

            if (bookingAttempts == 0) { //no more booking attempts
                uiHelper.dialog1Btn(getContext(), getString(R.string.booking_confirmation),
                        getString(R.string.booking_no_attempts), R.string.ok_label,
                        R.color.black, null, true, false);

            } else if (bookingAttempts > 0) { //still got booking attempts
                String msg;

                if (bookingAttempts == 1) { //use singular if 1
                    msg = getString(R.string.booking_retry_1) + " " + bookingAttempts + " " +
                            getString(R.string.attempt_left) + " " + getString(R.string.booking_retry_2);
                } else {
                    msg = getString(R.string.booking_retry_1) + " " + bookingAttempts + " " +
                            getString(R.string.attempts_left) + " " + getString(R.string.booking_retry_2);
                }

                uiHelper.dialog2Btns(getContext(), getString(R.string.booking_confirmation),
                        msg, R.string.ok_label, R.string.cancel, R.color.white, R.color.black,
                        retryBookingPositive, null, true);

            } else { //booking attempt < 0, means first time booking

                uiHelper.dialog1Btn(getContext(), getString(R.string.booking_confirmation), getString(R.string.booking_sent_review),
                        R.string.ok_label, R.color.black, null, true, true);

//                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
//                alertDialog.setTitle(R.string.booking_confirmation);
//                alertDialog.setMessage(getString(R.string.booking_sent_review));
//                alertDialog.show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pax = Integer.parseInt(((TextView) view).getText().toString());
        selectPax.setText("");
        selectPax.setFocusable(true);
    }

    public void resBookingPost(String booking_date, String booking_time,
                               final String pax, final String promotion_id, String restaurant_id, String
                                       special_request, final String bookingAttempt, final String
                                       isRetry) {
        String access_token = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(),
                GlobalVariables.STRPREF_ACCESS_TOKEN);
        RestaurantBookingInfo model = new RestaurantBookingInfo("book", booking_date, booking_time,
                pax, promotion_id, restaurant_id, special_request);

        //build retrofit
        RestaurantBooking client = RetrofitAPIClient.getClient().create(RestaurantBooking.class);
        retrofit2.Call<RestaurantBookingInfo> call = client.resBook(model, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantBookingInfo>() {

            @Override
            public void onResponse(Call<RestaurantBookingInfo> call, Response<RestaurantBookingInfo> response) {
                if (!response.isSuccessful()) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(getContext(), R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT)
                            .show();

                    new MiscHelper().retroLogUnsuc(response , "resBookingPost " , "JAY");

                    return;
                }

                bookingId = response.body().getBooking_id();
                //first time booking, set remaining attempts to 2
                databaseHelper.outgoingResBooking(bookingId, resID, bookingDate, bookingTime, pax + "",
                        promotion_id, resPropic, resLat, resLon, resName, resLoc, resState,
                        resPhone, Integer.parseInt(bookingAttempt), resJID, mainCuisine, overallRating);

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (isRetry.equals("1")) { //show remaining attempt and update previous UI
                    String msg;

                    if (bookingAttempt.equals("1")) { //only use singular for 1
                        msg = getString(R.string.booking_accept_status) + " " + bookingAttempt + " " + getString(R.string.attempt_left);
                    } else { //other than 1 use plural
                        msg = getString(R.string.booking_accept_status) + " " + bookingAttempt + " " + getString(R.string.attempts_left);
                    }

                    new UIHelper().dialog1Btn(getContext(), getString(R.string.thanks), msg,
                            R.string.ok_label, R.color.black, retryBookingReviewPositive, false, false);
                } else { //just toast user and finish activity

                    Intent intent = new Intent(getContext(), ResBookingActivity.class);
                    intent.putExtra("resID", resID);
                    intent.putExtra("bookingid", bookingId);

                    startActivity(intent);

                    Toast.makeText(getContext(), R.string.booking_sent_review, Toast
                            .LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<RestaurantBookingInfo> call, Throwable t) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                new MiscHelper().retroLogFailure(t , "resBookingPost " , "JAY");
                Toast.makeText(getContext(), R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void putArguments(Bundle args) {
        resID = args.getString("resID");
        resJID = args.getString("resJID");
        resName = args.getString("restitle");
        resPropic = args.getString("image_id");
        resLoc = args.getString("resLoc");
        resState = args.getString("resState");
        resPhone = args.getString("resPhone");
        resLon = args.getString("resLon");
        resLat = args.getString("resLat");
    }

    private class AsyncResBooking extends android.os.AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = ProgressDialog.show(getContext(), null, getString(R.string
                    .booking_progress));
        }

        @Override
        protected Void doInBackground(String... params) {
            resBookingPost(params[0], params[1], params[2], params[3], params[4], params[5],
                    params[6], params[7]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
