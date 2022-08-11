package com.soapp.soapp_tab.booking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RestaurantBookingInfo;
import com.soapp.SoappApi.Interface.RestaurantCancel;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.SoappModel.ResMyBookingModel;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.global.sharing.SharingController;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.joiners.BookingDetails;

import net.glxn.qrgen.android.QRCode;

import java.security.MessageDigest;
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
import androidx.lifecycle.ViewModelProviders;
import retrofit2.Call;
import retrofit2.Response;
/* Created by ash on 17/01/2018. */

public class ResBookingActivity extends BaseActivity implements View.OnClickListener {
    //basics
    private UIHelper uiHelper = new UIHelper();

    //public static for updating UI when biz owner confirms booking
    public static String myBookingQRCode;
    public static TextView mybookingstatus;
    public static ImageView img_qr_code, booking_share;
    public static List<ResMyBookingModel> list = new ArrayList<>();
    static ProgressDialog progressDialog;
    Button btn_more, btn_delete;
    ImageView mybooking_profile_img, mybooking_res_location, mybooking_phone, b_backBtn;
    String resID, sharedJID, ownerJID, resTitle, resPropic, resLoc, resState, resLat, resLon,
            resPhone, dateStr, timeStr, bookerJID, PhoneNumber, pax, promo;
    int bookingStatus;
    TextView mybookingbooker, mybookingtime, mybookingdate, mybookingpax, mybookingpromo,
            restaurantname;
    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private Runnable actionCallResPositive = new Runnable() {
        public void run() {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + PhoneNumber));

