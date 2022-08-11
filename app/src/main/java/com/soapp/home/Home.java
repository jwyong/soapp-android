package com.soapp.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.soapp.BuildConfig;
import com.soapp.R;
import com.soapp.SoappApi.Interface.QuestInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.chat_class.Chatlist.ChatTab;
import com.soapp.food.FoodTab;
import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.GlobalVariables;
import com.soapp.global.HomeTabHelper;
import com.soapp.global.PowerSaverHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SharingHelper;
import com.soapp.global.SyncHelper;
import com.soapp.global.UIHelper;
import com.soapp.new_chat_schedule.group.NewGrpChatController;
import com.soapp.new_chat_schedule.individual.NewChatController;
import com.soapp.schedule_class.Schedulelist.ScheduleTab;
import com.soapp.settings_tab.SettingsTab;
import com.soapp.setup.Soapp;
import com.soapp.soapp_tab.SoappTab;
import com.soapp.soapp_tab.contact.ContactActivity;
import com.soapp.sql.DatabaseHelper;
import com.soapp.xmpp.CheckSmackHelper;
import com.soapp.xmpp.connection.SoappService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import io.branch.referral.Branch;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity {

    public static Activity activity;
    public static TabLayout tabLayout;
    private Preferences preferences = Preferences.getInstance();

    //badge ryan
    private static DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    private static int chatCount;
    private static int apptCount, iconCount, ic_menubar_home_grey_new;

    // handle permission sync
    public SyncHelper syncHelper = new SyncHelper(this, "normalSync");

    //boolean for showing all 5 tabs
    public static boolean showAllTabs = true;

    private int chatPos;
    private int apptPos;
    private int[] intArrayIcons;
    private int[] intArrayIconsSelected;

    private ViewPager viewPager;

    //for passing variables to fragment
    public ChatTabComm chatTabComm;
    public ScheTabComm scheTabComm;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //start contacts service
        Intent soappservice = new Intent(Soapp.getInstance(), SoappService.class);
        startService(soappservice);

        setContentView(R.layout.home_tab);

        //msia, show all tabs, not msia, show 3 tabs only
        showAllTabs = preferences.getValue(this, GlobalVariables.STRPREF_COUNTRY_CODE).equals("60");

        activity = this;

        //set home tabs 5 or 3 depending on msia or not
        if (showAllTabs) { //5 tabs
            intArrayIcons = new int[]{
                    R.drawable.ic_menubar_home_grey_120px,
                    R.drawable.ic_menubar_search_grey_120px,
                    R.drawable.ic_menubar_chat_grey_120px,
                    R.drawable.ic_menubar_schedule_grey_120px,
                    R.drawable.ic_menubar_setting_grey_120px};

            intArrayIconsSelected = new int[]{
                    R.drawable.ic_menubar_green_home_120px,
                    R.drawable.ic_menubar_search_green_120px,
                    R.drawable.ic_menubar_chat_green_120px,
                    R.drawable.ic_menubar_schedule_green_120px,
                    R.drawable.ic_menubar_setting_green_120px};

            chatPos = 2;
            apptPos = 3;

        } else { //3 tabs
            intArrayIcons = new int[]{
                    R.drawable.ic_menubar_search_grey_120px,
                    R.drawable.ic_menubar_schedule_grey_120px,
                    R.drawable.ic_menubar_setting_grey_120px};

            intArrayIconsSelected = new int[]{
                    R.drawable.ic_menubar_search_green_120px,
                    R.drawable.ic_menubar_schedule_green_120px,
                    R.drawable.ic_menubar_setting_green_120px};

            chatPos = 0;
            apptPos = 1;

        }
        inUit();

        //version control msg - show alert if versionContCode is 0 (expired)
        //for code = 2, will update daily when doing wake alarm
        String code = preferences.getValue(this, "versionContCode");
        if (code != null && code.equals("0")) {
            showVerContAlert(this, getString(R.string.update_expired_msg), true, "0");
        }

        //for sharing deep link posting to retro
        String sharer_jid = preferences.getValue(Home.this, GlobalVariables.STR_SHARER);
        if (!sharer_jid.equals("nil") && sharer_jid.length() == 12) { //make sure it's a jid
            questInviteFriendRetro(sharer_jid);
        }

        //do special ops only if NOT new user
        Intent intent = PowerSaverHelper.prepareIntentForWhiteListingOfBatteryOptimization(this, getPackageName(), false);
        if (intent != null) {
            startActivity(intent);
        }

        switch (preferences.getIntValue(Home.this, "newUser")) {
            case 1: //brand new user - show tute: swipe, new chat/sche
                welcomeInvite();

                ConstraintLayout tute_swipe_cl = findViewById(R.id.tute_swipe_cl);
                tute_swipe_cl.setVisibility(View.VISIBLE);

                tute_swipe_cl.setOnTouchListener((view, motionEvent) -> {
                    fadeOutView(tute_swipe_cl);

                    //action for new chat/sche
                    ConstraintLayout tute_new_chat_sche = findViewById(R.id.tute_new_chat_sche);
                    fadeInView(tute_new_chat_sche);

                    tute_new_chat_sche.setOnClickListener(view13 -> {
                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 2);
                    });

                    //dismiss btn
                    TextView home_new_chat_dismiss = findViewById(R.id.home_new_chat_dismiss);
                    home_new_chat_dismiss.setOnClickListener(view12 -> {
                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 2);
                    });

                    //new chat btn
                    CardView home_new_chat_sche_btn = findViewById(R.id.home_new_chat_sche_btn);
                    home_new_chat_sche_btn.setOnClickListener(view1 -> {
                        new HomeTabHelper().startNewChat(this);

                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 2);
                    });
                    return true;
                });
                break;

            default: //existing users
                int prevVerCode1 = preferences.getIntValue(this, GlobalVariables.prevVerCode);
                int currentVerCode1 = BuildConfig.VERSION_CODE;

                if (currentVerCode1 > prevVerCode1) { //only do spOps if updated
                    //do special operations
//                    new SpecialOps().executeSpOps( this, prevVerCode1, currentVerCode1);
                }
                break;
        }

        CheckSmackHelper.getInstance().check();
        CheckInternetAvaibility.xmppauth.observe(this, aBoolean -> findViewById(R.id.xmppcheck).setVisibility(aBoolean ? View.GONE : View.VISIBLE));
    }

    //animation for fade-in and fade-out
    private void fadeOutView(View view) {
        view.animate().alpha(0).setDuration(500);
        view.setEnabled(false);

        Handler handler = new Handler();
        Runnable runnable = () -> view.setVisibility(View.GONE);
        handler.postDelayed(runnable, 500);
    }

    private void fadeInView(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0);
        view.animate().alpha(1).setDuration(500);
    }

    public void inUit() {
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        setTabIcon();

        if (getIntent().hasExtra("remoteSche")) {
            TabLayout.Tab tab = tabLayout.getTabAt(apptPos);
            View v = tab.getCustomView();
            ImageView im = v.findViewById(R.id.custom_tab_imgbtn);
            im.setImageResource(intArrayIconsSelected[apptPos]);
//            TextView txtv = v.findViewById(R.id.custom_tab_txtv);
//            txtv.setTextColor(ContextCompat.getColor(this, R.color.primaryDark4));
            tab.setIcon(intArrayIconsSelected[apptPos]);
            tab.select();

            //remove "remoteSche" so that won't come back to appt tab after 1 time
            getIntent().removeExtra("remoteSche");

        } else {
            TabLayout.Tab tab = tabLayout.getTabAt(chatPos);
            View v = tab.getCustomView();
            ImageView im = v.findViewById(R.id.custom_tab_imgbtn);
            im.setImageResource(intArrayIconsSelected[chatPos]);
//            TextView txtv = v.findViewById(R.id.custom_tab_txtv);
//            txtv.setTextColor(ContextCompat.getColor(this, R.color.primaryDark4));
            tab.setIcon(intArrayIconsSelected[chatPos]);
            tab.select();
        }
        chatCount = databaseHelper.getChatNotiForChatTab();
        apptCount = databaseHelper.getApptNotiForScheTab();

        if (chatCount > 0) {
            TabLayout.Tab tab = tabLayout.getTabAt(chatPos);
            View v = tab.getCustomView();
            TextView notiTxtView = v.findViewById(R.id.noti_badge);
            notiTxtView.setText(String.valueOf(chatCount));
            notiTxtView.setVisibility(View.VISIBLE);
        }

        if (apptCount > 0) {
            TabLayout.Tab tab = tabLayout.getTabAt(apptPos);
            View v = tab.getCustomView();
            TextView notiTxtView = v.findViewById(R.id.noti_badge);
            notiTxtView.setText(String.valueOf(apptCount));
            notiTxtView.setVisibility(View.VISIBLE);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView im = v.findViewById(R.id.custom_tab_imgbtn);

                int tabIndex = tab.getPosition();
                im.setImageResource(intArrayIconsSelected[tabIndex]);
                tab.setIcon(intArrayIconsSelected[tabIndex]);

                //set "new chat" or "new sche" if newUser = 2
                if (preferences.getIntValue(Home.this, "newUser") == 2 && tabIndex == apptPos) {
                    ConstraintLayout tute_new_chat_sche = findViewById(R.id.tute_new_chat_sche);
                    ImageView img = findViewById(R.id.img_icon);
                    TextView popup_msg2 = findViewById(R.id.popup_msg2);
                    TextView home_new_chat_dismiss = findViewById(R.id.home_new_chat_dismiss);
                    CardView home_new_chat_sche_btn = findViewById(R.id.home_new_chat_sche_btn);

                    //set function for dimissing view on click
                    fadeInView(tute_new_chat_sche);
                    home_new_chat_dismiss.setOnClickListener(view -> {
                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 0);
                    });

                    tute_new_chat_sche.setOnClickListener(view13 -> {
                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 2);
                    });

                    img.setImageResource(R.drawable.ic_menubar_schedule_green_280px);
                    popup_msg2.setText("NEW SCHEDULE");

                    home_new_chat_sche_btn.setOnClickListener(view1 -> {
                        new HomeTabHelper().startNewSche(Home.this, 0);

                        fadeOutView(tute_new_chat_sche);

                        preferences.saveint(Home.this, "newUser", 2);
                    });

//                    } else if (tabIndex == chatPos) { //chattab
//                        img.setImageResource(R.drawable.ic_menubar_chat_green_280px);
//                        popup_msg2.setText("NEW CHAT");
//
//                        home_new_chat_sche_btn.setOnClickListener(view1 -> {
//                            new HomeTabHelper().startNewChat(Home.this);
//
//                            fadeOutView(tute_new_chat_sche);
//
//                            preferences.saveint(Home.this, "newUser", 2);
//                        });
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView im = v.findViewById(R.id.custom_tab_imgbtn);
//                TextView txtv = v.findViewById(R.id.custom_tab_txtv);

                int tabIndex = tab.getPosition();
                im.setImageResource(intArrayIcons[tabIndex]);
                tab.setIcon(intArrayIcons[tabIndex]);
//                txtv.setTextColor(ContextCompat.getColor(Home.this, R.color.grey9));

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Branch logging for debugging
//        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);

        WorkManager.getInstance().getStatusesByTag("ReminderAppt").observe(this, workStatuses -> {
            for (WorkStatus workStatus : workStatuses) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    public static void chatBadgeListener() {
        if (databaseHelper != null && tabLayout != null) {
            chatCount = 0;
            chatCount = databaseHelper.getChatNotiForChatTab();

            TabLayout.Tab tab;

            if (showAllTabs) {
                tab = tabLayout.getTabAt(2);
            } else {
                tab = tabLayout.getTabAt(0);
            }

            View v = tab.getCustomView();
            TextView notiTxtView = v.findViewById(R.id.noti_badge);
            if (chatCount > 0) {
                notiTxtView.setText(String.valueOf(chatCount));
                notiTxtView.setVisibility(View.VISIBLE);
            } else {
                notiTxtView.setVisibility(View.GONE);
            }
        }
    }

    public static void scheBadgeListener() {
        if (databaseHelper != null && tabLayout != null) {
            apptCount = 0;
            apptCount = databaseHelper.getApptNotiForScheTab();

            TabLayout.Tab tab;

            if (showAllTabs) {
                tab = tabLayout.getTabAt(3);
            } else {
                tab = tabLayout.getTabAt(1);
            }

            View v = tab.getCustomView();
            TextView notiTxtView = v.findViewById(R.id.noti_badge);
            if (apptCount > 0) {
                notiTxtView.setText(String.valueOf(apptCount));
                notiTxtView.setVisibility(View.VISIBLE);
            } else {
                notiTxtView.setVisibility(View.GONE);
            }
        }
    }

    //show msg for version control
    public static void showVerContAlert(final Activity activity, String msg, final boolean
            isExpired, final String remaining) {

        //action for allow notification
        Runnable pushNotificationPositive = () -> {
            Uri uri = Uri.parse("market://details?id=com.soapp");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(Soapp.getInstance().getApplicationContext(), R.string.update_failed,
                        Toast.LENGTH_SHORT).show();
            }

            if (isExpired) { //kill soapp if expired
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        };

        //action for cancel notification
        Runnable pushNotificationNegative = () -> {
            if (isExpired) { //kill soapp then go to update page

                Uri uri = Uri.parse("market://details?id=com.soapp");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(Soapp.getInstance().getApplicationContext(), R.string
                            .update_failed, Toast.LENGTH_SHORT).show();
                }

                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            } else { //show remaining days
                String toast = activity.getString(R.string.update_half_expired_cancel) +
                        " " + remaining + " " + activity.getString(R.string.days);
                Toast.makeText(activity, toast, Toast.LENGTH_LONG)
                        .show();
            }

        };

        new UIHelper().dialog2Btns(activity,
                activity.getString(R.string.new_ver_avail),
                msg,
                R.string.update,
                R.string.cancel,
                R.color.white,
                R.color.primaryDark3,
                pushNotificationPositive,
                pushNotificationNegative,
                false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra("remoteSche")) {
            TabLayout.Tab tab = tabLayout.getTabAt(apptPos);
            View v = tab.getCustomView();
            ImageView im = v.findViewById(R.id.custom_tab_imgbtn);
            im.setImageResource(intArrayIconsSelected[apptPos]);
//            TextView txtv = v.findViewById(R.id.custom_tab_txtv);
//            txtv.setTextColor(ContextCompat.getColor(this, R.color.primaryDark4));
            tab.setIcon(intArrayIconsSelected[apptPos]);
            tab.select();

            //remove "remoteSche" so that won't come back to appt tab after 1 time
            getIntent().removeExtra("remoteSche");

        }
    }

    private void setupViewPager(ViewPager viewPager) {
        SyncTabAdapter adapter = new SyncTabAdapter(this.getSupportFragmentManager());

        if (showAllTabs) {
            adapter.addFragment(new SoappTab(), this.getString(R.string.contacts));
            adapter.addFragment(new FoodTab(), this.getString(R.string.food));
        }

        adapter.addFragment(new ChatTab(), this.getString(R.string.chat));
        adapter.addFragment(new ScheduleTab(), this.getString(R.string.schedule));
        adapter.addFragment(new SettingsTab(), this.getString(R.string.settings));

        viewPager.setAdapter(adapter);
    }

    public void setTabIcon() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
    }

    private View prepareTabView(final int i) {
        View view = getLayoutInflater().inflate(R.layout.home_tab_item, null);
        ImageView imgbtn = view.findViewById(R.id.custom_tab_imgbtn);
        imgbtn.setImageResource(intArrayIcons[i]);

        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabLayout.Tab clickTab = tabLayout.getTabAt(i);
                clickTab.select();

            }
        });
