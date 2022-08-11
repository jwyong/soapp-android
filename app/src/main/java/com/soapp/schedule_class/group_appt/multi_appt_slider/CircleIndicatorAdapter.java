package com.soapp.schedule_class.group_appt.multi_appt_slider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.soapp.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by rlwt on 5/28/18.
 */

public class CircleIndicatorAdapter extends RecyclerView.Adapter<CircleIndicatorAdapter.CircleIndicatorHolder> {

    int size = 0;

    public CircleIndicatorAdapter(Integer size) {
        this.size = size;
    }

    @Override
    public CircleIndicatorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View circleListLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_indicator_sche, null);
        CircleIndicatorHolder circleIndicatorHolder = new CircleIndicatorHolder(circleListLayout);
        return circleIndicatorHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CircleIndicatorHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return size;
    }

    public class CircleIndicatorHolder extends RecyclerView.ViewHolder {

        ImageView dot;

        public CircleIndicatorHolder(View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.circle_indicator_item);
        }
    }

}