            if (androidx.core.app.ActivityCompat.checkSelfPermission(Soapp.getInstance().getApplicationContext(),
                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(callIntent);
        }
    };
    private TextView mybooking_dateMonth;
    private TextView mybooking_dateDay;
    private TextView mybooking_title;
    private ImageView mybooking_qr, mybooking_share, mybooking_statusColor;
    private TextView mybooking_statusDetails;
    private TextView mybooking_dateDayName;
    private TextView mybooking_dateFull;
    private TextView mybooking_time;
    private TextView mybooking_pax;
    private TextView mybooking_promoCode;
    private TextView mybooking_tableNo;
    private View mybooking_mainLayout;
    private BookingDetails bookDetail;
    private String bookingid;
    private ImageView mybooking_cancel;
    private Runnable CancelOK = new Runnable() {
        @Override
        public void run() {
            {
                //only need send stanza to bizUser if not
                // expired/rejected by owner (3, 4)
                if (bookingStatus < 3) {
                    new AsyncResBookingCancel().execute(bookingid);


                } else { //expired/rejected, just clear sqlite
                    databaseHelper.outgoingCancelSelfResBooking(bookingid);

                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    Toast.makeText(ResBookingActivity.this, getString(R
                            .string.booking_cancelled_success), Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.soapp_mybooking_activity);

        //get resID from intent extra
        bookingid = getIntent().getStringExtra("bookingid");
        resID = getIntent().getStringExtra("resID");
        sharedJID = getIntent().getStringExtra("sharedJID");

        initUI();

        ResBookingVM vm = ViewModelProviders.of(this).get(ResBookingVM.class);
        vm.setBookingId(bookingid);
        vm.init();
        vm.getBookingInfo().observe(this, bookingLists -> {

            if (bookingLists != null) {
                bookDetail = bookingLists;
                setUI(bookDetail);
            }

        });
    }

    private void initUI() {
        //set onclick listeners to buttons
//            mybooking_res_cam_btn = (ImageView) findViewById(R.id.mybooking_res_cam_btn);
        mybooking_dateMonth = findViewById(R.id.mybooking_dateMonth);
        mybooking_dateDay = findViewById(R.id.mybooking_dateDay);
        mybooking_title = findViewById(R.id.mybooking_title);
        mybooking_qr = findViewById(R.id.mybooking_qr);
        mybooking_statusColor = findViewById(R.id.mybooking_statusColor);
        mybooking_statusDetails = findViewById(R.id.mybooking_statusDetails);
        mybooking_dateDayName = findViewById(R.id.mybooking_dateDayName);
        mybooking_dateFull = findViewById(R.id.mybooking_dateFull);
        mybooking_time = findViewById(R.id.mybooking_time);
        mybooking_pax = findViewById(R.id.mybooking_pax);
        mybooking_promoCode = findViewById(R.id.mybooking_promoCode);
        mybooking_tableNo = findViewById(R.id.mybooking_tableNo);
        mybooking_mainLayout = findViewById(R.id.mybooking_mainLayout);
        mybooking_time = findViewById(R.id.mybooking_time);
        mybooking_share = findViewById(R.id.mybooking_btn2);
        mybooking_cancel = findViewById(R.id.mybooking_btn1);

        mybooking_share.setOnClickListener(this);
        mybooking_cancel.setOnClickListener(this);
    }

    private void setUI(BookingDetails data) {
        resID = data.getBooking().getBookingResId();
        resTitle = data.getRestaurant().getResTitle();
        resPropic = data.getRestaurant().getResImageUrl();
        resLat = data.getRestaurant().getResLatitude();
        resLon = data.getRestaurant().getResLongitude();
        dateStr = String.valueOf(data.getBooking().getBookingDate());
        timeStr = String.valueOf(data.getBooking().getBookingTime());
        pax = String.valueOf(data.getBooking().getBookingPax());
        promo = data.getBooking().getBookingPromo();


        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(data.getBooking().getBookingDate());
        DateFormat timeformat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        String timeStr = timeformat.format(data.getBooking().getBookingTime());

        String monthName = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);

        mybooking_dateMonth.setText(monthName);
        mybooking_dateDay.setText(String.valueOf(date.get(Calendar.DATE)));
        mybooking_title.setText(data.getRestaurant().getResTitle());

        Drawable statusDotResource;
        String statusFull;
        bookingStatus = data.getBooking().getBookingStatus();
        switch (bookingStatus) {
            case -1:
                statusDotResource = this.getDrawable(R.drawable.ic_black_dot);
                statusFull = "Cancelled";
                break;
            case 0:
                statusDotResource = this.getDrawable(R.drawable.ic_grey_dot_42px);
                statusFull = "GG";
                break;
            case 1:
                statusDotResource = this.getDrawable(R.drawable.ic_green_dot_42px);
                statusFull = "All set";
                break;
            case 2:
                statusDotResource = this.getDrawable(R.drawable.ic_orange_dot);
                statusFull = "Pending";
                break;
            case 3:
                statusDotResource = this.getDrawable(R.drawable.ic_red_dot);
                statusFull = "Rejected";
                break;
            case 4:
                statusDotResource = this.getDrawable(R.drawable.ic_black_dot);
                statusFull = "Expired";
                break;
            default:
                statusDotResource = this.getDrawable(R.drawable.ic_black_dot);
                statusFull = "Pending";
        }

        GlideApp.with(this)
                .asDrawable()
                .load(statusDotResource)
                .placeholder(R.drawable.ic_black_dot)
                .into(mybooking_statusColor);

        mybooking_statusDetails.setText(statusFull);


        String dayname = String.format("%s,", date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK));
        mybooking_dateDayName.setText(dayname);
        String dateFull = String.format("%s %d", date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK), date.get(Calendar.DATE));
        mybooking_dateFull.setText(dateFull);
        mybooking_time.setText(timeStr);

        mybooking_pax.setText(data.getBooking().getBookingPax().toString());
        mybooking_promoCode.setText(data.getBooking().getBookingPromo());


        if (data.getBooking().getBookingQRCode() != null && !data.getBooking().getBookingQRCode().isEmpty()) {
            myBookingQRCode = data.getBooking().getBookingQRCode();
            GlideApp.with(this)
                    .asBitmap()
                    .load(QRCode.from(data.getBooking().getBookingQRCode()).bitmap())
                    .placeholder(R.drawable.qr)
                    .into(mybooking_qr);
        }

        BitmapTransformation transformation = new BitmapTransformation() {
            @Override
            protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                return TransformationUtils.rotateImage(toTransform, 90);
            }

            @Override
            public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

            }
        };


        GlideApp.with(this)
                .as(BitmapDrawable.class)
                .load(data.getRestaurant().getResImageUrl())
                .into(new SimpleTarget<BitmapDrawable>() {
                    @Override
                    public void onResourceReady(@NonNull BitmapDrawable resource, @Nullable Transition<? super BitmapDrawable> transition) {
                        Bitmap bitmap = resource.getBitmap();
                        resource = new BitmapDrawable(getResources(), TransformationUtils.rotateImage(bitmap, 270));

                        mybooking_mainLayout.setBackground(resource);
                    }

                });

    }

    private void finishActivity() {
        Toast.makeText(ResBookingActivity.this, getString(R.string.booking_not_exist), Toast.LENGTH_SHORT).show();

        finish();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        //populate booking details from indiScheList
//        if (list != null && list.size() > 0) {
//            ownerJID = list.get(0).getResJID();
//            resTitle = list.get(0).getResTitle();
//            resPropic = list.get(0).getResImgURL();
//            resLoc = list.get(0).getResLoc();
//            resState = list.get(0).getResState();
//            resPhone = list.get(0).getResPhone();
//            resLon = list.get(0).getResLon();
//            resLat = list.get(0).getResLat();
//            bookerJID = list.get(0).getBookerJID();
//            myBookingQRCode = list.get(0).getBookingQRCode();
//
//            GlideApp.with(ResBookingActivity.this)
//                    .asBitmap()
//                    .load(resPropic)
//                    .placeholder(R.drawable.food_default)
//                    .transition(BitmapTransitionOptions.withCrossFade())
//                    .into(mybooking_profile_img);
//
//            restaurantname.setText(resTitle);
//
//            if (resPhone != null && !resPhone.equals("null")) {
//                PhoneNumber = resPhone;
//                mybooking_phone.setOnClickListener(this);
//            } else {
//                PhoneNumber = "-";
//                mybooking_phone.setVisibility(View.GONE);
//            }
//            mybookingbooker.setText(databaseHelper.getNameFromContactRoster(bookerJID));
//
//            long bookingDate = list.get(0).getBookingDate();
//            long bookingTime = list.get(0).getBookingTime();
//
//            if (bookingDate != 0) {
//                DateFormat dateformat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
//                dateStr = dateformat.format(bookingDate);
//
//                mybookingdate.setText(dateStr);
//            }
//
//            if (bookingTime != 0) {
//                DateFormat timeformat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
//                timeStr = timeformat.format(bookingTime);
//
//                mybookingtime.setText(timeStr);
//            }
//
//            String bookingStatusStr;
//
//            //check for booking status
//            bookingStatus = list.get(0).getBookingStatus();
//
//            if (bookingStatus < 4) { //current status not expired yet
//                //check if booking already expired as of now
//                if (!isTimeDiffOK(1)) { //expired
//                    //update booking status to "expired" = 4
//                    bookingStatus = 4;
//                    databaseHelper.updateSelfResBookingStatus(resID, 4);
//
//                    bookingStatusStr = getString(R.string.booking_expired);
//
//                } else { //booking not expired yet as of now
//                    switch (bookingStatus) {
//                        case 1: //accepted by biz owner
//                            bookingStatusStr = getString(R.string.booking_accepted);
//                            break;
//
//                        case 2: //pending action from biz owner
//                            bookingStatusStr = getString(R.string.booking_pending);
//                            break;
//
//                        case 3: //rejected by biz owner
//                            bookingStatusStr = getString(R.string.booking_declined);
//                            break;
//
//                        default:
//                            bookingStatusStr = getString(R.string.booking_pending);
//                            break;
//                    }
//                }
//            } else { //already expired
//                bookingStatusStr = getString(R.string.booking_expired);
//            }
//            mybookingstatus.setText(bookingStatusStr);
//
//            //set pax and promo
//            pax = list.get(0).getPax();
//            promo = list.get(0).getPromo();
//            mybookingpax.setText(pax);
//            if (promo != null && !promo.matches("")) {
//                mybookingpromo.setText(promo);
//            }
//
//            //generate qr image based on qr code string
//            String userJid = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(),
//                    GlobalVariables.STRPREF_USER_ID);
//
//            //self is booker, show share and more buttons, hide delete button
//            if (bookerJID.equals(userJid)) {
//                btn_delete.setVisibility(View.GONE);
//
//                btn_more.setVisibility(View.VISIBLE);
//                btn_more.setOnClickListener(this);
//
//                //only show share button if status is accepted
//                if (bookingStatus == 1) {
//                    booking_share.setVisibility(View.VISIBLE);
//                } else { //status not accepted, hide share button
//                    booking_share.setVisibility(View.GONE);
//                }
//
//            } else { //self is NOT booker, hide share and more buttons, show delete button
//                btn_delete.setVisibility(View.VISIBLE);
//                btn_delete.setOnClickListener(this);
//
//                booking_share.setVisibility(View.GONE);
//                btn_more.setVisibility(View.GONE);
//            }
//
//            //check if got qr code only try to generate
//            if (myBookingQRCode != null && !myBookingQRCode.equals("")) {
//                Bitmap myBitmap = QRCode.from(myBookingQRCode).bitmap();
//                img_qr_code.setImageBitmap(myBitmap);
//
//            } else { //no QR code - means either pending or rejected/expired
//                img_qr_code.setImageResource(R.drawable.qr);
//            }
////            setupToolbar();
//
//            //set restaurant title
//            setTitle(resTitle);
//        }
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.mybooking_btn2):
                Intent shareIntent = new Intent(ResBookingActivity.this, SharingController.class);

                shareIntent.putExtra("from", "bookingChat");
                shareIntent.putExtra("bookingQR", myBookingQRCode);

                shareIntent.putExtra("bookingid", bookingid);
                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("resLat", resLat);
                shareIntent.putExtra("resLon", resLon);
                shareIntent.putExtra("bookingDate", dateStr);
                shareIntent.putExtra("bookingTime", timeStr);
                shareIntent.putExtra("pax", pax);
                shareIntent.putExtra("promo", promo);

                startActivity(shareIntent);
                break;

            case (R.id.mybooking_btn1):
                if (databaseHelper.getSelfResBookingExist(bookingid) > 0) { //only do cancel
                    // action if booking exists

                    new UIHelper().dialog2Btns(ResBookingActivity.this, getString(R.string.cancel_booking),
                            getString(R.string.booking_cancellation_ask), R.string.ok_label, R.string.cancel,
                            R.color.white, R.color.grey7,CancelOK, null, true);

//                    final AlertDialog alertDialog = new AlertDialog.Builder(ResBookingActivity.this).create();
//                    alertDialog.setTitle(R.string.cancel_booking);
//                    alertDialog.setMessage(getString(R.string
//                            .booking_cancellation_ask));
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    //only need send stanza to bizUser if not
//                                    // expired/rejected by owner (3, 4)
//                                    if (bookingStatus < 3) {
//                                        new AsyncResBookingCancel().execute(bookingid);
//
//                                        dialog.dismiss();
//
//                                    } else { //expired/rejected, just clear sqlite
//                                        databaseHelper.outgoingCancelSelfResBooking(resID);
//
//                                        if (progressDialog != null && progressDialog.isShowing()) {
//                                            progressDialog.dismiss();
//                                        }
//
//                                        Toast.makeText(ResBookingActivity.this, getString(R
//                                                .string.booking_cancelled_success), Toast.LENGTH_SHORT).show();
//
//                                        dialog.dismiss();
//                                        finish();
//                                    }
//                                }
//                            });
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string
//                            .cancel), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    alertDialog.show();
//
//                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryDark4));
//                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.primaryDark4));

                } else { //booking doesn't exist, toast user and finish activity
                    //ash - add toast/snackbar tell user booking doesn't exist
                    Toast.makeText(ResBookingActivity.this, getString(R.string.booking_not_exist), Toast.LENGTH_SHORT).show();

                    finish();
                }
        }


    }
