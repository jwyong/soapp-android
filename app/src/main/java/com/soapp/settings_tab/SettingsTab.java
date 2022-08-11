package com.soapp.settings_tab;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.Preferences;
import com.soapp.global.SharingHelper;
import com.soapp.global.UIHelper;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import io.branch.referral.Branch;
import io.github.rockerhieu.emojicon.EmojiconTextView;

//[Jay]

/* Created by Soapp on 29/06/2017. */

public class SettingsTab extends Fragment {

    public static EmojiconTextView text_view_profile_name;
    ImageView image_view_profile;
    Preferences preferences = Preferences.getInstance();
    View rootView;
    String user_displayname;

    CardView editing, settingCard, shareCard, contactCard, aboutCard;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Branch.getInstance().initSession((referringParams, error) -> {});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.setting_tab, container, false);

            // setting the statusbar
            ImageView statusBar = rootView.findViewById(R.id.statusBar);
            LinearLayout.LayoutParams statusParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            new UIHelper().getStatusBarHeight(getActivity()));
            statusBar.setLayoutParams(statusParams);
//            statusBar.setBackgroundColor(getResources().getColor(R.color.grey8));
            //** end setting statusbar **//

            user_displayname = preferences.getValue(Soapp.getInstance().getApplicationContext
                    (), GlobalVariables.STRPREF_USERNAME);
            text_view_profile_name = rootView.findViewById(R.id.text_view_profile_name);
            text_view_profile_name.setText(user_displayname);
            image_view_profile = rootView.findViewById(R.id.image_view_profile);

            //set font for emojitextview
            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Quicksand_Bold.otf");
            text_view_profile_name.setTypeface(typeface);

            //profile
            editing = rootView.findViewById(R.id.cardview_main);
            editing.setOnClickListener(v -> startActivity(new Intent(getActivity(), Profile.class)));

            //settings
            settingCard = rootView.findViewById(R.id.settingCard);
            settingCard.setOnClickListener(arg0 -> {
                Intent intent = new Intent(getContext(), AdditionalSettings.class);
                startActivity(intent);
            });

            //invite friends
            shareCard = rootView.findViewById(R.id.shareCard);
            shareCard.setOnClickListener(view -> {
                new SharingHelper().shareSoappToFriends(getContext(), getActivity().findViewById(R.id.progress_bar_cl), view);
            });

            //contact us
            contactCard = rootView.findViewById(R.id.contactCard);
            contactCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), ContactUs.class)));

            //about us
            aboutCard = rootView.findViewById(R.id.aboutCard);
            aboutCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutUs.class)));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (image_view_profile != null) {
            //glide profile image from db
            String userJid = preferences.getValue(getContext(), GlobalVariables.STRPREF_USER_ID);
            byte[] profileImgByte = DatabaseHelper.getInstance()
                    .getImageBytesThumbFromContactRoster(userJid);

            GlideApp.with(this)
                    .load(profileImgByte)
                    .placeholder(R.drawable.in_propic_circle_150px)
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .encodeQuality(50)
                    .override(180, 180)
                    .into(image_view_profile);
        }
    }
}