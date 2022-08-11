package com.soapp.soapp_tab.favourite;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.UIHelper;
import com.soapp.global.sharing.SharingController;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;
import com.soapp.sql.room.entity.Restaurant;

import androidx.recyclerview.widget.RecyclerView;

/* Created by Ryan Soapp on 12/4/2017. */

public class FavResHolder extends RecyclerView.ViewHolder {
    //basics
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private ImageView favres_profile_img, favres_fav_btn, favres_appt_btn, favres_loc_btn, favres_share_btn,
            favres_cam_btn;
    private TextView favres_name, favres_cuisine, favres_loc_state, favres_rating;
    private String resPropic, resLoc, resState, resID, resTitle, resLat, resLon;
    private int fav, softBtnHeight;

    FavResHolder(View itemView) {
        super(itemView);
        favres_profile_img = itemView.findViewById(R.id.favres_profile_img);
        favres_fav_btn = itemView.findViewById(R.id.favres_fav_btn);
        favres_cam_btn = itemView.findViewById(R.id.favres_cam_btn);
        favres_appt_btn = itemView.findViewById(R.id.favres_appt_btn);
        favres_loc_btn = itemView.findViewById(R.id.favres_loc_btn);
        favres_share_btn = itemView.findViewById(R.id.favres_share_btn);
        favres_rating = itemView.findViewById(R.id.favres_rating);
        favres_name = itemView.findViewById(R.id.favres_name);
//        favres_cuisine = (TextView) itemView.findViewById(R.id.favres_cuisine);
        favres_loc_state = itemView.findViewById(R.id.favres_loc_state);
    }

    public void setData(Cursor cursor) {
        resPropic = cursor.getString(cursor.getColumnIndex("restaurantimage"));
        if (resPropic == null || resPropic.equals("")) {
            favres_cam_btn.setVisibility(View.GONE);
            favres_cam_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open camera to take res propic
                }
            });
        } else {
            favres_cam_btn.setVisibility(View.GONE);

            GlideApp.with(Soapp.getInstance().getApplicationContext())
                    .load(resPropic)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .into(favres_profile_img);
        }
        favres_name.setText(cursor.getString(cursor.getColumnIndex("restauranttitle")));

        resLoc = cursor.getString(cursor.getColumnIndex("restaurantlocation"));
//        resState = cursor.getString(cursor.getColumnIndex("restauranttitle"));
        if (resState != null && !resState.equals("null")) {
            resLoc = resState + ", " + resLoc;
        }

        //fav
        fav = cursor.getInt(cursor.getColumnIndex("fav"));
        if (fav == 1) {
            favres_fav_btn.setImageResource(R.drawable.ic_fav_red_200px);
        }

        favres_fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();

                if (fav == 1) {
                    cv.put("fav", 0);
                } else {
                    cv.put("fav", 1);
                }

                databaseHelper.updateRDB1Col(DatabaseHelper.RES_TABLE_NAME, cv, DatabaseHelper.RES_ID, resID);
            }
        });

        //go into restaurant details intent
        resID = cursor.getString(cursor.getColumnIndex("restaurantid"));
        resTitle = cursor.getString(cursor.getColumnIndex("restauranttitle"));

        favres_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(favres_profile_img.getContext(),
                        FoodDetailLog.class);

                intent.putExtra("resID", resID);
//                intent.putExtra("ownerJid", ownerJid);
                intent.putExtra("resName", resTitle);
                intent.putExtra("resPropic", resPropic);
                intent.putExtra("resLoc", resLoc);
                intent.putExtra("resState", resState);
//                intent.putExtra("resVid", resVid);
                intent.putExtra("resLat", resLat);
                intent.putExtra("resLon", resLon);

                favres_profile_img.getContext().startActivity(intent);
            }
        });
        //send restaurant coords, map URL, title etc to APPT
        favres_appt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent(favres_appt_btn.getContext(), SharingController.class);

                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);

                shareIntent.putExtra("from", "foodAppt");

                favres_appt_btn.getContext().startActivity(shareIntent);
            }
        });

        //go to map activity with restaurant coords
        resLat = cursor.getString(cursor.getColumnIndex("restaurantlat"));
        resLon = cursor.getString(cursor.getColumnIndex("restaurantlon"));

        favres_loc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] point = new int[2];
                view.getLocationOnScreen(point);
//
                new UIHelper().showNavigationPopup(itemView.getContext(), resLat, resLon,
                        point[0], point[1]);
            }
        });

        //send restaurant coords, map URL, title etc to chat
        favres_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(favres_share_btn.getContext(), SharingController.class);

                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("from", "foodChat");

                favres_share_btn.getContext().startActivity(shareIntent);
            }
        });
    }

    public void setData(Restaurant item) {
        resPropic = item.getResImageUrl();
        if (resPropic == null || resPropic.equals("")) {
            favres_cam_btn.setVisibility(View.GONE);
            favres_cam_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open camera to take res propic
                }
            });
        } else {
            favres_cam_btn.setVisibility(View.GONE);

            GlideApp.with(Soapp.getInstance().getApplicationContext())
                    .load(resPropic)
                    .placeholder(R.drawable.ic_res_default_no_image_black_640px)
                    .into(favres_profile_img);
        }
        favres_name.setText(item.getResTitle());

        resLoc = item.getResLocation();
//        resState = cursor.getString(cursor.getColumnIndex("restauranttitle"));
        if (resState != null && !resState.equals("null")) {
            resLoc = resState + ", " + resLoc;
        }
        favres_loc_state.setText(resLoc);

        //fav
        fav = item.getResFavourited();
        if (fav == 1) {
            favres_fav_btn.setImageResource(R.drawable.ic_fav_red_200px);
        } else {
            favres_fav_btn.setImageResource(R.drawable.ic_fav_white_70px);
        }
//todo not updating

        favres_fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();

                if (fav == 1) {
                    cv.put("ResFavourited", 0);
                } else {
                    cv.put("ResFavourited", 1);
                }

                databaseHelper.updateRDB1Col(DatabaseHelper.RES_TABLE_NAME, cv, DatabaseHelper.RES_ID, resID);
            }
        });

        //go into restaurant details intent
        resID = item.getResID();
        resTitle = item.getResTitle();

        favres_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(favres_profile_img.getContext(), FoodDetailLog.class);

                intent.putExtra("resID", resID);
//                intent.putExtra("ownerJid", ownerJid);
                intent.putExtra("resName", resTitle);
                intent.putExtra("resPropic", resPropic);
                intent.putExtra("resLoc", resLoc);
                intent.putExtra("resState", resState);
//                intent.putExtra("resVid", resVid);
                intent.putExtra("resLat", resLat);
                intent.putExtra("resLon", resLon);

                favres_profile_img.getContext().startActivity(intent);
            }
        });
        //send restaurant coords, map URL, title etc to APPT
        favres_appt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent(favres_appt_btn.getContext(), SharingController.class);

                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);

                shareIntent.putExtra("from", "foodAppt");

                favres_appt_btn.getContext().startActivity(shareIntent);
            }
        });

        //go to map activity with restaurant coords
        resLat = item.getResLatitude();
        resLon = item.getResLongitude();

        favres_loc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] point = new int[2];
                view.getLocationOnScreen(point);
                new UIHelper().showNavigationPopup(itemView.getContext(), resLat, resLon,
                        point[0], point[1]);
            }
        });

        //send restaurant coords, map URL, title etc to chat
        favres_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(favres_share_btn.getContext(), SharingController.class);

                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("from", "foodChat");

                favres_share_btn.getContext().startActivity(shareIntent);
            }
        });

    }
}