//
//            case (R.id.btn_more):
//                //time diff less than 3 hours (10800000 ms) AND already confirmed by owner, CAN'T
//                // CHANGE
//                if (!isTimeDiffOK(10800000) && bookingStatus == 1) {
//                    //ash - alert progressDialog "You can no longer edit this
//
//                    final AlertDialog alertDialog = new AlertDialog.Builder(ResBookingActivity.this).create();
//                    alertDialog.setTitle(R.string.booking_edit_unable);
//                    alertDialog.setMessage(getString(R.string.cant_edit));
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//
//                    alertDialog.show();
//
//                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryDark4));
//                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.primaryDark4));
//
//                } else { //other situations, still can change
//                    View viewInflate = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_2_btns, null);
//
//                    final BottomSheetDialog btmSheetDialog = new BottomSheetDialog(this);
//                    btmSheetDialog.setContentView(viewInflate);
//
//                    Button btn_edit = (Button) viewInflate.findViewById(R.id.btn_edit);
//                    btn_edit.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//
//                            Intent intent = new Intent(ResBookingActivity.this, FoodDetailBookingFragment.class);
//
//                            intent.putExtra("infoid", resID);
//                            intent.putExtra("ownerJID", ownerJID);
//                            intent.putExtra("restitle", resTitle);
//                            intent.putExtra("image_id", resPropic);
//                            intent.putExtra("resLoc", resLoc);
//                            intent.putExtra("resState", resState);
//                            intent.putExtra("resPhone", resPhone);
//                            intent.putExtra("resLat", resLat);
//                            intent.putExtra("resLon", resLon);
//
//                            startActivity(intent);
//
//                            btmSheetDialog.dismiss();
//                        }
//                    });

