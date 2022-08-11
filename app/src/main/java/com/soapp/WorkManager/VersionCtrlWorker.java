package com.soapp.WorkManager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.soapp.BuildConfig;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.VersionControlModel;
import com.soapp.SoappApi.Interface.SpecialFunctions;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.StateCheck;
import com.soapp.home.Home;
import com.soapp.setup.Soapp;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import retrofit2.Call;
import retrofit2.Response;

public class VersionCtrlWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        Context context = Soapp.getInstance().getApplicationContext();
        Preferences.getInstance().save(context, "versionContCode", "1");

        String versionCode = BuildConfig.VERSION_NAME;
        String access_token = Preferences.getInstance().getValue(context, GlobalVariables.STRPREF_ACCESS_TOKEN);
        //build retrofit
        SpecialFunctions client = RetrofitAPIClient.getClient().create(SpecialFunctions.class);
        retrofit2.Call<VersionControlModel> call = client.versionControl(versionCode, "android",
                "Bearer " + access_token);
        call.enqueue(new retrofit2.Callback<VersionControlModel>() {
            @Override
            public void onResponse(Call<VersionControlModel> call, Response<VersionControlModel> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "SoappJobScheduler  ", "JAY");

//                    if (!StateCheck.foreground) { //user NOT in foreground, kill app
//                        int pid = android.os.Process.myPid();
//                        android.os.Process.killProcess(pid);
//                    }
                    return;
                }

                //save code to preferences for splash checking
                int code = response.body().getCode();
                int remaining = response.body().getRemaining_days();
                Preferences.getInstance().save(context, "remaining", String.valueOf(remaining));
                Preferences.getInstance().save(context, "versionContCode", String.valueOf(code));

                if (StateCheck.foreground) { //user in foreground, need show alert
                    switch (code) {
                        case 2: //expires in xx days
                            new Handler(Looper.getMainLooper())
                                    .post(() -> Home.showVerContAlert(Home.activity, context
                                            .getString(R.string
                                                    .update_half_expired_msg), false, String.valueOf(remaining)));
                            break;

                        case 3: //expiring in xx days, but need to FORCE user to re-register
                            break;

                        case 4: //expired, need update
                            new Handler(Looper.getMainLooper())
                                    .post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Home.showVerContAlert(Home.activity, context
                                                    .getString(R.string.update_expired_msg), true, "0");
                                        }
                                    });
                            break;

                        case 5: //expired and FORCE user to re-register
                            break;

                        default: //no need update
                            break;
                    }
                } else { //app in background, kill app
//                    int pid = android.os.Process.myPid();
//                    android.os.Process.killProcess(pid);
                }
            }

            @Override
            public void onFailure(Call<VersionControlModel> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "SoappJobScheduler  ", "JAY");

//                if (!StateCheck.foreground) { //user NOT in foreground, kill app
//                    int pid = android.os.Process.myPid();
//                    android.os.Process.killProcess(pid);
//                }
            }
        });
        
        return Result.SUCCESS;
    }
}
