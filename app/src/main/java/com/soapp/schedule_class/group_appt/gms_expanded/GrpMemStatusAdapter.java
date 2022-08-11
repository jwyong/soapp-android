package com.soapp.schedule_class.group_appt.gms_expanded;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.soapp.R;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.sql.room.entity.ContactRoster;

import java.util.List;

/**
 * Created by rlwt on 5/31/18.
 */

public class GrpMemStatusAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<ContactRoster> memberList;
    private Context mContext;

    public GrpMemStatusAdapter(Context context, List<ContactRoster> memberList) {
        mInflater = LayoutInflater.from(context);
        this.memberList = memberList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int i) {
        return memberList.get(i);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        GridViewHolder gridViewHolder;

        if (view == null) {
            gridViewHolder = new GridViewHolder();
            view = mInflater.inflate(R.layout.image_top_textview_down, null);

            gridViewHolder.imgv_profile_pic = view.findViewById(R.id.imgv_profile_pic);
            gridViewHolder.tv_display_name = view.findViewById(R.id.tv_display_name);

            view.setTag(gridViewHolder);
        } else {
            gridViewHolder = (GridViewHolder) view.getTag();
        }

        //set profile img
        if (memberList.get(i).getProfilephoto() != null) {
            GlideApp.with(mContext)
                    .load(memberList.get(i).getProfilephoto())
                    .placeholder(R.drawable.in_propic_circle_150px)
                    .apply(RequestOptions.circleCropTransform())
                    .into(gridViewHolder.imgv_profile_pic);
        }

        //displayname
        String memberName = memberList.get(i).getPhonename();

        //phone number - for passing into intent then calling
        String phoneNumber = memberList.get(i).getPhonenumber();

        if (memberName == null) {
            String displayName = memberList.get(i).getDisplayname();
            memberName = displayName + " " + phoneNumber;
        }
        gridViewHolder.tv_display_name.setText(memberName);

        //set onclick
        return view;
    }

    private static class GridViewHolder {
        public ImageView imgv_profile_pic;
        public TextView tv_display_name;
    }

}
