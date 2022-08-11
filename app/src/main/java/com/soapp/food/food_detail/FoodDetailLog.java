package com.soapp.food.food_detail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.google.android.material.tabs.TabLayout;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.ResDetPromoTypeModel;
import com.soapp.SoappApi.ApiModel.ResDetPromotionModel;
import com.soapp.SoappApi.ApiModel.RestaurantInfo;
import com.soapp.SoappApi.Interface.RestaurantDetails;
import com.soapp.base.BaseActivity;
import com.soapp.camera.VideoPreviewActivity;
import com.soapp.food.food_detail.booking.FoodDetailBookingFragment;
import com.soapp.food.food_detail.images.SliderImageAdapter;
import com.soapp.food.food_detail.info.FoodDetailInfoFragment;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewSlideFood;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.sharing.SharingController;
import com.soapp.home.SyncTabAdapter;
import com.soapp.setup.Soapp;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.soapp.camera.VideoPreviewActivity.VideoTimer;

public class FoodDetailLog extends BaseActivity implements View.OnClickListener {
    //public statics for access from fragments
    public static String[] foodDetailPromo, foodDetailInfo, foodDetailImages, foodreview;
    public static int operatingHours;

    //strings for intent extras
    public static String resLoc, resState, resTitle, resPropic, resID, ownerJID, resLat, resLon;
    public static int Videopause;
    private static ViewPager mPager;
    private static int currentPage = 0;

    public ArrayList<String> imageUrlList = new ArrayList<>();
    boolean hasVideoPath = false;
    File fileFVideo;
    String path;
    Uri uri;
    ViewPager viewPager;
    ImageView food_default_profile_img, res_cam_btn;
    LinearLayout food_detail_progress, food_detail_failed;
    ImageView food_detail_share, food_detail_appt;
    Button btnbooking;
    //    Button btn_promo;
    Bitmap qrBitmap;
    int[] intArrayIcons = new int[]{
            R.drawable.ic_info_grey100px,
            R.drawable.ic_discount_grey100px,
            R.drawable.ic_star_grey_100px,
            R.drawable.ic_schedule_grey100px
    };
    int[] intArrayIconsSelected = new int[]{
            R.drawable.ic_info_grey100px,
            R.drawable.ic_discount_grey100px,
            R.drawable.ic_star_grey_100px,
            R.drawable.ic_schedule_grey100px

    };
    //image view dolar
    ImageView imgv_price_intro_one, imgv_price_intro_two, imgv_price_intro_three, imgv_price_intro_four, imgv_price_intro_five;
    //image view star
    ImageView imgv_rating_intro_one, imgv_rating_intro_two, imgv_rating_intro_three, imgv_rating_intro_four, imgv_rating_intro_five;
    RelativeLayout video_imgbtn, video_relay, food_propic_rl, rl_image_slider;
    TextView video_percent_txtview;
    ProgressBar video_loading_probar;
    MediaPlayer mp;
    VideoView videoStreaming;
    ImageButton mute_imgbtn;
    boolean muted = true;
    FoodDetailBookingFragment fragment;
    Bundle bundle;

    //text view
    TextView tv_food_intro, tv_price_range_intro, tv_rating_intro, tv_open_status;