//        TextView txtv = view.findViewById(R.id.custom_tab_txtv);
//        txtv.setText(tabsTitle[i]);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1007:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);

                        }
                        if (!showRationale) {
                            preferences.save(this, "askAgain", String.valueOf(showRationale));
                            break;
                        } else {
                            preferences.save(this, "askAgain", String.valueOf(showRationale));
                        }

                    } else {

                        syncHelper.new AsyncContactbtn().execute();

                        String permisssion_contact = preferences.getValue(this, "contactPermission");
                        switch (permisssion_contact) {
                            case "chat":
                                Intent intent = new Intent(this, NewChatController.class);
                                intent.putExtra("From", "chat");
                                startActivity(intent);
                                break;

                            case "groupchat":
                                Intent intent2 = new Intent(this, NewGrpChatController.class);
                                intent2.putExtra("From", "groupchat");
                                startActivity(intent2);

                                break;

                            case "schedule":

                                Long dwname = Long.valueOf(preferences.getValue(this, "dwn"));
                                Intent schedule = new Intent(activity, NewChatController.class);
                                schedule.putExtra("From", "schedule");
                                schedule.putExtra("Date", dwname);
                                startActivity(schedule);

                                break;
                            case "groupschedule":
                                Long dwname1 = Long.valueOf(preferences.getValue(this, "dwn"));
                                Intent groupschedule = new Intent(activity, NewGrpChatController.class);
                                groupschedule.putExtra("From", "schedule");
                                groupschedule.putExtra("Date", dwname1);
                                startActivity(groupschedule);
                                break;

                            case "homeContact":
                                Intent contactAct = new Intent(this, ContactActivity.class);
                                startActivity(contactAct);

                                break;
                        }

                        break;
                    }
                }
                break;
        }
    }

    //for search bar
    public interface ChatTabComm {
        boolean isChatSearchIconified();

        void closeChatTabSearch();
    }

    public interface ScheTabComm {
        boolean isScheSearchIconified();

        void closeScheTabSearch();
    }

    public void questInviteFriendRetro(String user_jid) {
        String access_token = preferences.getValue(Home.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        QuestInterface client = RetrofitAPIClient.getClient().create(QuestInterface.class);
        retrofit2.Call<ResponseBody> call = client.questInviteFriendsApi(user_jid, "Bearer " + access_token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Home.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
                    return;
                }

                //ask friend to share to more friends
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Home.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //function for showing "welcome, invite friends"
    private void welcomeInvite() {
        Runnable shareToFriends = () -> {
            new SharingHelper().shareSoappToFriends(Home.this, findViewById(R.id.progress_bar_cl), null);
        };

        new UIHelper().dialog1Btn(Home.this, getString(R.string.welcome), getString(R.string.welcome_msg),
                R.string.invite_friends, R.color.primaryDark3, shareToFriends, true, true);
    }

    @Override
    public void onBackPressed() {
        if (!chatTabComm.isChatSearchIconified()) { //not iconified means is searching, close search
            chatTabComm.closeChatTabSearch();

        } else if (!scheTabComm.isScheSearchIconified()) {
            scheTabComm.closeScheTabSearch();

        } else { //do navigate action only if user NOT searching

            if (tabLayout.getSelectedTabPosition() == chatPos) {
                moveTaskToBack(true);
            } else {
                TabLayout.Tab tab = tabLayout.getTabAt(chatPos);
                tab.select();
            }
        }
    }
}