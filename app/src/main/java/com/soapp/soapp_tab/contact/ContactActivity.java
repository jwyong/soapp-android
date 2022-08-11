package com.soapp.soapp_tab.contact;

//import android.app.FragmentManager;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.PermissionHelper;
import com.soapp.global.UIHelper;
//import com.soapp.setup.BasicPermissions;

/* Created by Ryan Soapp on 11/29/2017. */

public class ContactActivity extends BaseActivity {

    private TextView title;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new UIHelper().setStatusBarColor(this, false , R.color.primaryDark4);

        if (PackageManager.PERMISSION_GRANTED == this.checkCallingOrSelfPermission(android.Manifest.permission.READ_CONTACTS)) {
            setContentView(R.layout.soapp_contact_activity);

            //status bar color
            new UIHelper().setStatusBarColor(this, false , R.color.grey8);

            setupToolbar();
            setTitle("");

            title = findViewById(R.id.soapp_contact_title);

            if (getIntent().hasExtra("fromChat")) { //came from chat
                title.setText(R.string.share_contact);
            } else { //came from contact (home tab)
                title.setText(R.string.contacts);
            }
        } else {
            new PermissionHelper().CheckPermissions(this, 1007, R.string.permission_txt);

        }

    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 100:
//                if (resultCode == 1) {
//                    setContentView(R.layout.soapp_contact_activity);
//                    setupToolbar();
//
//                    if (getIntent().hasExtra("fromChat")) { //came from chat
//                        setTitle(R.string.share_contact);
//                    } else { //came from contact (home tab)
//                        setTitle(R.string.contacts);
//                    }
//                }
//                break;
//
//            default:
//                break;
//        }
//    }
}
