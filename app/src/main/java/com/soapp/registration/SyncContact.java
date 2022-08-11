package com.soapp.registration;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SyncHelper;
import com.soapp.global.WaveLoadingView;
import com.soapp.home.Home;
import com.soapp.registration.country_codes.SliderDrawable.SliderDrawableAdapter;

import androidx.viewpager.widget.ViewPager;
import me.relex.circleindicator.CircleIndicator;

/**
 * Created by nikesh.chandarana on 19-Jun-17.
 * Override by joseph chang on today
 * Then heavily modified by Soapp later on
 */

public class SyncContact extends Activity implements View.OnClickListener {
    public SyncHelper syncHelper = new SyncHelper(this, "SyncContact");
    public static Button btnNext;
//    public static TextView sync_text2;

    public Preferences preferences = Preferences.getInstance();
    private static Integer[] tuto_imgs = {R.layout.tutorial_page1, R.layout.tutorial_page2, R.layout.tutorial_page3,
            R.layout.tutorial_page4, R.layout.tutorial_page5};
    private ViewPager mPager;
    private RelativeLayout tutorial_rv;
    public static WaveLoadingView logo_setting_sync;
    TextView sync_text;

    //public static status for sync contacts and tutorial
    //0 = hvn't seen tutorial and hvn't done syncing - first time action
    //1 = done both - go to Home
    //2 = done tutorial but failed syncing
    public static int syncStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_synccontact);

        //show user tutorial first
        startTutorial();

        //set preferences for next time coming in straight come to synccontact
        preferences.save(SyncContact.this, GlobalVariables.STRPREF_LOGIN_STATUS, "almostsuccessful");

        //save preferences for new user (for tutorial and other purposes)
        preferences.saveint(this, "newUser", 1);

        sync_text = findViewById(R.id.sync_text);
        logo_setting_sync = findViewById(R.id.logo_setting_sync2);
        logo_setting_sync.setProgressValue(0);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnNext.performClick();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1000:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(this, "dunAskagain", String.valueOf(showRationale));
                            break;
                        } else {
                            preferences.save(this, "dunAskagain", String.valueOf(showRationale));
                        }
                        syncHelper.new AsyncOldGourpBtn().execute();
                        break;
                    } else {
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }
                        syncHelper.new AsyncRegSyncBtn().execute();
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case (R.id.btn_next): //sync contacts btn
                switch (syncStatus) {
                    case 0: //first time - show tutorial and start syncing
                        //show tutorial
                        startTutorial();

                        //start syncing contacts + get old grps
                        startSync();
                        break;

                    case 1: //done ALL - got to home
                        goToHome();
                        break;

                    case 2: //done tutorial but FAILED syncing - sync again
                        startSync();
                        break;
                }
                break;

            case R.id.skip_btn: //skip or done btn - same function
                if (syncStatus == 1) { //done syncing, just go to home
                    goToHome();
                } else { //hvnt done syncing, just hide tutorial
                    tutorial_rv.setVisibility(View.GONE);
                }
                break;

        }

    }

    //function to start tutorial
    private void startTutorial() {
        tutorial_rv = findViewById(R.id.tutorial_rv);

        Button skip_btn = findViewById(R.id.skip_btn);
        skip_btn.setOnClickListener(this);

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new SliderDrawableAdapter(SyncContact.this, tuto_imgs));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 4) { //set btn to "done" on last page (page 5, index 4)
                    skip_btn.setText(R.string.done);
                } else {
                    skip_btn.setText(R.string.skip);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        tutorial_rv.setVisibility(View.VISIBLE);
    }

    //function for syncing ONLY
    private void startSync() {
        // no permissions
        if (PackageManager.PERMISSION_DENIED == this.checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS)) {
            new PermissionHelper().CheckPermissions(this, 1000, R.string.permission_txt);

        } else { //allow permissions
            syncHelper.new AsyncRegSyncBtn().execute();
        }

        //show progress in next button when syncing
        btnNext.setVisibility(View.INVISIBLE);
    }

    //function to start stream service and go to home
    private void goToHome() {
        preferences.save(this, GlobalVariables.STRPREF_LOGIN_STATUS, "successful");

        Intent IntentMain = new Intent(this, Home.class);
        startActivity(IntentMain);
        finish();
    }

    @Override
    protected void onDestroy() {
        syncStatus = 0;
        btnNext = null;
        logo_setting_sync.setProgressValue(0);

        super.onDestroy();
    }
}