//                    Button btn_cancel_mybooking = (Button) viewInflate.findViewById(R.id.btn_cancel_mybooking);
//                    btn_cancel_mybooking.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (databaseHelper.getSelfResBookingExist(resID) > 0) { //only do cancel
//                                // action if booking exists
//
//                                final AlertDialog alertDialog = new AlertDialog.Builder(ResBookingActivity.this).create();
//                                alertDialog.setTitle(R.string.cancel_booking);
//                                alertDialog.setMessage(getString(R.string
//                                        .booking_cancellation_ask));
//                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//
//                                                //only need send stanza to bizUser if not
//                                                // expired/rejected by owner (3, 4)
//                                                if (bookingStatus < 3) {
//                                                    new AsyncResBookingCancel().execute(resID);
//
//                                                    btmSheetDialog.dismiss();
//                                                    dialog.dismiss();
//
//                                                } else { //expired/rejected, just clear sqlite
//                                                    databaseHelper.outgoingCancelSelfResBooking(resID);
//
//                                                    if (progressDialog != null && progressDialog.isShowing()) {
//                                                        progressDialog.dismiss();
//                                                    }
//
//                                                    Toast.makeText(ResBookingActivity.this, getString(R
//                                                            .string.booking_cancelled_success), Toast.LENGTH_SHORT).show();
//
//                                                    btmSheetDialog.dismiss();
//                                                    dialog.dismiss();
//                                                    finish();
//                                                }
//                                            }
//                                        });
//                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string
//                                        .cancel), new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                                alertDialog.show();
//
//                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryDark4));
//                                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.primaryDark4));
//
//                            } else { //booking doesn't exist, toast user and finish activity
//                                //ash - add toast/snackbar tell user booking doesn't exist
//                                Toast.makeText(ResBookingActivity.this, getString(R.string.booking_not_exist), Toast.LENGTH_SHORT).show();
//
//                                btmSheetDialog.dismiss();
//                                finish();
//                            }
//                        }
//                    });
//                    btmSheetDialog.show();
//
//                }
//                break;
//
//            //delete booking, by non-bookers only
//            case (R.id.btn_delete):
//                if (databaseHelper.getSharedResBookingExist(resID, sharedJID) > 0) { //only do cancel
//                    // action if booking exists
//
//                    final AlertDialog alertDialog = new AlertDialog.Builder(ResBookingActivity.this).create();
//                    alertDialog.setTitle(R.string.booking_delete);
//                    alertDialog.setMessage(getString(R.string
//                            .booking_deletion_ask));
//
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    databaseHelper.deleteBookingNonBooker(resID, sharedJID);
//
//                                    Toast.makeText(ResBookingActivity.this, getString(R
//                                            .string.booking_deleted), Toast.LENGTH_SHORT).show();
//
//                                    dialog.dismiss();
//                                    finish();
//                                }
//                            });
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string
//                            .cancel), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    alertDialog.show();
//
//                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primaryDark4));
//                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.primaryDark4));
//
//                } else { //booking doesn't exist, toast user and finish activity
//                    //ash - add toast/snackbar tell user booking doesn't exist
//                    finish();
//                    Toast.makeText(ResBookingActivity.this, "Sorry, no booking record found", Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            default:
//                break;
//
//            case (R.id.mybooking_phone):
//                new UIHelper().alertDialog2Btns(this, R.string.booking_call_res, getString(R.string
//                                .booking_call_res_msg), R.string.ok_label, R.string.cancel, Color.BLACK,
//                        Color.BLACK, actionCallResPositive, null, true);
//                break;
//
//            case (R.id.mybooking_res_location):
//                new UIHelper().showNavigationPopup(this, resLat, resLon, Gravity.END, Gravity
//                        .CENTER, 20, 40);
//                break;
//
//            case (R.id.b_backBtn):
//                onBackPressed();
//                break;
//        }


    private boolean isTimeDiffOK(int timeDiffMiliSec) {
        //check if time current time is already within 3 hours of booking
        long dateTimeDiff;

        DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yy h:mm a", Locale
                .ENGLISH);
        try {
            Date convertedDateTime = dateTimeFormat.parse(dateStr + " " + timeStr);
            dateTimeDiff = convertedDateTime.getTime() - System.currentTimeMillis();
        } catch (ParseException e) {
            dateTimeDiff = 0;
        }

        return dateTimeDiff > timeDiffMiliSec;
    }

    //Post booking info to biz owner via retro