    //
    ConstraintLayout cl_res_intro;
    private TabLayout tabLayout;
    private Preferences preferences = Preferences.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_detail);

        //constraint layout
        cl_res_intro = findViewById(R.id.cl_res_intro);

        //image view dolar
        imgv_price_intro_one = findViewById(R.id.imgv_price_intro_one);
        imgv_price_intro_two = findViewById(R.id.imgv_price_intro_two);
        imgv_price_intro_three = findViewById(R.id.imgv_price_intro_three);
        imgv_price_intro_four = findViewById(R.id.imgv_price_intro_four);
        imgv_price_intro_five = findViewById(R.id.imgv_price_intro_five);

        //image view star
        imgv_rating_intro_one = findViewById(R.id.imgv_rating_intro_one);
        imgv_rating_intro_two = findViewById(R.id.imgv_rating_intro_two);
        imgv_rating_intro_three = findViewById(R.id.imgv_rating_intro_three);
        imgv_rating_intro_four = findViewById(R.id.imgv_rating_intro_four);
        imgv_rating_intro_five = findViewById(R.id.imgv_rating_intro_five);

        tv_food_intro = findViewById(R.id.tv_food_intro);
        tv_price_range_intro = findViewById(R.id.tv_price_range_intro);
        tv_rating_intro = findViewById(R.id.tv_rating_intro);
        tv_open_status = findViewById(R.id.tv_open_status);

        //set the video timer
        VideoTimer = 0;
        Videopause = 0;

        //get intent extras
        resID = getIntent().getStringExtra("resID");
        ownerJID = getIntent().getStringExtra("ownerJid");
        resTitle = getIntent().getStringExtra("resName");
        resPropic = getIntent().getStringExtra("resPropic");
        resLoc = getIntent().getStringExtra("resLoc");
        resState = getIntent().getStringExtra("resState");
        resLat = getIntent().getStringExtra("resLat");
        resLon = getIntent().getStringExtra("resLon");

        bundle = new Bundle();

        bundle.putString("resID", resID);
        bundle.putString("ownerJid", ownerJID);
        bundle.putString("resName", resTitle);
        bundle.putString("resPropic", resPropic);
        bundle.putString("resLoc", resLoc);
        bundle.putString("resState", resState);
        bundle.putString("resLat", resLat);
        bundle.putString("resLon", resLon);

        fragment = new FoodDetailBookingFragment();
        fragment.setArguments(bundle);
        fragment.putArguments(bundle);

        //get restaurant info from server
        getRestaurant(resID, preferences.getValue(this, GlobalVariables.STRPREF_ACCESS_TOKEN));

        //image slider list
        rl_image_slider = findViewById(R.id.rl_image_slider);

        food_propic_rl = findViewById(R.id.food_propic_rl);
        videoStreaming = findViewById(R.id.videoStreaming);

        video_relay = findViewById(R.id.video_relay);
        video_percent_txtview = findViewById(R.id.video_percent_txtview);
        video_loading_probar = findViewById(R.id.video_loading_probar);
        mute_imgbtn = findViewById(R.id.mute_imgbtn);
        mute_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (muted) {
                    unmute();
                } else {
                    mute();
                }
            }
        });

        //action button which appears after video finish playing
        video_imgbtn = findViewById(R.id.video_imgBtn);
        video_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnbooking.performClick();
            }
        });

        viewPager = findViewById(R.id.food_det_viewpager);

        tabLayout = findViewById(R.id.food_det_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primaryDark1));

        ImageView b_backBtn = findViewById(R.id.b_backBtn);
        b_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setTitle(resTitle);

        res_cam_btn = findViewById(R.id.res_cam_btn);
        food_default_profile_img = findViewById(R.id.food_default_profile_img);
        food_default_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resPropic != null) {
                    Intent intent = new Intent(FoodDetailLog.this, ImagePreviewSlideFood.class);
                    intent.putExtra("url", resPropic);

                    startActivity(intent);
                }
            }
        });

        food_detail_progress = findViewById(R.id.food_detail_progress);
        food_detail_failed = findViewById(R.id.food_detail_failed);

        food_detail_appt = findViewById(R.id.food_detail_appt);
        food_detail_appt.setOnClickListener(view -> {
            Intent shareIntent = new Intent(FoodDetailLog.this, SharingController.class);

            shareIntent.putExtra("infoid", resID);
            shareIntent.putExtra("restitle", resTitle);
            shareIntent.putExtra("image_id", resPropic);
            shareIntent.putExtra("resLat", resLat);
            shareIntent.putExtra("resLon", resLon);
            shareIntent.putExtra("from", "foodAppt");

            startActivity(shareIntent);
        });

        food_detail_share = findViewById(R.id.food_detail_share);
        food_detail_share.setOnClickListener(view -> {
            Intent shareIntent = new Intent(FoodDetailLog.this, SharingController.class);
            shareIntent.putExtra("infoid", resID);
            shareIntent.putExtra("restitle", resTitle);
            shareIntent.putExtra("image_id", resPropic);
            shareIntent.putExtra("from", "foodChat");
            startActivity(shareIntent);
        });

        if (resPropic == null) {
            res_cam_btn.setVisibility(View.GONE);
            res_cam_btn.setOnClickListener(this);
        } else {
//            imageUrlList.add(resPropic);
            res_cam_btn.setVisibility(View.GONE);
        }

        GlideApp.with(this)
                .asBitmap()
                .load(resPropic)
                .placeholder(R.drawable.ic_res_def_loading_640px)
                .error(R.drawable.ic_res_default_no_image_black_640px)
                .transition(BitmapTransitionOptions.withCrossFade())
                .thumbnail(0.1f)
                .into(food_default_profile_img);
