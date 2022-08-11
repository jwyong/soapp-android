package com.soapp.settings_tab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.Interface.SpecialFunctions;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.base.BaseActivity;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.SyncHelper;
import com.soapp.global.UIHelper;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/* Created by Nurfaiza on 25/04/2018. */


public class AdditionalSettings extends BaseActivity {
    View view;

    private TextView notification, dateusage, oldgroup, cardview_delete_acc;

    //devops: delete soapp acc
    private int devDelAcc = 0;
    private CardView delete_card;

    //single instance

    private Preferences preferences = Preferences.getInstance();
    public SyncHelper syncHelper = new SyncHelper(this, "normalSync");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_tab_additionals_settings);
        setupToolbar();

        new UIHelper().setStatusBarColor(this, false, R.color.black3a);
        notification = findViewById(R.id.rl_cardview_noticationsettings);
        dateusage = findViewById(R.id.cardview_dataandusage);
        oldgroup = findViewById(R.id.cardview_retrieveoldgroup);
        cardview_delete_acc = findViewById(R.id.cardview_delete_acc);
        delete_card = findViewById(R.id.delete_card);

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Notifications.class);
            startActivity(intent);
        });

        dateusage.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), DataUsage.class);
            startActivity(intent);
        });

        oldgroup.setOnClickListener(v -> {
            Runnable checkOldGrpsAction = () -> syncHelper.new AsyncOldGourpBtn().execute();

            new UIHelper().dialog2Btns(this, getString(R.string.settings_joined_grp), getString(R
                            .string.settings_joined_grp_msg), R.string.ok_label, R.string.cancel, R.color.white,
                    R.color.black, checkOldGrpsAction, null, true);
        });

        //devOps: delAcc
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(v -> {
            if (devDelAcc == 9) {
                Toast.makeText(this, "danger!", Toast.LENGTH_SHORT).show();

                delete_card.setVisibility(View.VISIBLE);
                cardview_delete_acc.setOnClickListener(view -> {
                    Runnable deleteAccAction1 = () -> {
                        Runnable deleteAccAction2 = this::deleteAcc;

                        new UIHelper().dialog2Btns(AdditionalSettings.this, getString(R.string.settings_delete_acc), getString(R
                                        .string.delete_acc_msg2), R.string.delete, R.string.cancel, R.color.red,
                                R.color.black, deleteAccAction2, null, true);
                    };

                    new UIHelper().dialog2Btns(this, getString(R.string.settings_delete_acc), getString(R
                                    .string.delete_acc_msg), R.string.ok_label, R.string.cancel, R.color.red,
                            R.color.black, deleteAccAction1, null, true);
                });
                devDelAcc = 0;

            } else {
                devDelAcc++;
            }
        });
    }

    private void deleteAcc() {
        String access_token = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String userJid = Preferences.getInstance().getValue(this, GlobalVariables.STRPREF_USER_ID);

        //build retrofit
        SpecialFunctions client = RetrofitAPIClient.getClient().create(SpecialFunctions.class);
        retrofit2.Call<ResponseBody> call = client.deleteAcc("Bearer " + access_token, userJid);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "deleteAcc", "JAY");
                    Toast.makeText(AdditionalSettings.this, "Looks like our server doesn't want you to go", Toast.LENGTH_SHORT).show();
                    return;
                }
                //delete pref
                preferences.save(AdditionalSettings.this, GlobalVariables.STRPREF_LOGIN_STATUS, "");

                Toast.makeText(AdditionalSettings.this, "Your account has been deleted - kill or uninstall your app", Toast.LENGTH_SHORT).show();

//
//
//                //restart soapp
//                Intent mStartActivity = new Intent(AdditionalSettings.this, Splash.class);
//                int mPendingIntentId = android.os.Process.myPid();
//
//                PendingIntent mPendingIntent = PendingIntent.getActivity(AdditionalSettings.this, mPendingIntentId, mStartActivity,
//                        PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager) AdditionalSettings.this.getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//
//                android.os.Process.killProcess(mPendingIntentId);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "deleteAcc", "JAY");

                Toast.makeText(AdditionalSettings.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

