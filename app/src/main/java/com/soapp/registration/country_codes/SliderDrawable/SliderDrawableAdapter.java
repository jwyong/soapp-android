package com.soapp.registration.country_codes.SliderDrawable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soapp.R;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderDrawableAdapter extends PagerAdapter {

    Integer [] imageArray;
    Context context;
    private LayoutInflater inflater;

    public SliderDrawableAdapter(Context context, Integer [] imageArray) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.imageArray = imageArray;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View myImageLayout = inflater.inflate(R.layout.slide_image,container);
        if (position == 0)
        {
            myImageLayout  = inflater.inflate(R.layout.tutorial_page1, container, false);

        }
        else if (position == 1)
        {
            myImageLayout  = inflater.inflate(R.layout.tutorial_page2, container, false);
        }

        else if (position == 2)
        {
            myImageLayout  = inflater.inflate(R.layout.tutorial_page3, container, false);
        }

        else if (position == 3)
        {
            myImageLayout  = inflater.inflate(R.layout.tutorial_page4, container, false);
        }

        else if (position == 4)
        {
            myImageLayout  = inflater.inflate(R.layout.tutorial_page5, container, false);
        }

        container.addView(myImageLayout, 0);
        return myImageLayout;
    }

    @Override
    public int getCount() {

        return imageArray.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
