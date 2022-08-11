package com.soapp.global.MediaGallery.Adapters;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

//import com.erikagtierrez.multiple_media_picker.R;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {
    private List<String> bitmapList;
    private List<Boolean> selected;
    private Context context;
    private DisplayMetrics dm = new DisplayMetrics();

    public MainAdapter(List<String> bitmapList, List<Boolean> selected, Context context) {
        this.bitmapList = bitmapList;
        this.context = context;
        this.selected = selected;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_media_item, parent, false);

        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;

        itemView.getLayoutParams().width = width / 3;
        itemView.getLayoutParams().height = width / 3;
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GlideApp.with(context)
                .asBitmap()
                .load("file://" + bitmapList.get(position))
                .override(200, 200)
                .centerCrop()
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(holder.thumbnail);
        if (selected.get(position).equals(true)) {
            holder.check.setVisibility(View.VISIBLE);
            holder.check.setImageAlpha(150);
        } else {
            holder.check.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return bitmapList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail, check;

        public MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.image);
            check = view.findViewById(R.id.image2);
        }
    }
}

