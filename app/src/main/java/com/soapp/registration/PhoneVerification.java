package com.soapp.registration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.AuthenticateModel;
import com.soapp.SoappApi.ApiModel.RegisterModel;
import com.soapp.SoappApi.ApiModel.RegisterUser;
import com.soapp.SoappApi.Interface.AuthenticateInterface;
import com.soapp.SoappApi.Interface.RegisterInterface;
import com.soapp.SoappApi.Interface.RegisterPn;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.CheckInternetAvaibility;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;

import java.util.Locale;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class PhoneVerification extends Activity implements View.OnClickListener {

    public static TextView txt_timer;
    public static CountDownTimer timer;
    public static boolean isTimerStart_Running = false;
    static boolean isRunning = true;
    static EditText edtxt1;
    Boolean isDone = false;
    private Button btn_continue = null;
    private final TextWatcher OTPNumber1 = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 6) {
                edtxt1.setEnabled(true);
                edtxt1.requestFocus();
            }
            checkBtnCotinueEnable();

        }

        public void afterTextChanged(Editable s) {
        }
    };
    private ProgressDialog dialog;
    private Preferences preferences = null;
    private ViewGroup verifyreg;

    //sms receiver
    private SmsReceiver smsReceiver = new SmsReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_phoneverification);

        //register sms receiver first
        registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        inUit();

        edtxt1 = findViewById(R.id.edtxt1);
        timer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                isTimerStart_Running = true;

                txt_timer.setText("0" + ((int) ((millisUntilFinished / (1000 * 60)) % 60))
                        + ":" + String.format(Locale.ENGLISH, "%02d", ((int) (millisUntilFinished /
                        1000) % 60)));
            }

            public void onFinish() {
                isTimerStart_Running = false;
                txt_timer.setText(R.string.pVResend);
                txt_timer.setEnabled(true);
            }
        };

        txt_timer.setEnabled(false);
        timer.start();

        edtxt1.setOnKeyListener((v, keyCode, event) -> {
            checkBtnCotinueEnable();

            return false;
        });
    }

    //receive sms automatically
    public void receiveSms(String message) {
        try {
            edtxt1.setText(message);
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_continue:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (CheckInternetAvaibility.gotInternet) {
                    new asyncRegAuth().execute();
                }
                break;

            case R.id.txt_timer:
                clearAllInput();

                InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm2.hideSoftInputFromWindow(v.getWindowToken(), 0);

                dialog = ProgressDialog.show(this, null, "Sending confirmation code");

                String strCountryCode = preferences.getValue(PhoneVerification.this, GlobalVariables.STRPREF_COUNTRY_CODE);
                String STRPREF_PHONE_NUMBER = preferences.getValue(PhoneVerification.this, GlobalVariables.STRPREF_PHONE_NUMBER);
                RegisterUser user = new RegisterUser(strCountryCode, STRPREF_PHONE_NUMBER.trim(), 1);

                //build retrofit
                RegisterPn client = RetrofitAPIClient.getClient().create(RegisterPn.class);
                retrofit2.Call<RegisterUser> call = client.registerPhoneNumber(user);

                call.enqueue(new retrofit2.Callback<RegisterUser>() {
                    @Override
                    public void onResponse(retrofit2.Call<RegisterUser> call, retrofit2.Response<RegisterUser> response) {
                        if (!response.isSuccessful()) {

                            new MiscHelper().retroLogUnsuc(response, "sendOTP", "JAY");

                            Toast.makeText(PhoneVerification.this, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            return;
                        }

                        //start timer again on response
                        txt_timer.setEnabled(false);
                        if (timer != null) {
                            timer.start();
                        }

                        String token = response.body().getToken();
                        preferences.save(PhoneVerification.this, GlobalVariables.STRPREF_USER_TOKEN, token);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<RegisterUser> call, Throwable t) {

                        new MiscHelper().retroLogFailure(t, "sendOTP", "JAY");

                        Toast.makeText(PhoneVerification.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
                break;

            case (R.id.verifyreg):

                new UIHelper().closeKeyBoard(this);


                break;
            default:
                break;
        }

    }

    public void inUit() {
        preferences = Preferences.getInstance();

        verifyreg = findViewById(R.id.verifyreg);

        btn_continue = findViewById(R.id.btn_continue);
        txt_timer = findViewById(R.id.txt_timer);
        edtxt1 = findViewById(R.id.edtxt1);

        verifyreg.setOnClickListener(this);

        txt_timer.setOnClickListener(this);
        btn_continue.setOnClickListener(this);

        edtxt1.addTextChangedListener(OTPNumber1);

        btn_continue.setEnabled(false);
    }

    public void checkBtnCotinueEnable() {
        if (edtxt1.getText().toString().trim().length() == 6) { //can click continue
            btn_continue.setEnabled(true);
            btn_continue.setBackgroundResource(R.drawable.xml_roundcorner_primarylogo);

            if (!isTimerStart_Running) {
                txt_timer.setEnabled(true);
            } else {
                txt_timer.setEnabled(false);
            }

            if (!isDone) {
                btn_continue.performClick();
                isDone = true;
            }
        } else { //can't click continue
            btn_continue.setEnabled(false);
            btn_continue.setBackgroundResource(R.drawable.xml_roundcorner_tint);

            if (!isTimerStart_Running) {
                txt_timer.setEnabled(true);
            } else {
                txt_timer.setEnabled(false);
            }
        }
    }

    private void clearAllInput() {
        edtxt1.setText("");
        btn_continue.setEnabled(false);
    }

    //async register and auth
    private class asyncRegAuth extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progressDialog before download starts
            dialog = ProgressDialog.show(PhoneVerification.this, null, "Sending " +
                    "confirmation code");

            txt_timer.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (timer != null) {
                timer.start();
            }

            final String smsCode = edtxt1.getText().toString().trim();
            final String userToken = preferences.getValue(PhoneVerification.this, GlobalVariables.STRPREF_USER_TOKEN);

            final String password = Settings.Secure.getString(PhoneVerification.this.getContentResolver(), Settings.Secure.ANDROID_ID);
            final String description = Build.BRAND + "(" + Build.MODEL + ")";

            RegisterModel registerModel = new RegisterModel(smsCode, userToken, password, description);
            RegisterInterface client = RetrofitAPIClient.getClient().create(RegisterInterface.class);
            retrofit2.Call<RegisterModel> call = client.register(registerModel);
            call.enqueue(new retrofit2.Callback<RegisterModel>() {
                @Override
                public void onResponse(retrofit2.Call<RegisterModel> call, retrofit2.Response<RegisterModel> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "getAuth", "JAY");

                        Toast.makeText(PhoneVerification.this, R.string.invalidOTP,
                                Toast.LENGTH_LONG).show();

                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        return;
                    }

                    String user_id = response.body().getUser_id();
                    String device_id = response.body().getDevice_id();
                    String xmpp_password = response.body().getXmpp_password();

                    //save details into preferences
                    preferences.save(PhoneVerification.this, GlobalVariables.STRPREF_USER_ID, user_id);
                    preferences.save(PhoneVerification.this, GlobalVariables.STRPREF_DEVICE_ID, device_id);
                    preferences.save(PhoneVerification.this, GlobalVariables.STRPREF_XMPP_PASSWORD, xmpp_password);

                    AuthenticateModel authenticateModel = new AuthenticateModel(GlobalVariables.PASS,
                            user_id, device_id, password, "2", GlobalVariables.STRPREF_PATH);
                    AuthenticateInterface client = RetrofitAPIClient.getClient().create(AuthenticateInterface.class);
                    retrofit2.Call<AuthenticateModel> call1 = client.authenticate(authenticateModel);
                    call1.enqueue(new retrofit2.Callback<AuthenticateModel>() {
                        @Override
                        public void onResponse(retrofit2.Call<AuthenticateModel> call, retrofit2.Response<AuthenticateModel> response) {
                            if (!response.isSuccessful()) {
                                new MiscHelper().retroLogUnsuc(response, "sendAuth", "JAY");

                                Toast.makeText(PhoneVerification.this, R.string.onresponse_unsuccessful,
                                        Toast.LENGTH_SHORT).show();
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                return;
                            }

                            String access_token = response.body().getAccess_token();

                            preferences.save(PhoneVerification.this, GlobalVariables
                                    .STRPREF_ACCESS_TOKEN, access_token);

                            Intent IntentMain = new Intent(PhoneVerification.this, UserProfile.class);

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            startActivity(IntentMain);

                            //finish ALL previous activities below this one
                            finishAffinity();
                        }

                        @Override
                        public void onFailure(retrofit2.Call<AuthenticateModel> call, Throwable t) {
                            new MiscHelper().retroLogFailure(t, "sendAuth", "JAY");
                            Toast.makeText(PhoneVerification.this, R.string.onfailure,
                                    Toast.LENGTH_SHORT).show();

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                        }
                    });
                }

                @Override
                public void onFailure(retrofit2.Call<RegisterModel> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "getAuth", "JAY");

                    Toast.makeText(PhoneVerification.this, R.string.onfailure,
                            Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        unregisterReceiver(smsReceiver);

        super.onDestroy();
    }
}
