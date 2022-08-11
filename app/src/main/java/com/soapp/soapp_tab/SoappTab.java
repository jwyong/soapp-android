package com.soapp.soapp_tab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.soapp.R;
import com.soapp.global.PermissionHelper;
import com.soapp.global.Preferences;
import com.soapp.global.UIHelper;
import com.soapp.settings_tab.Profile;
import com.soapp.settings_tab.QrReader;
import com.soapp.soapp_tab.contact.ContactActivity;
import com.soapp.soapp_tab.favourite.FavResController;
import com.soapp.soapp_tab.reward.reward_personal_info.RewardPersonalInfoActivity;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

//import com.soapp.global.AddContactHelper;

/* Created by Ryan Soapp on 11/29/2017. */

public class SoappTab extends Fragment {
    private Preferences preferences = Preferences.getInstance();
    private View rootView;
    RelativeLayout contactImgView, favImgView, bookingImgView;
    LinearLayout imgViewRow;
    ImageView soapp_contact, soapp_favourite, soapp_booking, soapp_profile, soapp_schedule, soapp_qrscan, btnTest;

//    temporary use this circle
    ImageView settingBubble;

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.soapp_tab_home, container, false);

            // setting the statusbar
            ImageView statusBar = rootView.findViewById(R.id.statusBar);
            ConstraintLayout.LayoutParams statusParams =
                    new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            new UIHelper().getStatusBarHeight(getActivity()));
            statusBar.setLayoutParams(statusParams);
//            statusBar.setBackgroundColor(getResources().getColor(R.color.grey8));
            //** end setting statusbar **//

            soapp_qrscan = rootView.findViewById(R.id.qrBubble);
            soapp_booking = rootView.findViewById(R.id.bookingBubble);
            soapp_contact = rootView.findViewById(R.id.contactBubble);
            soapp_profile = rootView.findViewById(R.id.profileBubble);
            soapp_favourite = rootView.findViewById(R.id.favBubble);

            //temp - booking = rewards bubble
            setupRewardCircle(rootView);

//            soapp_booking.setOnClickListener(view -> {
//                Intent ResBookAct = new Intent(getActivity(), ResBookingsController.class);
//                startActivity(ResBookAct);
//            });

            soapp_contact.setOnClickListener(view -> {
                if (PackageManager.PERMISSION_GRANTED == getContext().checkCallingOrSelfPermission(android.Manifest.permission.READ_CONTACTS)) {
                    Intent contactAct = new Intent(getActivity(), ContactActivity.class);
                    startActivity(contactAct);
                } else {
                    new PermissionHelper().CheckPermissions(getContext(), 1007, R.string.permission_txt);
                    preferences.save(getContext(), "contactPermission", "homeContact");

                }

            });

            soapp_profile.setOnClickListener(view -> {
                Intent ProfileAct = new Intent(getActivity(), Profile.class);
                startActivity(ProfileAct);
            });

            soapp_favourite.setOnClickListener(view -> {
                Intent FavAct = new Intent(getActivity(), FavResController.class);
                startActivity(FavAct);
            });

            soapp_qrscan.setOnClickListener(v -> {
                Intent QrScan = new Intent(getActivity(), QrReader.class);
                startActivity(QrScan);
            });

        }
        return rootView;
    }

    public void setupRewardCircle(View rootView){
//        settingBubble = rootView.findViewById(R.id.settingBubble);
        soapp_booking.setClickable(true);
        soapp_booking.setEnabled(true);
        soapp_booking.setOnClickListener(view -> {
            Intent rewardIntent = new Intent(getContext(), RewardPersonalInfoActivity.class);
            getContext().startActivity(rewardIntent);
        });
    }
}



