package com.soapp.global.MediaGallery.Adapters;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

//import com.soapp.global.MediaGallery.Adapters.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {
    private List<String> bucketNames, bitmapList;
    private Context context;
    private DisplayMetrics dm = new DisplayMetrics();

    public GalleryAdapter(List<String> bucketNames, List<String> bitmapList, Context context) {
        this.bucketNames = bucketNames;
        this.bitmapList = bitmapList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_album_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        bucketNames.get(position);
        holder.title.setText(bucketNames.get(position));

        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;

        holder.thumbnail.getLayoutParams().width = width / 3;
        holder.thumbnail.getLayoutParams().height = width / 3;

        GlideApp.with(context)
                .asBitmap()
                .load("file://" + bitmapList.get(position))
                .centerCrop()
                .override(200, 200)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return bucketNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.title);
            thumbnail = view.findViewById(R.id.image);
        }
    }
}