//
//        Glide.with(this)
//                .load(resPropic)
////                .placeholder(R.drawable.food_default)
//                .into(food_default_profile_img);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int posFromVideoView = data.getIntExtra("pos", 0);

                videoStreaming.seekTo(posFromVideoView);
                videoStreaming.start();
            }
        }
    }

    public void mute() {
        mute_imgbtn.setImageResource(R.drawable.ic_mute_100px);
        setVolume(0);
        muted = true;
    }

    public void unmute() {
        mute_imgbtn.setImageResource(R.drawable.ic_unmute_100px);
        setVolume(100);
        muted = false;
    }

    private void setVolume(int amount) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        mp.setVolume(volume, volume);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.res_cam_btn):
                break;

            default:
                break;
        }
    }

    private void getRestaurant(final String res_ID, final String access_token) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://api2.soappchat.com/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        RestaurantInfo model = new RestaurantInfo(res_ID);

        RestaurantDetails client = retrofit.create(RestaurantDetails.class);
        retrofit2.Call<RestaurantInfo> call = client.nearbyRes(model, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<RestaurantInfo>() {
            @Override
            public void onResponse(retrofit2.Call<RestaurantInfo> call, retrofit2.Response<RestaurantInfo> response) {
                if (!response.isSuccessful()) {
                    food_detail_progress.setVisibility(View.GONE);
                    food_detail_failed.setVisibility(View.VISIBLE);
                    new MiscHelper().retroLogUnsuc(response, "getRestaurant ", "JAY");
                    Toast.makeText(Soapp.getInstance().getApplicationContext(), R.string.onresponse_unsuccessful, Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                //get restaurant details in string form
                foodDetailInfo = new String[]{
                        //Description (0)
                        String.valueOf(response.body().getDescription()),
                        String.valueOf(response.body().getMainCuisine()),
                        String.valueOf(response.body().getCuisine2()),
                        String.valueOf(response.body().getCuisine3()),
                        String.valueOf(response.body().getRating()),
                        String.valueOf(response.body().getPrice()),

                        //Information (6)
                        String.valueOf(response.body().getAddress()),
                        String.valueOf(response.body().getPhone1()),
                        String.valueOf(response.body().getMall()),

                        //additional details (9)
                        String.valueOf(response.body().getHalal()),
                        String.valueOf(response.body().getAlcohol()),
                        String.valueOf(response.body().getSmoking()),
                        String.valueOf(response.body().getWifi()),

                        //Operating Hours (13)
                        String.valueOf(response.body().getMon1()),
                        String.valueOf(response.body().getTue1()),
                        String.valueOf(response.body().getWed1()),
                        String.valueOf(response.body().getThu1()),
                        String.valueOf(response.body().getFri1()),
                        String.valueOf(response.body().getSat1()),
                        String.valueOf(response.body().getSun1()),

                        //Location (20)
                        String.valueOf(response.body().getLatitude()),
                        String.valueOf(response.body().getLongitude()),

                        //promo video for premium owners (22)
                        String.valueOf(response.body().getVideo()),

                        //booking-related - 1 able to book,

                        //res id and details for non-intent possible use
                        res_ID,
                        resTitle
                };

                //set restaurant title
                tv_food_intro.setText(resTitle);

                //set price
                switch (foodDetailInfo[5]) {
                    //premium price - set full gold dollars
                    case "Above RM150":
                        imgv_price_intro_five.setImageResource(R.drawable.ic_dolar_red_40px);
                        imgv_price_intro_four.setImageResource(R.drawable.ic_dolar_red_40px);
                        imgv_price_intro_three.setImageResource(R.drawable.ic_dolar_red_40px);
                        imgv_price_intro_two.setImageResource(R.drawable.ic_dolar_red_40px);
                        imgv_price_intro_one.setImageResource(R.drawable.ic_dolar_red_40px);
                        break;

                    //non-premium - set red dollars
                    case "RM81 - RM150":
                        imgv_price_intro_five.setImageResource(R.drawable.ic_dolar_red_40px);

                    case "RM41 - RM80":
                        imgv_price_intro_four.setImageResource(R.drawable.ic_dolar_red_40px);

                    case "RM21 - RM40":
                        imgv_price_intro_three.setImageResource(R.drawable.ic_dolar_red_40px);

                    case "RM11 - RM20":
                        imgv_price_intro_two.setImageResource(R.drawable.ic_dolar_red_40px);

                    case "RM10 and below":
                        imgv_price_intro_one.setImageResource(R.drawable.ic_dolar_red_40px);
                        break;
                }
                tv_price_range_intro.setText(foodDetailInfo[5]);

                //set rating
                switch (foodDetailInfo[4]) {
                    //full points
                    case "5.0":
                    case "5":
                        imgv_rating_intro_five.setImageResource(R.drawable.ic_star_red);

                    case "4.0":
                    case "4":
                        imgv_rating_intro_four.setImageResource(R.drawable.ic_star_red);

                    case "3.0":
                    case "3":
                        imgv_rating_intro_three.setImageResource(R.drawable.ic_star_red);

                    case "2.0":
                    case "2":
                        imgv_rating_intro_two.setImageResource(R.drawable.ic_star_red);

                    case "1.0":
                    case "1":
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red);
                        break;

                    //half points
                    case "4.5":
                        imgv_rating_intro_five.setImageResource(R.drawable.ic_star_red_half);
                        imgv_rating_intro_four.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_three.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_two.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red);
                        break;

                    case "3.5":
                        imgv_rating_intro_four.setImageResource(R.drawable.ic_star_red_half);
                        imgv_rating_intro_three.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_two.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red);
                        break;

                    case "2.5":
                        imgv_rating_intro_three.setImageResource(R.drawable.ic_star_red_half);
                        imgv_rating_intro_two.setImageResource(R.drawable.ic_star_red);
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red);
                        break;

                    case "1.5":
                        imgv_rating_intro_two.setImageResource(R.drawable.ic_star_red_half);
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red);
                        break;

                    case "0.5":
                        imgv_rating_intro_one.setImageResource(R.drawable.ic_star_red_half);
                        break;

                }
                tv_rating_intro.setText(foodDetailInfo[4]);

                //operating hours - open now
                long timeNow = System.currentTimeMillis();

                SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm", Locale.ENGLISH);

                String timeNowStr = timeFormat.format(timeNow);
                String todayStr = new SimpleDateFormat("EE", Locale.ENGLISH).format(timeNow);

                //current time formats returned from server:
                // "---" = no operating hours info
                // "8:00-21:00" = time range in 24 hours format
                // "00:00-00:00 = open 24 hours
                // "Closed" = closed

                //get row of day in int first
                int todayInt;
                switch (todayStr) {
                    case "Mon":
                        todayInt = 13;
                        break;

                    case "Tue":
                        todayInt = 14;
                        break;

                    case "Wed":
                        todayInt = 15;
                        break;

                    case "Thu":
                        todayInt = 16;
                        break;

                    case "Fri":
                        todayInt = 17;
                        break;

                    case "Sat":
                        todayInt = 18;
                        break;

                    case "Sun":
                        todayInt = 19;
                        break;

                    default:
                        todayInt = 13;
                        break;
                }

                //based on row, determine open or closed today
                switch (foodDetailInfo[todayInt]) {
                    case "---": //no operating hours info
                        tv_open_status.setText("");
                        operatingHours = 2;
                        break;

                    case "Closed": //closed
                        tv_open_status.setText(R.string.food_closed_caps);
                        operatingHours = 0;
                        break;

                    case "00:00-00:00": //open 24 hours
                        tv_open_status.setText(R.string.food_open_caps);
                        operatingHours = 1;
                        break;

                    default: //time range - need to check time now
                        try {
                            String[] parts = foodDetailInfo[todayInt].split("-");

                            //set calendar for from time
                            String timeFromStr = parts[0];
                            Date timeFromDate = timeFormat.parse(timeFromStr);
                            Calendar calFrom = Calendar.getInstance();
                            calFrom.setTime(timeFromDate);

                            //set calendar for to time
                            String timeToStr = parts[1];
                            Date timeToDate = timeFormat.parse(timeToStr);
                            Calendar calTo = Calendar.getInstance();
                            calTo.setTime(timeToDate);
                            calTo.add(Calendar.DATE, 1);

                            //set calendar for now
                            Date timeNowDate = timeFormat.parse(timeNowStr);
                            Calendar calNow = Calendar.getInstance();
                            calNow.setTime(timeNowDate);
                            calNow.add(Calendar.DATE, 1);

                            //check if calendar for time now is between from and to
                            if (timeNowDate.after(calFrom.getTime()) && timeNowDate.before(calTo.getTime())) { //between, open
                                tv_open_status.setText(R.string.food_open_caps);
                                operatingHours = 1;
                            } else { //closed
                                tv_open_status.setText(R.string.food_closed_caps);
                                operatingHours = 0;
                            }
                        } catch (ParseException e) {
                            tv_open_status.setText("");
                            operatingHours = 2;
                        }
                        break;
                }

                //get promotions in list form
                List<ResDetPromotionModel> listPromo = response.body().getPromotion();

                //first type of promotion - simple discount
                List<ResDetPromoTypeModel> listPromoType = listPromo.get(0).getPromotion_restaurant_simple_discount();

                //initialise boolean for gotPromo first
                boolean gotPromo;

                //if no promotions at all
                if (listPromoType == null)

                {
                    gotPromo = false;
                } else

                { //got promotions, loop promotion ids in each promotion type
                    gotPromo = true;
//                    btn_promo.setVisibility(View.VISIBLE);
//                    btn_promo.setVisibility(View.GONE);
                    //for now get 1st promo only, next time need loop
//                    for (int j = 0; j < listPromoType.size(); j++) {
                    foodDetailPromo = new String[]{
                            //promotion details (0)
                            listPromoType.get(0).getPromotion_id(), //0
                            listPromoType.get(0).getTitle(), //1
                            listPromoType.get(0).getDate_start(), //2
                            listPromoType.get(0).getDate_end(), //3
                            listPromoType.get(0).getDiscount(), //4
                            listPromoType.get(0).getDetails_1(), //5
                            listPromoType.get(0).getDetails_2(), //6
                            listPromoType.get(0).getResource_url(), //7
                            listPromoType.get(0).getQr_code_id(), //8
                    };
//                    btn_promo.setText(foodDetailPromo[4] + "%" + " Promotion");
                }

                //try to stream vid if got video
                if (foodDetailInfo[22] != null && foodDetailInfo[22].equals("1")) { //got vid
                    food_propic_rl.setVisibility(View.GONE);
                    rl_image_slider.setVisibility(View.VISIBLE);
                    video_relay.setVisibility(View.GONE);

                    //get resID int from behind "MY-res-xx"
                    int res_id = Integer.valueOf(resID.substring(resID.lastIndexOf('-') + 1));
                    path = "https://s3-ap-southeast-1.amazonaws" +
                            ".com/soappchat-restaurant-video/MY/MY" + res_id + "/" + resID + ".mp4";
                    hasVideoPath = true;
                    imageUrlList.add(path);
                    imageUrlList.add(resPropic);
                    fileFVideo = new File(getCacheDir() + "/" + resID + ".mp4"); //put the downloaded file here

                    if (!fileFVideo.exists()) {
                        uri = Uri.parse(path);
                        new DownloaderAsyncTask().execute();

                        videoStreaming.setVideoURI(uri);
                    } else {
                        videoStreaming.setVideoPath(String.valueOf(fileFVideo));
                    }

                    videoStreaming.requestFocus();
                    videoStreaming.setOnCompletionListener(
                            new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    video_relay.setVisibility(View.GONE);
                                    video_imgbtn.setVisibility(View.VISIBLE);

                                    // finish the video set all time to 0
                                    Videopause = 0;
                                    VideoTimer = 0;

                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                            video_imgbtn.setVisibility(View.GONE);
                                            video_relay.setVisibility(View.VISIBLE);

                                            // set if cache dun have this video will auto download to cache
                                            if (!fileFVideo.exists()) {
                                                uri = Uri.parse(path);
                                                new DownloaderAsyncTask().execute();
                                                videoStreaming.setVideoURI(uri);
                                                videoStreaming.start();
                                            } else {
                                                videoStreaming.setVideoPath(String.valueOf(fileFVideo));
                                                videoStreaming.start();
                                            }
                                        }
                                    }, 5000);
                                }
                            }
                    );

                    videoStreaming.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {

                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                Intent intent = new Intent(FoodDetailLog.this, VideoPreviewActivity.class);
                                if (!fileFVideo.exists()) {
                                    intent.putExtra("streamLink", path);
                                } else {
                                    intent.putExtra("streamLink1", getCacheDir() + "/" + resID + ".mp4");
                                }
                                intent.putExtra("lastStopAt", videoStreaming.getCurrentPosition());
                                startActivity(intent);

                            }
                            return false;
                        }
                    });

                    videoStreaming.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {

                            mute_imgbtn.setVisibility(View.VISIBLE);
                            video_loading_probar.setVisibility(View.GONE);

                            // set when come back the video timer
                            if (VideoTimer != 0) {
                                mediaPlayer.seekTo(VideoTimer);
                                mediaPlayer.start();

                            } else if (Videopause != 0) {
                                mediaPlayer.seekTo(Videopause);

                            }

                            mediaPlayer.start();
                            mp = mediaPlayer;
                            mute();

                            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                                @Override
                                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                                    if (i <= 20) {
                                        String buffering = getString(R.string.video_buffering) +
                                                " " + i + "%";
                                        video_percent_txtview.setText(buffering);
                                    } else {
                                        video_percent_txtview.setText("");
                                    }
                                }
                            });
                        }
                    });

                } else { //no vid, just show propic
                    video_relay.setVisibility(View.GONE);
//                    food_propic_rl.setVisibility(View.VISIBLE);
                    rl_image_slider.setVisibility(View.VISIBLE);
                    imageUrlList.add(resPropic);
                    food_propic_rl.setVisibility(View.GONE);
                }

                //detail image


                if (String.valueOf(response.body().getI1URL()).equals("null")) {
                    foodDetailImages = new String[]{};
                } else if (String.valueOf(response.body().getI2URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                    };
                } else if (String.valueOf(response.body().getI3URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                    };
                } else if (String.valueOf(response.body().getI4URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                    };
                } else if (String.valueOf(response.body().getI5URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                            String.valueOf(response.body().getI4URL()),
                    };
                } else if (String.valueOf(response.body().getI6URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                            String.valueOf(response.body().getI4URL()),
                            String.valueOf(response.body().getI5URL()),
                    };
                } else if (String.valueOf(response.body().getI7URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                            String.valueOf(response.body().getI4URL()),
                            String.valueOf(response.body().getI5URL()),
                            String.valueOf(response.body().getI6URL()),
                    };
                } else if (String.valueOf(response.body().getI8URL()).equals("null")) {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                            String.valueOf(response.body().getI4URL()),
                            String.valueOf(response.body().getI5URL()),
                            String.valueOf(response.body().getI6URL()),
                            String.valueOf(response.body().getI7URL()),
                    };
                } else {
                    foodDetailImages = new String[]{
                            String.valueOf(response.body().getI1URL()),
                            String.valueOf(response.body().getI2URL()),
                            String.valueOf(response.body().getI3URL()),
                            String.valueOf(response.body().getI4URL()),
                            String.valueOf(response.body().getI5URL()),
                            String.valueOf(response.body().getI6URL()),
                            String.valueOf(response.body().getI7URL()),
                            String.valueOf(response.body().getI8URL())
                    };
                }

                if (foodDetailImages.length != 0) {
                    init();
                } else {
                    rl_image_slider.setVisibility(View.GONE);
                    food_propic_rl.setVisibility(View.VISIBLE);
                    food_default_profile_img.setImageResource(R.drawable.ic_res_default_no_image_black_640px);
                }
                //FoodReviewFrag
                foodreview = new String[]{

                };

                //setup details tabs and contents only when got data
                setupViewPager(viewPager, gotPromo);

