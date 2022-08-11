package com.soapp.settings_tab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.BuildConfig;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.UIHelper;

/* Created by Soapp on 29/06/2017. */


public class AboutUs extends BaseActivity {

    private UIHelper uiHelper = new UIHelper();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingstab_about);
        setupToolbar();

        //
        uiHelper.setStatusBarColor(this ,  false , R.color.black3a);

        ImageView fb = findViewById(R.id.btn_fb);
        ImageView tw = findViewById(R.id.btn_tw);
        ImageView web = findViewById(R.id.btn_web);
        ImageView insta = findViewById(R.id.btn_insta);


        String versionName = BuildConfig.VERSION_NAME;
        TextView aboutText = findViewById(R.id.text_view_version_name);
        String versionStr = getString(R.string.welcome1) + " " + versionName;
        aboutText.setText(versionStr);
        setupToolbar();

        TextView terms = findViewById(R.id.terms);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.soappchat" +
                        ".com/terms-of-use.html")));

            }
        });

        fb.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Soappchat/")));

            }
        });

        tw.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/SoappChat")));

            }
        });

        web.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.soappchat.com/")));

            }
        });

        insta.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/soappchat/")));

            }
        });
    }

}
