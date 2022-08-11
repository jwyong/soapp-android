package com.soapp.food.food_detail.promotion;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.soapp.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FoodDetailPromo extends Fragment implements View.OnClickListener {

    //Button
    Button btn_promo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_details_rest_promotion_item, container, false);
        btn_promo = view.findViewById(R.id.btn_promo);
        btn_promo.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_promo:
                BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
                        FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
                        if (bottomSheet == null)
                            return;
                        bottomSheet.setBackground(null);
                    }
                });
                dialog.setContentView(R.layout.food_promo_info_qr_code);
                dialog.show();
                break;
        }
    }
}