//                setTabIcon();

//                TabLayout.Tab tab = tabLayout.getTabAt(0);
//                View v = tab.getCustomView();
//                RelativeLayout rl_food_detail_tab = v.findViewById(R.id.rl_food_detail_tab);
//                rl_food_detail_tab.setBackgroundColor(
//
//                        getResources().
//
//                                getColor(R.color.primaryDark2));
//                ImageView im = v.findViewById(R.id.imgv_icon);
//                im.setImageResource(intArrayIconsSelected[0]);
//                tab.select();

                food_detail_failed.setVisibility(View.GONE);
                food_detail_progress.setVisibility(View.GONE);
                cl_res_intro.setVisibility(View.VISIBLE);

                if (ownerJID == null || ownerJID.equals("")) {
//                    btnbooking.setVisibility(View.GONE);
                } else {
//                    btnbooking.setVisibility(View.VISIBLE); the button tat block the slider dot
//                    btnbooking.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RestaurantInfo> call, Throwable t) {
                food_detail_failed.setVisibility(View.VISIBLE);
                food_detail_progress.setVisibility(View.GONE);
                new MiscHelper().retroLogFailure(t, "resBookingPost ", "JAY");

                Toast.makeText(Soapp.getInstance().getApplicationContext(), R.string.onfailure, Toast
                        .LENGTH_SHORT).show();
            }
        });
    }

    //function to set number of tabs to show (show or hide promo tab etc)
    private void setupViewPager(ViewPager viewPager, boolean gotPromo) {
        SyncTabAdapter adapter = new SyncTabAdapter(this.getSupportFragmentManager());


        adapter.addFragment(new FoodDetailInfoFragment(), null);
//        adapter.addFragment(new FoodDetailPromo(), null);
//        adapter.addFragment(new FoodDetailReview(), null);
//        adapter.addFragment(new FoodDetailBookingFragment(), null);

        viewPager.setAdapter(adapter);

        setViewPagerIcons(gotPromo);

    }

    //function to set icon for view pager to show (got promo or not etc)
    private void setViewPagerIcons(boolean gotPromo) {
        //for now don't use tab for promo, just do button + qr
//        if (gotPromo) { //got promo, use icons starting from 0
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                RelativeLayout rl_food_detail_tab = v.findViewById(R.id.rl_food_detail_tab);
                rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.primaryDark2));
                ImageView imgv_icon = v.findViewById(R.id.imgv_icon);

                int tabIndex = tab.getPosition();
                imgv_icon.setImageResource(intArrayIconsSelected[tabIndex]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                int tabIndex = tab.getPosition();
                RelativeLayout rl_food_detail_tab = v.findViewById(R.id.rl_food_detail_tab);
                switch (tabIndex) {
                    case 0:
                        rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey8));
                        break;
                    case 1:
                        rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey6));
                        break;
                    case 2:
                        rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey4));
                        break;
                    case 3:
                        rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey2));
                        break;
                }
                ImageView imgv_icon = v.findViewById(R.id.imgv_icon);
                imgv_icon.setImageResource(intArrayIconsSelected[tabIndex]);
                tab.setIcon(intArrayIconsSelected[tabIndex]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setTabIcon() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//            tabLayout.getTabAt(i).setIcon(intArrayIcons[i]);
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
    }

    protected void onPause() {
        super.onPause();

        // pause the video
        VideoTimer = 0;
        Videopause = videoStreaming.getCurrentPosition();
    }

    public String[] getfoodDetailInfo() {
        return foodDetailInfo;
    }

    public String getResId() {
        return resID;
    }

    public String getResName() {
        return resTitle;
    }

    public void init() {
        imageUrlList.addAll(Arrays.asList(foodDetailImages));
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new SliderImageAdapter(FoodDetailLog.this, imageUrlList, hasVideoPath));
        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
    }

    private View prepareTabView(final int i) {
        View view = getLayoutInflater().inflate(R.layout.tab_item_food_detail, null);
        RelativeLayout rl_food_detail_tab = view.findViewById(R.id.rl_food_detail_tab);
        switch (i) {
            case 0:
                rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey6));
                break;
            case 1:
                rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey4));
                break;
            case 2:
                rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey2));
                break;
            case 3:
                rl_food_detail_tab.setBackgroundColor(getResources().getColor(R.color.grey1));
                break;
        }

        ImageView imgbtn = view.findViewById(R.id.imgv_icon);
        imgbtn.setImageResource(intArrayIcons[i]);

        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabLayout.Tab clickTab = tabLayout.getTabAt(i);
                clickTab.select();
            }
        });
        return view;
    }

    class DownloaderAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(String... longs) {

            try {
                URL url = new URL(path); //you can write here any link

                long startTime = System.currentTimeMillis();
                /* Open a connection to that URL. */
                URLConnection ucon = url.openConnection();

                /*
                 * Define InputStreams to read from the URLConnection.
                 */
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1).
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                /* Convert the Bytes read to a String. */
                FileOutputStream fos = new FileOutputStream(fileFVideo);
                fos.write(baf.toByteArray());
                fos.close();

            } catch (IOException e) {
            }

            return null;
        }
    }

}