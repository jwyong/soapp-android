package com.soapp.global;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.soapp.sql.room.entity.Message;

import java.io.File;
import java.util.List;

public class ImageVideo_Adapter extends BaseAdapter {
    private Context mContext;
    private Activity activity;
    public static List<Message> list;

    public ImageVideo_Adapter(Context c) {
        mContext = c;
        activity = (Activity) c;
    }

    public int getCount() {
//        return 0;
        return list.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        String mediaPath = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);


            int width = (new UIHelper().getScreenWidth(activity) / 4);

            imageView.setLayoutParams(new ViewGroup.LayoutParams(width, width));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }


        switch (list.get(position).getIsSender()) {
            case 20: //outgoing image
                mediaPath = GlobalVariables.IMAGES_SENT_PATH + "/" + list.get(position).getMsgInfoUrl();
                break;

            case 21: //incoming image
                mediaPath = GlobalVariables.IMAGES_PATH + list.get(position).getMsgInfoUrl();
                break;

            case 24:
                mediaPath = GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + list.get(position).getMsgInfoUrl() + ".jpg";
                break;
            case 25:
                mediaPath = GlobalVariables.VIDEO_THUMBNAIL_PATH + "/" + list.get(position).getMsgInfoUrl() + ".jpg";
                break;
        }

        File imgFile = new File(mediaPath);

        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


            imageView.setImageBitmap(myBitmap);

        }

        return imageView;
    }

    // references to our images
//    private Integer[] mThumbIds = {
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7
//    };
}
