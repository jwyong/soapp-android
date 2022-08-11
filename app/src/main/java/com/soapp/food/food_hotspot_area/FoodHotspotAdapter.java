package com.soapp.food.food_hotspot_area;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.SoappModel.RestaurantModel;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.CameraHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.UIHelper;
import com.soapp.global.sharing.SharingController;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

/* Created by Soapp on 19/09/2017. */

public class FoodHotspotAdapter extends RecyclerView.Adapter<FoodHotspotHolder> {


    private List<RestaurantModel> list;
    private Context context;
    private Activity activity;
    private DatabaseHelper databaseHelper = DatabaseHelper.getInstance();


    public FoodHotspotAdapter(Context context, List<RestaurantModel> list) {
        setHasStableIds(true);
        this.list = list;
        this.context = context;
        activity = (Activity) context;

    }

    @Override
    public FoodHotspotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FoodHotspotHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false));
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getID().hashCode();

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final FoodHotspotHolder holder, int position) {
        final RestaurantModel item = list.get(position);

        //get all relevant info from indiScheList and lock to final variable
        final String resPropic = item.getPropic();
        final String resID = item.getID();
        final String ownerJid = item.getOwner_jid();
        final String resLat = item.getLatitude();
        final String resLon = item.getLongitude();
        final String resLoc = item.getLocation();
        final String resState = item.getState();
        final String resTitle = item.getName();
        final String resVid = item.getVideo();
        final String mainCuisine = item.getMainCuisine();

        final String overallRating = item.getRating();

        if (resPropic == null || resPropic.equals("2")) {
            //API not ready for now (set to gone)
            holder.res_cam_btn.setVisibility(View.GONE);

            holder.res_cam_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open camera to take res propic
                    int res_id = Integer.valueOf(resID.substring(resID.lastIndexOf('-') + 1));

                    Intent takePropicIntent = new CameraHelper().startCameraIntent(activity);

                    takePropicIntent.putExtra("resID", res_id);
                    takePropicIntent.putExtra("from", "foodNearbyTabItem");

                    activity.startActivity(takePropicIntent);
                }
            });
        } else {
            holder.res_cam_btn.setVisibility(View.GONE);
        }

        GlideApp.with(context)
                .asBitmap()
                .load(resPropic)
                .placeholder(R.drawable.ic_res_def_loading_640px)
                .error(R.drawable.ic_res_default_no_image_black_640px)
                .transition(BitmapTransitionOptions.withCrossFade())
                .thumbnail(0.1f)
                .into(holder.res_profile_img);

        holder.res_name.setText(resTitle);
//        holder.res_loc_state.setText(resLoc + ", " + resState);
        float floatValue = Float.parseFloat(item.getRating());
        String NumStar = String.format(Locale.ENGLISH, "%.1f", floatValue);
        holder.star_num.setText(NumStar);
        holder.res_cuisine.setText(item.getMainCuisine());

        //fav | ll_testje
        if (item.getFav() > 0) {
            holder.res_fav_btn.setImageResource(R.drawable.ic_fav_red_200px);
        } else {
            holder.res_fav_btn.setImageResource(R.drawable.ic_fav_white_70px);
        }

        holder.res_fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues cv = new ContentValues();
                cv.put("ResID", resID);
                cv.put("ResTitle", resTitle);
                cv.put("ResLocation", resLoc);
                cv.put("ResState", resState);
                cv.put("ResMainCuisine", mainCuisine);
                cv.put("ResLatitude", resLat);
                cv.put("ResLongitude", resLon);
//                    cv.put("resjid", ownerJid);
                cv.put("ResImageUrl", resPropic);
                cv.put("ResOverallRating", overallRating);

                //was 1 then become 0, so set empty love
                if (item.getFav() > 0) {
                    cv.put("ResFavourited", 0);
                    item.setFav(0);
                    holder.res_fav_btn.setImageResource(R.drawable.ic_fav_white_70px);
                } else {
                    //was 0 den become 1, so set full love
                    cv.put("ResFavourited", 1);
                    item.setFav(1);
                    holder.res_fav_btn.setImageResource(R.drawable.ic_fav_red_200px);
                }

                if (databaseHelper.getRestaurantExist(resID) == 1) { //exists
                    databaseHelper.updateRDB1Col(DatabaseHelper.RES_TABLE_NAME, cv, DatabaseHelper.RES_ID, resID);
                } else {
                    databaseHelper.insertCVToFav(cv);
                }
            }
        });

        //go into restaurant details intent
        holder.res_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FoodDetailLog.class);

                intent.putExtra("resID", resID);
                intent.putExtra("ownerJid", ownerJid);
                intent.putExtra("resName", resTitle);
                intent.putExtra("resPropic", resPropic);
                intent.putExtra("resLoc", resLoc);
                intent.putExtra("resState", resState);
                intent.putExtra("resVid", resVid);
                intent.putExtra("resLat", resLat);
                intent.putExtra("resLon", resLon);

                activity.startActivity(intent);
            }
        });

        //send restaurant coords, map URL, title etc to APPT
        holder.res_appt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Soapp.getInstance().getApplicationContext(), SharingController.class);
                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("resLat", resLat);
                shareIntent.putExtra("resLon", resLon);
                shareIntent.putExtra("from", "foodAppt");

                activity.startActivity(shareIntent);
            }
        });

        //go to map activity with restaurant coords
        holder.res_loc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] point = new int[2];
                view.getLocationOnScreen(point);
                float density = context.getResources().getDisplayMetrics().density;
                float margin = 130 * density;
                new UIHelper().showNavigationPopup(activity, resLat, resLon,
                        point[0] - (int) margin, point[1]);
            }
        });

        //send restaurant coords, map URL, title etc to chat
        holder.res_share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Soapp.getInstance().getApplicationContext(), SharingController.class);
                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("from", "foodChat");

                activity.startActivity(shareIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

}
