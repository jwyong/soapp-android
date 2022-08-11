package com.soapp.soapp_tab.reward.my_reward_list.MyRewardDetails;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.global.UIHelper;

import net.glxn.qrgen.android.QRCode;

public class MyRewardDetailsActivity extends BaseActivity {

    ImageView imgv_qr_voucher_details;

    TextView reward_title, reward_des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_voucher_details);
        setupToolbar();

        //status bar color
        new UIHelper().setStatusBarColor(this, false , R.color.grey6);

        if (getIntent().hasExtra("redemption_id")) {
            initWidget();
            String redemption_id = getIntent().getStringExtra("redemption_id");
            String title = getIntent().getStringExtra("title");
            String description = getIntent().getStringExtra("description");
            setupQrcode(redemption_id);
            setupTextView(title, description);
        } else {
            Toast.makeText(MyRewardDetailsActivity.this, "The voucher is no longer valid", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void initWidget() {
        imgv_qr_voucher_details = findViewById(R.id.imgv_qr_voucher_details);
        reward_title = findViewById(R.id.reward_title);
        reward_des = findViewById(R.id.reward_des);
    }

    public void setupQrcode(String redemption_id) {

        Bitmap myBitmap = QRCode.from(redemption_id).withSize(200, 200).bitmap();

        imgv_qr_voucher_details.setImageBitmap(myBitmap);
    }

    public void setupTextView(String title, String description) {
        reward_title.setText(title);
        reward_des.setText(description);
    }
}
