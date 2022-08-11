package com.soapp.settings_tab;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;

/* Created by ash on 23/01/2018. */

public class DataUsage extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static boolean Itemswitch;
    Switch photos_data_switch, photos_wifi_switch, audio_data_switch, audio_wifi_switch, video_data_switch, video_wifi_switch;
    int PhotoData;
    private Preferences preferences = Preferences.getInstance();

    //    boolean Itemswitch;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_data_usage);
        setupToolbar();
        new UIHelper().setStatusBarColor(this , false , R.color.black3a);

        photos_data_switch = findViewById(R.id.photos_data_switch);
        photos_wifi_switch = findViewById(R.id.photos_wifi_switch);

        video_data_switch = findViewById(R.id.video_data_switch);
        video_wifi_switch = findViewById(R.id.video_wifi_switch);

        audio_data_switch = findViewById(R.id.audio_data_switch);
        audio_wifi_switch = findViewById(R.id.audio_wifi_switch);

        photos_data_switch.setOnClickListener(this);
        photos_wifi_switch.setOnClickListener(this);

        video_data_switch.setOnClickListener(this);
        video_wifi_switch.setOnClickListener(this);

        audio_data_switch.setOnClickListener(this);
        audio_wifi_switch.setOnClickListener(this);

        //ddefault data is off, while default wifi is on for both image and video
        String photoData = preferences.getValue(DataUsage.this, "PData");
        if (!photoData.equals("nil")) {
            if (photoData.equals("off")) {
                photos_data_switch.setChecked(false);
            } else {
                photos_data_switch.setChecked(true);
            }
        } else {
            photos_data_switch.setChecked(false);
            preferences.save(DataUsage.this, "PData", "off");
        }

        String photoWifi = preferences.getValue(DataUsage.this, "PWifi");
        if (!photoWifi.equals("nil")) {
            if (photoWifi.equals("off")) {
                photos_wifi_switch.setChecked(false);
            } else {
                photos_wifi_switch.setChecked(true);
            }
        } else {
            photos_wifi_switch.setChecked(true);
            preferences.save(DataUsage.this, "PWifi", "on");
        }
        //photo end

        //video
        String videoData = preferences.getValue(DataUsage.this, "VData");
        if (!videoData.equals("nil")) {
            if (videoData.equals("off")) {
                video_data_switch.setChecked(false);
            } else {
                video_data_switch.setChecked(true);
            }
        } else {
            video_data_switch.setChecked(false);
            preferences.save(DataUsage.this, "VData", "off");
        }

        String videoWifi = preferences.getValue(DataUsage.this, "VWifi");
        if (!videoWifi.equals("nil")) {
            if (videoWifi.equals("off")) {
                video_wifi_switch.setChecked(false);
            } else {
                video_wifi_switch.setChecked(true);
            }
        } else {
            video_wifi_switch.setChecked(true);
            preferences.save(DataUsage.this, "VWifi", "on");
        }
        // video end

        //audio
        String audioData = preferences.getValue(DataUsage.this, "AData");
        if (!audioData.equals("nil")) {
            if (audioData.equals("off")) {
                audio_data_switch.setChecked(false);
            } else {
                audio_data_switch.setChecked(true);
            }
        } else {
            audio_data_switch.setChecked(true);
            preferences.save(DataUsage.this, "AData", "on");
        }

        String audioWifi = preferences.getValue(DataUsage.this, "AWifi");
        if (!audioWifi.equals("nil")) {
            if (audioWifi.equals("off")) {
                audio_wifi_switch.setChecked(false);
            } else {
                audio_wifi_switch.setChecked(true);
            }
        } else {
            audio_wifi_switch.setChecked(true);
            preferences.save(DataUsage.this, "AWifi", "on");
        }
        //audio end

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            //photo
            case R.id.photos_data_switch:
                if (photos_data_switch.isChecked()) {
                    preferences.save(DataUsage.this, "PData", "on");
                } else {
                    preferences.save(DataUsage.this, "PData", "off");
                }
                break;

            case R.id.photos_wifi_switch:
                if (photos_wifi_switch.isChecked()) {
                    preferences.save(DataUsage.this, "PWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "PWifi", "off");
                }
                break;
            //photo end
            //video
            case R.id.video_data_switch:
                if (video_data_switch.isChecked()) {
                    preferences.save(DataUsage.this, "VData", "on");
                } else {
                    preferences.save(DataUsage.this, "VData", "off");
                }
                break;

            case R.id.video_wifi_switch:
                if (video_wifi_switch.isChecked()) {
                    preferences.save(DataUsage.this, "VWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "VWifi", "off");
                }
                break;
            //video end
            //audio
            case R.id.audio_data_switch:
                if (audio_data_switch.isChecked()) {
                    preferences.save(DataUsage.this, "AData", "on");
                } else {
                    preferences.save(DataUsage.this, "AData", "off");
                }
                break;

            case R.id.audio_wifi_switch:
                if (audio_wifi_switch.isChecked()) {
                    preferences.save(DataUsage.this, "AWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "AWifi", "off");
                }
                break;
            //audio end

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
        switch (compoundButton.getId()) {
            //photo
            case R.id.photos_data_switch:
                if (on) {
                    //do stuff when Switch is ON
                    preferences.save(DataUsage.this, "PData", "on");
                } else {
                    //do stuff when Switch if OFF
                    preferences.save(DataUsage.this, "PData", "off");
                }
                break;

            case R.id.photos_wifi_switch:
                if (on) {
                    preferences.save(DataUsage.this, "PWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "PWifi", "off");
                }
                break;
            //photo end
            //video
            case R.id.video_data_switch:
                if (on) {
                    preferences.save(DataUsage.this, "VData", "on");
                } else {
                    preferences.save(DataUsage.this, "VData", "off");
                }
                break;

            case R.id.video_wifi_switch:
                if (on) {
                    preferences.save(DataUsage.this, "VWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "VWifi", "off");
                }
                break;
            //video end
            //audio
            case R.id.audio_data_switch:
                if (on) {
                    preferences.save(DataUsage.this, "AData", "on");
                } else {
                    preferences.save(DataUsage.this, "AData", "off");
                }
                break;

            case R.id.audio_wifi_switch:
                if (audio_wifi_switch.isChecked()) {
                    preferences.save(DataUsage.this, "AWifi", "on");
                } else {
                    preferences.save(DataUsage.this, "AWifi", "off");
                }
                break;
            //audio end
        }
    }
}

