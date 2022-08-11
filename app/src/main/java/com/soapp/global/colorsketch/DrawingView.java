package com.soapp.global.colorsketch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.soapp.R;

import java.util.ArrayList;

public class DrawingView extends View {
    private Path mDrawPath;
    private Paint mBackgroundPaint;
    private Paint mDrawPaint;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private ArrayList<Path> mPaths = new ArrayList<>();
    private ArrayList<Paint> mPaints = new ArrayList<>();
    private ArrayList<Path> mUndonePaths = new ArrayList<>();
    private ArrayList<Paint> mUndonePaints = new ArrayList<>();

    // Set default values
    private int mBackgroundColor = 0xFFFFFFFF;
    private int mPaintColor = 0xFF660000;
    private int mStrokeWidth = 10;

    public boolean isdrawing;
    public String sImagepath;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        mDrawPath = new Path();
        mBackgroundPaint = new Paint();


        initPaint();
    }

    private void initPaint() {
        mDrawPaint = new Paint();
        mDrawPaint.setColor(mPaintColor);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(mStrokeWidth);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setFilterBitmap(true);
    }

    private void drawBackground(Canvas canvas) {

//        mBackgroundPaint.setColor(0);
//        canvas.drawRect(0,0,getWidth(),getHeight(),mBackgroundPaint);

        Bitmap bitmap;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float dp = 60f;
        float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        int pixels = Math.round(fpixels);
//        mBackgroundPaint.;

        bitmap = BitmapFactory.decodeFile(sImagepath);
//        bitmap = decodeSampledBitmapFile(sImagepath, width, height);

        if (bitmap != null) {


            if (bitmap.getHeight() > bitmap.getWidth()) {

                Rect imgDestV = new Rect(0, 0, width, (height - pixels));
//                Rect imgDestV = new Rect(0, 0, 200, 200);
                canvas.drawBitmap(bitmap, null, imgDestV, mBackgroundPaint);
            } else {

                Rect imgDestH = new Rect(0, 0, width, getHeight());
                canvas.drawBitmap(bitmap, null, imgDestH, mBackgroundPaint);

            }

        } else {

            Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.default_img_full);
            Rect dest0 = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(bitmap0, null, dest0, null);

        }

    }

//    private void setdrawimg(Canvas canvas){
//        Bitmap bitmap;
//        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
//        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
//        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
//        float dp = 60f;
//        float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
//        int pixels = Math.round(fpixels);
////        mBackgroundPaint.;
//
//        bitmap = BitmapFactory.decodeFile(sImagepath);
////        bitmap = decodeSampledBitmapFile(sImagepath, width, height);
//
//        if (bitmap != null) {
//
//            if (bitmap.getHeight() > bitmap.getWidth()) {
//
//                Rect imgDestV = new Rect(0, 0, width, (height - pixels));
////                Rect imgDestV = new Rect(0, 0, 200, 200);
//                canvas.drawBitmap(bitmap, null, imgDestV, mBackgroundPaint);
//            } else {
//
//                Rect imgDestH = new Rect(0, 0, width, getHeight());
//                canvas.drawBitmap(bitmap, null, imgDestH, mBackgroundPaint);
//
//            }
//
//        } else {
//
//            Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.drawable.default_img_full);
//            Rect dest0 = new Rect(0, 0, getWidth(), getHeight());
//            canvas.drawBitmap(bitmap0, null, dest0, null);
//
//        }
//    }

    private void drawPaths(Canvas canvas) {
        int i = 0;
        for (Path p : mPaths) {
            canvas.drawPath(p, mPaints.get(i));
            i++;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {


        drawBackground(canvas);
        drawPaths(canvas);
        canvas.drawPath(mDrawPath, mDrawPaint);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isdrawing) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDrawPath.moveTo(x, y);
                    mX = x;
                    mY = y;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mDrawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                        mX = x;
                        mY = y;
                    }

                    break;
                case MotionEvent.ACTION_UP:

                    mDrawPath.lineTo(mX, mY);
                    mPaths.add(mDrawPath);
                    mPaints.add(mDrawPaint);
                    mDrawPath = new Path();
                    initPaint();
                    break;
            }
        }
        invalidate();

        return true;
    }

    public void clearCanvas() {
        mPaths.clear();
        mPaints.clear();
        mUndonePaths.clear();
        mUndonePaints.clear();
        mDrawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void setPaintColor(int color) {
        mPaintColor = color;
        mDrawPaint.setColor(mPaintColor);
    }

    public void setPaintStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        mDrawPaint.setStrokeWidth(mStrokeWidth);
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(mBackgroundColor);
        invalidate();
    }

    public Bitmap getBitmap() {
        drawBackground(mDrawCanvas);
//        setdrawimg(mDrawCanvas);
        drawPaths(mDrawCanvas);
        return mCanvasBitmap;
    }

    public void undo() {
        if (mPaths.size() > 0) {
            mUndonePaths.add(mPaths.remove(mPaths.size() - 1));
            mUndonePaints.add(mPaints.remove(mPaints.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if (mUndonePaths.size() > 0) {
            mPaths.add(mUndonePaths.remove(mUndonePaths.size() - 1));
            mPaints.add(mUndonePaints.remove(mUndonePaints.size() - 1));
            invalidate();
        }
    }

    public void isdrawing(boolean b) {

        isdrawing = b;
    }

    public void setImagePath(String simagePath) {
        sImagepath = simagePath;
    }


//    static Bitmap decodeSampledBitmapFile(String filepath, int reqWidth,
//                                          int reqHeight) {
//        // First decode with inJustDecodeBounds=true to check dimensions
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filepath, options);
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(filepath, options);
//    }
//
//    //Given the bitmap size and View size calculate a subsampling size (powers of 2)
//    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        int inSampleSize = 1;   //Default subsampling size
//        // See if image raw height and width is bigger than that of required view
//        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
//            //bigger
//            final int halfHeight = options.outHeight / 2;
//            final int halfWidth = options.outWidth / 2;
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width.
//            while ((halfHeight / inSampleSize) > reqHeight
//                    && (halfWidth / inSampleSize) > reqWidth) {
//                inSampleSize *= 2;
//            }
//        }
//        return inSampleSize;
//    }

}