//    public void resBookingPost(final String resID) {
//        String access_token = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(),
//                GlobalVariables.STRPREF_ACCESS_TOKEN);
//        RestaurantBookingInfo model = new RestaurantBookingInfo("reject", "",
//                "", "", "", resID, "");
//
//        //build retrofit
//        RestaurantCancel client = RetrofitAPIClient.getClient().create(RestaurantCancel.class);
//        retrofit2.Call<RestaurantBookingInfo> call = client.resBook(model, "Bearer " + access_token);
//
//        call.enqueue(new retrofit2.Callback<RestaurantBookingInfo>() {
//
//            @Override
//            public void onResponse(Call<RestaurantBookingInfo> call, Response<RestaurantBookingInfo> response) {
//                if (!response.isSuccessful()) {
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                    Toast.makeText(ResBookingActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT)
//                            .show();
//                    return;
//                }
//
//                //first time booking, set remaining attempts to 2
//                databaseHelper.outgoingCancelSelfResBooking(resID);
//
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//
//                Toast.makeText(ResBookingActivity.this, getString(R
//                        .string.booking_deleted), Toast.LENGTH_SHORT).show();
//
//                finish();
//            }
//
//            @Override
//            public void onFailure(Call<RestaurantBookingInfo> call, Throwable t) {
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                Toast.makeText(ResBookingActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void resBookingPost(final String bookingid) {
        String access_token = Preferences.getInstance().getValue(Soapp.getInstance().getApplicationContext(),
                GlobalVariables.STRPREF_ACCESS_TOKEN);
        RestaurantBookingInfo model = new RestaurantBookingInfo("reject", null,
                null, null, null, null, null, bookingid);

        //build retrofit
        RestaurantCancel client = RetrofitAPIClient.getClient().create(RestaurantCancel.class);
        retrofit2.Call<RestaurantBookingInfo> call = client.resBook(model, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantBookingInfo>() {

            @Override
            public void onResponse(Call<RestaurantBookingInfo> call, Response<RestaurantBookingInfo> response) {
                if (!response.isSuccessful()) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    new MiscHelper().retroLogUnsuc(response, "resBookingPost ", "JAY");
                    Toast.makeText(ResBookingActivity.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }

                //first time booking, set remaining attempts to 2
                databaseHelper.outgoingCancelSelfResBooking(bookingid);

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(ResBookingActivity.this, getString(R
                        .string.booking_deleted), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<RestaurantBookingInfo> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new MiscHelper().retroLogFailure(t, "resBookingPost ", "JAY");
                Toast.makeText(ResBookingActivity.this, R.string.onfailure, Toast.LENGTH_SHORT).show();

            }
        });
    }


    private class AsyncResBookingCancel extends android.os.AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ResBookingActivity.this, null, getString(R.string
                    .booking_progress));
        }

        @Override
        protected Void doInBackground(String... params) {
            resBookingPost(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}