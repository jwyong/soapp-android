package com.soapp.chat_class.share_contact;

//import android.app.FragmentManager;

import android.app.Activity;
import android.os.Bundle;

import com.soapp.R;
import com.soapp.base.BaseActivity;
//import com.soapp.setup.BasicPermissions;

/* Created by Ryan Soapp on 11/29/2017. */

public class ShareContactActivity extends BaseActivity {
    public static String jid;
    public static Activity activity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_share_contact_activity);
        setupToolbar();
        setTitle(R.string.share_contact);

        activity = this;

        jid = getIntent().getStringExtra("jid");
        //check for basic permissions
//        Intent intent = new Intent(this, BasicPermissions.class);
//        intent.putExtra("notLaunch", "1");

//        startActivityForResult(intent, 100);
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 100:
//                if (resultCode == 1) {
//                    setContentView(R.layout.chat_share_contact_activity);
//                    setupToolbar();
//                    setTitle(R.string.share_contact);
//
//                    activity = this;
//
//                    jid = getIntent().getStringExtra("jid");
//                }
//                break;
//
//            default:
//                break;
//        }
//    }
}
