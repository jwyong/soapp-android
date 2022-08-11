package com.soapp.camera;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

/**
 * Created by ryan on 29/12/2017.
 */

public class TouchableImageButton extends AppCompatImageButton {
    public TouchableImageButton(Context context) {
        super(context);
    }

    public TouchableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    boolean isLongPress = false;
//    @Override
//    public boolean performLongClick() {
////        CameraActivity.recordVideo();
//        isLongPress = true;
//
//        return super.performLongClick();
//    }
//
//    @Override
//    public boolean performClick() {
//        if(isLongPress){
//            CameraActivity.camera.stopCapturingVideo();
//            CameraActivity.capturePhotoImgBtn.setImageResource(android.R.color.transparent);
//            isLongPress = false;
//        }else{
//            CameraActivity.capturePhoto();
//        }
//        return super.performClick();
//    }
//
//    @Override
//    public boolean performLongClick(float x, float y) {
//        return super.performLongClick(x, y);
//    }
}
