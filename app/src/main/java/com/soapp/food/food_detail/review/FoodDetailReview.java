package com.soapp.food.food_detail.review;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.soapp.R;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.Preferences;
import com.soapp.sql.DatabaseHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class FoodDetailReview extends Fragment implements View.OnClickListener {

    TextView tv_review;

    //card view
    CardView cv_write_review;
    //res id and name
    String resId, resName;
    //
    private Preferences preferences = Preferences.getInstance();
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.food_details_under_review_item1, container, false);

        FoodDetailLog foodDetailLog = (FoodDetailLog) getActivity();
        resId = foodDetailLog.getResId();
        resName = foodDetailLog.getResName();

        //text view
        tv_review = view.findViewById(R.id.tv_review);
        tv_review.setOnClickListener(this);

        //card view
        cv_write_review = view.findViewById(R.id.cv_write_review);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_review:
                LayoutInflater dialog1layout = LayoutInflater.from(getContext());
                View dialogView = dialog1layout.inflate(R.layout.food_write_review_dialog, null);
                dialogView.setBackground(null);
                dialogView.setBackgroundResource(android.R.color.transparent);
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
                RatingBar rb_ratings = dialogView.findViewById(R.id.rb_ratings);
                TextView tv_average = dialogView.findViewById(R.id.tv_average);
                rb_ratings.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float v = rb_ratings.getRating();
                        if (v == 5) {
                            tv_average.setText("Excellent");
                        } else if (v == 4.5) {
                            tv_average.setText("Almost Excellent");
                        } else if (v == 4) {
                            tv_average.setText("Great");
                        } else if (v == 3.5) {
                            tv_average.setText("Nice");
                        } else if (v == 3) {
                            tv_average.setText("Good");
                        } else if (v == 2.5) {
                            tv_average.setText("Average");
                        } else if (v == 2) {
                            tv_average.setText("Below Average");
                        } else if (v == 1.5) {
                            tv_average.setText("Dislike it");
                        } else if (v == 1) {
                            tv_average.setText("Not coming again");
                        } else if (v == 0.5) {
                            tv_average.setText("Hate it");
                        } else if (v == 0) {
                            tv_average.setText("R.I.P");
                        }
                        return rb_ratings.onTouchEvent(motionEvent);
                    }
                });
                rb_ratings.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        if (v == 5) {
                            tv_average.setText("Excellent");
                        } else if (v == 4.5) {
                            tv_average.setText("Almost Excellent");
                        } else if (v == 4) {
                            tv_average.setText("Great");
                        } else if (v == 3.5) {
                            tv_average.setText("Nice");
                        } else if (v == 3) {
                            tv_average.setText("Good");
                        } else if (v == 2.5) {
                            tv_average.setText("Average");
                        } else if (v == 2) {
                            tv_average.setText("Below Average");
                        } else if (v == 1.5) {
                            tv_average.setText("Dislike it");
                        } else if (v == 1) {
                            tv_average.setText("Not coming again");
                        } else if (v == 0.5) {
                            tv_average.setText("Hate it");
                        } else if (v == 0) {
                            tv_average.setText("R.I.P");
                        }
                    }

                });
                dialog.setContentView(dialogView);
                dialog.show();
                break;
        }
    }

//    private void postResRating(final String resID, final int rating1, final int rating2) {
//        String access_token = preferences.getValue(getContext(), GlobalVariables.STRPREF_ACCESS_TOKEN);
//        RestaurantRatingModel restaurantRatingModel = new RestaurantRatingModel(resID, rating1, rating2);
//
//        //build retrofit
//        RestaurantRating client = RetrofitAPIClient.getClient().create(RestaurantRating.class);
//        retrofit2.Call<RestaurantRatingModel> call = client.resRating((restaurantRatingModel), "Bearer " + access_token);
//        call.enqueue(new retrofit2.Callback<RestaurantRatingModel>() {
//
//            @Override
//            public void onResponse(Call<RestaurantRatingModel> call, Response<RestaurantRatingModel> response) {
//                if (!response.isSuccessful()) {
//                    Toast.makeText(getContext(), R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                databaseHelper.updateResRating(resID, rating1, rating2);
//
//                String thankYou = getString(R.string.review_thank_you) + " " + resName;
//                Toast.makeText(getContext(), thankYou, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<RestaurantRatingModel> call1, Throwable t) {
//                Toast.makeText(getContext(), R.string.onfailure, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
