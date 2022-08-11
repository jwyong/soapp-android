package com.soapp.registration;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soapp.R;
import com.soapp.SoappApi.ApiModel.NotificationRegisterTokenModel;
import com.soapp.SoappApi.Interface.RegisterInterface;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.home.Home;
import com.soapp.setup.Soapp;

import androidx.core.content.ContextCompat;
import io.fabric.sdk.android.Fabric;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Splash extends Activity {
    ImageView img_soapp_logo;
    Button btnRegister = null, btnTermsCondition = null;
    boolean isRegistered = false;

    private Preferences preferences = Preferences.getInstance();
    Thread mThread;

    public Splash() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = Preferences.getInstance();

        if (!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)

            //check directory anyway
            checkDirOnBackground();

            finish();
            return;
        }

        //init Fabric
        Fabric.with(this, new Crashlytics());

        checkLogin();
    }

    public void checkLogin() {
        mThread = new Thread(() -> {
            Looper.prepare();
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Splash.this,
                    instanceIdResult -> {
                        String refreshedPrefToken = preferences.getValue(Splash.this, GlobalVariables.FCM_TOKEN);

//                        if (!instanceIdResult.getToken().equals(refreshedPrefToken)) {
                            registerFcmTokenWithServer(instanceIdResult.getToken());
//                        }
                    });
            Looper.loop();
        });
        mThread.start();


        //start to do all required tasks
        checkDirOnBackground();

        //get screen density and set to global constant
        GlobalVariables.screenDensity = getResources().getDisplayMetrics().density;

        String access_token = preferences.getValue(Splash.this, GlobalVariables.STRPREF_ACCESS_TOKEN);
        String Jid = preferences.getValue(Splash.this, GlobalVariables.STRPREF_USER_ID);
        String LoginStatus = preferences.getValue(Splash.this, GlobalVariables.STRPREF_LOGIN_STATUS);

//        LoginStatus = "";

        System.out.println("jay = " + access_token);

        if (!access_token.equals("nil") && !Jid.equals("nil")) {
            switch (LoginStatus) {
                case "halfsuccessful": //UserProfile
                    Intent IntentMain = new Intent(Splash.this, UserProfile.class);
                    startActivity(IntentMain);

                    finish();
                    break;

                case "almostsuccessful": //SyncContact
                    Intent IntentMain2 = new Intent(Splash.this, SyncContact.class);
                    startActivity(IntentMain2);

                    finish();
                    break;

                case "successful": //Home
                    Intent IntentMain3 = new Intent(Splash.this, Home.class);
                    startActivity(IntentMain3);

                    finish();
                    break;

                default:
                    setContentView(R.layout.reg_splash);

                    //go to registration page after 1 second
                    new Handler(Looper.getMainLooper()).post(() -> {
                        final Handler handler = new Handler();
                        handler.postDelayed(this::gotToRegistration, 1000);
                    });
                    break;
            }
        } else {
            setContentView(R.layout.reg_splash);

            new Handler(Looper.getMainLooper()).post(() -> {
                final Handler handler = new Handler();
                handler.postDelayed(this::gotToRegistration, 1000);
            });
        }
    }

    public void gotToRegistration() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent_register = new Intent(Splash.this, Registration.class);
        startActivity(intent_register);

        finish();
    }

    //check storage if got permission
    private void checkDirOnBackground() {
        //check storage permissions everytime launch (if got storage permissions) on background thread
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Observable.just(Splash.this)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Splash>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Splash splash) {
                            new DirectoryHelper().checkAndCreateDir(Splash.this, false);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void registerFcmTokenWithServer(final String refresh_token) {
        String access_token = preferences.getValue(Soapp.getInstance().getApplicationContext(),
                GlobalVariables.STRPREF_ACCESS_TOKEN);

        if (access_token == null) {
            return;
        }

        NotificationRegisterTokenModel notificationRegisterTokenModel =
                new NotificationRegisterTokenModel(preferences.getValue(this, GlobalVariables.STRPREF_USER_ID), "fcm"
                        , refresh_token);
        RegisterInterface client = RetrofitAPIClient.getClient().create(RegisterInterface.class);
        retrofit2.Call<NotificationRegisterTokenModel> call =
                client.notificationRegisterToken(notificationRegisterTokenModel, "Bearer " + access_token);

        call.enqueue(new retrofit2.Callback<NotificationRegisterTokenModel>() {
            @Override
            public void onResponse(retrofit2.Call<NotificationRegisterTokenModel> call,
                                   retrofit2.Response<NotificationRegisterTokenModel> response) {
                if (!response.isSuccessful()) {
                    new MiscHelper().retroLogUnsuc(response, "registerFcmTokenWithServer ", "JAY");

                    return;
                }
                Log.d("wtf", "onResponse: ");
//                FcmTokenData.setFcmToken(getApplicationContext(), refresh_token);
                Preferences.getInstance().save(Splash.this, GlobalVariables.FCM_TOKEN, refresh_token);
            }

            @Override
            public void onFailure(retrofit2.Call<NotificationRegisterTokenModel> call, Throwable t) {
                new MiscHelper().retroLogFailure(t, "registerFcmTokenWithServer ", "JAY");
            }
        });
    }
}