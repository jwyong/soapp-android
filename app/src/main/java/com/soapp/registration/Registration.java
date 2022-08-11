package com.soapp.registration;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.SoappApi.ApiModel.RegisterUser;
import com.soapp.SoappApi.Interface.RegisterPn;
import com.soapp.SoappApi.RetrofitAPIClient;
import com.soapp.global.DirectoryHelper;
import com.soapp.global.GlobalVariables;
import com.soapp.global.MiscHelper;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.registration.country_codes.CountryCodes;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


public class Registration extends Activity implements View.OnClickListener {
    //basics
    private PermissionHelper permissionHelper = new PermissionHelper();
    private Preferences preferences = Preferences.getInstance();

    ProgressDialog dialog;
    private EditText edtxt_phoneNumber, edtxt_countryCode;
    private Button btn_send_confirm_code;
    private TextView tv_reg_terms;
    private CheckBox checkBox_agreed;
    private boolean check_agreed, checkBox_agreed2;
    public String askAgain;
    public boolean fristtime, sectime = false;

    private final TextWatcher phoneNumberWatch = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() > 6 && s.length() < 12) {

                checkBox_agreed2 = true;
                getAgreed_btn(check_agreed, checkBox_agreed2);

            } else {

                checkBox_agreed2 = false;
                getAgreed_btn(check_agreed, checkBox_agreed2);

            }
        }
    };
    private String strCountryCode = "";

    //for sms permissions snack bars reg_registration  [reg_registration_faiza_old]
    private ViewGroup verifyreg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_registration);

        // check permission for >= 6.0
        permissionHelper.CheckPermissions(this, 1000, R.string.permission_txt);

        preferences.save(Registration.this, GlobalVariables.STRPREF_COUNTRY_CODE, "60");

        verifyreg = findViewById(R.id.verifyreg);
        edtxt_countryCode = findViewById(R.id.edtxt_countryCode);
        checkBox_agreed = findViewById(R.id.checkBox_agreed);
        edtxt_phoneNumber = findViewById(R.id.edtxt_phoneNumber);
        btn_send_confirm_code = findViewById(R.id.btn_send_confirm_code);
        tv_reg_terms = findViewById(R.id.tv_reg_terms);

        edtxt_phoneNumber.addTextChangedListener(phoneNumberWatch);

        tv_reg_terms.setOnClickListener(this);
        checkBox_agreed.setOnClickListener(this);
        btn_send_confirm_code.setOnClickListener(this);
        edtxt_countryCode.setOnClickListener(this);
        verifyreg.setOnClickListener(this);
        btn_send_confirm_code.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_confirm_code:
                //ask for sms reading permission first
                if (fristtime) {
                    preferences.save(this, "askAgain", "nil");
                    fristtime = sectime;
                }

                if (ActivityCompat.checkSelfPermission(Registration.this, Manifest
                        .permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    new PermissionHelper().CheckPermissions(this, 1002, R.string.permission_txt);
                } else {
                    requestOTP();
                }
                break;

            case R.id.edtxt_countryCode:
                Intent intentCountrycodes = new Intent(Registration.this, CountryCodes.class);
                startActivityForResult(intentCountrycodes, 1000);

                break;

            case R.id.checkBox_agreed:
                if (checkBox_agreed.isChecked()) {
                    check_agreed = checkBox_agreed.isChecked();
                    getAgreed_btn(check_agreed, checkBox_agreed2);

                } else {

                    check_agreed = checkBox_agreed.isChecked();
                    getAgreed_btn(check_agreed, checkBox_agreed2);

                }
                break;

            case (R.id.tv_reg_terms):
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.soappchat.com/terms-of-use.html")));
                break;


            case (R.id.verifyreg):
                new UIHelper().closeKeyBoard(this);

                break;
            default:
                break;
        }
    }


    private void getAgreed_btn(boolean check_agreed, boolean checkBox_agreed2) {

        if (check_agreed && checkBox_agreed2) {

            btn_send_confirm_code.setEnabled(true);
            btn_send_confirm_code.setBackgroundResource(R.drawable.xml_roundcorner_primarylogo);
        } else {

            btn_send_confirm_code.setEnabled(false);
            btn_send_confirm_code.setBackgroundResource(R.drawable.xml_roundcorner_tint);
        }
    }

    //activity results for choosing country codes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                String[] codefilter1 = data.getStringExtra("result").toString().split("\\(");

                String filter = (codefilter1[1]).substring(0, (codefilter1[1]).length() - 1);
                preferences.save(Registration.this, GlobalVariables.STRPREF_COUNTRY_CODE, filter);
                edtxt_countryCode.setText(filter);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];

                    //if permission is storage, need to go to directory helper if granted
//                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            showRationale = shouldShowRequestPermissionRationale(permission);
                        }
                        if (!showRationale) {
                            preferences.save(this, "askAgain", "false");
                            Toast.makeText(this, "Please Access Permissions", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } else { //allowed, restart app if storage permission haven't reset by itself
                        //create Soapp folders once permission granted
                        if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new DirectoryHelper().checkAndCreateDir(this, true);
                        }

                    }

//                    } else { //contacts permission

//                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                        boolean showRationale = false;
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                            showRationale = shouldShowRequestPermissionRationale(permission);
//                        }
//                        if (!showRationale) {
//                            preferences.save(this, "askAgain", "false");
//                            Toast.makeText(this, "Please Access Permissions", Toast.LENGTH_SHORT).show();
//                            break;
//                        }
//                        }
                }
//        }

                break;

            case 1002:
                //start to post OTP request to server
                requestOTP();
                break;
        }

    }

    private void requestOTP() {
        preferences = Preferences.getInstance();
        strCountryCode = preferences.getValue(Registration.this, GlobalVariables.STRPREF_COUNTRY_CODE);
        if (strCountryCode != null) {
            dialog = ProgressDialog.show(this, null, "Sending confirmation code");

            RegisterUser user = new RegisterUser(strCountryCode, edtxt_phoneNumber.getText().toString().trim(), 0);
            //build retrofit
            RegisterPn client = RetrofitAPIClient.getClient().create(RegisterPn.class);
            retrofit2.Call<RegisterUser> call = client.registerPhoneNumber(user);
            call.enqueue(new retrofit2.Callback<RegisterUser>() {
                @Override
                public void onResponse(retrofit2.Call<RegisterUser> call, retrofit2.Response<RegisterUser> response) {
                    if (!response.isSuccessful()) {
                        new MiscHelper().retroLogUnsuc(response, "getOTP", "JAY");

                        Toast.makeText(Registration.this, R.string.onresponse_unsuccessful,
                                Toast.LENGTH_SHORT).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        return;
                    }

                    final String token = String.valueOf(response.body().getToken());

                    preferences.save(Registration.this, GlobalVariables.STRPREF_PHONE_NUMBER, edtxt_phoneNumber.getText().toString().trim());
                    preferences.save(Registration.this, GlobalVariables.STRPREF_USER_TOKEN, token);

                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    //start phone veri activity
                    Intent IntentMain = new Intent(Registration.this, PhoneVerification.class);
                    startActivity(IntentMain);
                }

                @Override
                public void onFailure(retrofit2.Call<RegisterUser> call, Throwable t) {
                    new MiscHelper().retroLogFailure(t, "getOTP", "JAY");

                    Toast.makeText(Registration.this, R.string.onfailure, Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(Registration.this, "No country code is selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}