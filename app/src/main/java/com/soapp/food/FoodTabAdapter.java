package com.soapp.food;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.soapp.R;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.UIHelper;
import com.soapp.global.sharing.SharingController;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by jason on 24/04/2018.
 */


public class FoodTabAdapter extends RecyclerView.Adapter<FoodTabAdapter.ViewHolder> {

    String item1[] = {"MY-Res-40082", "MY-Res-33269", "MY-Res-31648", "MY-Res-40078"};
    String item2[] = {"Donkey and Crow Irish Pub", "Sushi Tsen", "Softsrve", "Ziffy"};
    String item3[] = {"https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respics/MY40082/MY40082i1.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respics/MY33269/MY33269i1.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respics/MY31648/MY31648i1.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respics/MY40078/MY40078i1.jpg?v="};
    String item4[] = {"Bangsar", "Damansara Jaya", "Damansara Utama", "Kota Damansara (KD)"};
    String item5[] = {"Kuala Lumpur (KL)", "Selangor", "Selangor", "Selangor"};
    String item6[] = {"3.143167", "3.127175", "3.1359500885", "3.150791"};
    String item7[] = {"101.666816", "101.616583", "101.6210021973", "101.595198"};
    String item8[] = {"5.0", "5.0", "5.0", "5.0"};
    String propic[] = {"https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respicsThumb/MY40082/MY40082.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respicsThumb/MY33269/MY33269.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respicsThumb/MY31648/MY31648.jpg?v=",
            "https://s3-ap-southeast-1.amazonaws.com/soappchat/MY/respicsThumb/MY40078/MY40078.jpg?v="};
    //    private List<RestaurantModel> list;
    private Context context;
    private Activity activity;
    private DisplayMetrics dm = new DisplayMetrics();

    public FoodTabAdapter(Context context) {
        setHasStableIds(true);
//        this.list = list;
        this.context = context;
        activity = (Activity) context;

    }

    @Override
    public FoodTabAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FoodTabAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food_tab_fragment_item, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return item1.length;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodTabAdapter.ViewHolder holder, int position) {

        String resID = item1[position];
        String ownerJid = null;
        String resTitle = item2[position];
        String resDetImg1 = item3[position];
        String resLoc = item4[position];
        String resState = item5[position];
        String resVid = null;
        String resPropic = propic[position];

        String resLat = item6[position];
        String resLon = item7[position];

        String resRating = item8[position];

        GlideApp.with(context)
                .load(resDetImg1)
                .centerCrop()
                .dontTransform()
//                .transition(withCrossFade())
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_res_def_loading_250px)
                .into(holder.PromoImg);

        holder.promo_Title.setText(resTitle);
        holder.promo_Loc.setText(resLoc + " , " + resState);

        //set width of imagecard
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        double width = dm.widthPixels * 0.65;

        holder.cardPromo.getLayoutParams().width = (int) width;

        float floatValue = Float.parseFloat(resRating);
        holder.promo_rating.setText(String.format("%.1f", floatValue));

        //go into restaurant details intent
        holder.cardPromo.setOnClickListener(new View.OnClickListener() {
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
        holder.promo_appt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(context, SharingController.class);

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
        holder.promo_Loc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] point = new int[2];
                view.getLocationOnScreen(point);
                new UIHelper().showNavigationPopup(context, resLat, resLon, point[0], point[1]);
            }
        });

        //send restaurant coords, map URL, title etc to chat
        holder.promo_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(context, SharingController.class);
                shareIntent.putExtra("infoid", resID);
                shareIntent.putExtra("restitle", resTitle);
                shareIntent.putExtra("image_id", resPropic);
                shareIntent.putExtra("from", "foodChat");

                activity.startActivity(shareIntent);
            }
        });

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView PromoImg, promo_share, promo_Loc_btn, promo_appt_btn;
        TextView promo_Title, promo_Loc, promo_rating;
        CardView cardPromo;

        public ViewHolder(View itemView) {
            super(itemView);
            PromoImg = itemView.findViewById(R.id.PromoImg);
            promo_Title = itemView.findViewById(R.id.promo_Title);
            promo_Loc = itemView.findViewById(R.id.promo_Loc);
            cardPromo = itemView.findViewById(R.id.cardPromo);
            promo_share = itemView.findViewById(R.id.promo_share);
            promo_Loc_btn = itemView.findViewById(R.id.promo_Loc_btn);
            promo_appt_btn = itemView.findViewById(R.id.promo_appt_btn);
            promo_rating = itemView.findViewById(R.id.promo_rating);

        }

//
//        public void setData() {
//            PromoImg.setImageResource(R.drawable.img_promo);
//        }
    }
}





