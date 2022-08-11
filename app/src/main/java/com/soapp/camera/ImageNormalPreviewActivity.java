package com.soapp.camera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.soappcamera.AspectRatio;
import com.soapp.R;
import com.soapp.base.BaseActivity;
import com.soapp.chat_class.group_chat.GroupChatLog;
import com.soapp.chat_class.single_chat.IndiChatLog;
import com.soapp.global.CameraHelper;
import com.soapp.global.GlideAPI.GlideApp;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImageHelper;
import com.soapp.global.MediaGallery.GalleryMainActivity;
import com.soapp.global.MediaGallery.OpenGallery;
import com.soapp.global.SoappMediaController;
import com.soapp.settings_tab.ContactUs;
import com.soapp.setup.Soapp;
import com.soapp.sql.DatabaseHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ImageNormalPreviewActivity extends BaseActivity {
    public static Activity activity;
    DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
    ImageView normal_preview_imgview;
    //intent
    String from, imageAddress;
    int resID = -1;
    //normal
    boolean fromCamera;
    Bitmap image_bitmap;
    int rotation;
    String orient = "vertical", videoName, videoOrientation;
    File newFile;
    Uri selectedImage, videoMainUri;
    RelativeLayout video_RL;
    VideoView normal_videoview;
    File videoFile;
    Menu topMenu;
    String sensor_orientation;
    //    DrawingView drawingImage;
//    ConstraintLayout drawingView;
//    ColorSlider sliderColor;
    String saveFilePath;
    //    boolean isdrawing;
    ConstraintLayout.LayoutParams paramsDrawImg;
    //basics
    private CameraHelper cameraHelper = new CameraHelper();

    private ExifInterface mExif;
    private ProgressDialog progressDialog;
    private boolean isClicked = false;

//    // color sketch
//    private ColorSlider.OnColorSelectedListener mListener = new ColorSlider.OnColorSelectedListener() {
//
//        @Override
//        public void onColorChanged(int position, int color) {
//            updateView(color);
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_normal_preview_activity);
        setupToolbar();
        activity = this;
        normal_preview_imgview = findViewById(R.id.normal_preview_imgview);
        video_RL = findViewById(R.id.video_RL);
        normal_videoview = findViewById(R.id.normal_videoview);

//        drawingImage = findViewById(R.id.drawingImage);
//        drawingImage.setPaintStrokeWidth(10);
//        drawingView = findViewById(R.id.drawingView);
//        drawingImage.setOnTouchListener(this);
//
//        sliderColor = findViewById(R.id.color_slider);
//        sliderColor.setListener(mListener);
//        sliderColor.setSelection(0);
//        sliderColor.setBackgroundColor(getResources().getColor(R.color.white));


        Intent intent = getIntent();
        if (intent.hasExtra("from")) {
            from = intent.getExtras().getString("from");
            sensor_orientation = intent.getExtras().getString("sensor_orientation");

            //come from camera
            if (intent.hasExtra("camera")) {
                fromCamera = true;
            }

            //if has picture
            if (intent.hasExtra("image_address")) {
                setTitle(R.string.img_preview);
                //for from foodtabnearby (Public image upload)
                if (intent.hasExtra("resID")) {
                    resID = getIntent().getIntExtra("resID", -1);
                }
                if (topMenu != null) {
                    //temporary set to false
                    topMenu.getItem(1).setVisible(true);
                }
                imageAddress = intent.getExtras().getString("image_address");
                if (imageAddress.contains("/storage")) {
                    imageAddress = imageAddress.substring(imageAddress.indexOf("/storage"));
                }

                normal_preview_imgview.setVisibility(View.VISIBLE);
                video_RL.setVisibility(View.GONE);
                //decode into bitmap
                image_bitmap = BitmapFactory.decodeFile(imageAddress);
                if (fromCamera) {
                    int android_version = Build.VERSION.SDK_INT;
                    if (android_version >= 23) {
                    } else {
                        try {
                            mExif = new ExifInterface(imageAddress);
                            if (image_bitmap != null) {
                                //temporary only, still need to test on samsung, htc ,xiaomi and etc seems ok for honor
                                if (mExif != null) {
                                    rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                    checkOrientCamera();
                                }
                            } else {
                                finish();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        mExif = new ExifInterface(imageAddress);
                        if (image_bitmap != null) {
                            //temporary only, still need to test on samsung, htc ,xiaomi and etc seems ok for honor
                            if (mExif != null) {
                                rotation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                checkOrientCamera();
                            }
                        } else {
                            finish();
                        }
                    } catch (IOException ignore) {
                    }
                }
                ImageHelper imageHelper = new ImageHelper(this);
                image_bitmap = imageHelper.scaleDown3KImage(image_bitmap);
//                drawingImage.setImagePath(imageAddress);
//                String compressedFilePath = ImageHelper.ryanCompressAndSaveImage(imageAddress, Uri.fromFile(new File(imageAddress)));
//                drawingImage.setImagePath(setImgWH(compressedFilePath));

                GlideApp.with(this)
                        .asBitmap()
                        .placeholder(R.drawable.default_img_full)
                        .load(imageAddress)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .thumbnail(0.5f)
                        .encodeQuality(50)
                        .into(normal_preview_imgview);

//                normal_preview_imgview.setImageBitmap(image_bitmap);

            } else if (intent.hasExtra("video")) {
                setTitle(R.string.video_preview);
                if (topMenu != null) {
                    //temporary set to false
                    topMenu.getItem(1).setVisible(false);
                }

                normal_preview_imgview.setVisibility(View.GONE);
                video_RL.setVisibility(View.VISIBLE);
                normal_videoview.setVisibility(View.VISIBLE);
                normal_videoview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playVideo();
                    }
                });
                String videoUriString = getIntent().getStringExtra("video");
                videoFile = new File(videoUriString);
                Uri videoUri = Uri.fromFile(videoFile);
                videoName = videoFile.getName();

                SoappMediaController controller = new SoappMediaController(this);
                controller.setAnchorView(normal_videoview);
                controller.setMediaPlayer(normal_videoview);
                normal_videoview.setMediaController(controller);
                normal_videoview.setVideoURI(videoUri);
                videoMainUri = videoUri;

//                if (topMenu != null) {
//                    topMenu.getItem(1).setVisible(false);
//                    topMenu.getItem(2).setVisible(false);
//
//                }
                if (from.equals("chat")) {
                    Soapp.getInstance().getApplicationContext().sendBroadcast(new Intent(Intent
                            .ACTION_MEDIA_SCANNER_SCAN_FILE, videoMainUri));
                }

                normal_videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        ViewGroup.LayoutParams lp = normal_videoview.getLayoutParams();
                        float videoWidth = mediaPlayer.getVideoWidth();
                        float videoHeight = mediaPlayer.getVideoHeight();
                        float viewWidth = normal_videoview.getWidth();
                        float viewHeight = normal_videoview.getHeight();
                        //vertical video
                        if (videoHeight > videoWidth) {
                            //view is horizontal
                            if (viewWidth > viewHeight) {
                                lp.width = (int) videoWidth;
                            }
                        }
                        //horizontal video
                        else {
                            //view is horizontal
                            if (viewWidth > viewHeight) {
                                lp.height = (int) (viewWidth);
                            }
                            //view is vertical
                            else {
                                lp.width = (int) (viewWidth * (videoWidth / videoHeight));
                            }
                        }
                        normal_videoview.seekTo(100);
                        controller.show();
//                        playVideo();
                    }
                });

            }
        } else {
            finish();
        }
    }

    //check image orientation
    public void checkOrientCamera() {
        //rotate bitmap image after taking photo with camera
        image_bitmap = cameraHelper.rotateCameraBitmap(image_bitmap, sensor_orientation, rotation, this);

        if (image_bitmap.getHeight() > image_bitmap.getWidth() || image_bitmap.getHeight() > image_bitmap.getWidth()) {
            orient = "vertical";
        } else if (image_bitmap.getWidth() > image_bitmap.getHeight() || image_bitmap.getWidth() > image_bitmap.getHeight()) { //left
            orient = "horizontal";
        } else {
            orient = "vertical";
        }
    }

    //menu thing do here
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        topMenu = menu;
        menu.getItem(0).setVisible(false);

        if (video_RL.getVisibility() == View.VISIBLE) {
            menu.getItem(1).setVisible(false);
        } else {
            menu.getItem(1).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_normal_preview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (image_bitmap != null) {
            if (id == R.id.action_crop) {
                progressDialog = ProgressDialog.show(ImageNormalPreviewActivity.this, null, "Preparing image");
                Intent in = new Intent(ImageNormalPreviewActivity.this, ImageCropPreviewActivity.class);
                in.putExtra("from", from);

                if (!imageAddress.equals(null)) {
                    if (from.equals("chatmedia")) {
                        //copy the image to cache and do all the cropping and rotating on that image
                        try {
                            File copiedFile = Util.copyFile(new File(imageAddress), getCacheDir());
                            if (copiedFile != null) {
                                in.putExtra("image_address", copiedFile.getPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        in.putExtra("image_address", imageAddress);
                    }
                }


//                if (drawingImage.getBitmap() != null) {
//
//                    String drawingTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
//
//                    File filesDir = getCacheDir();
//                    File imageFile = new File(filesDir, "dImg" + drawingTime + ".jpg");
//
//                    OutputStream os;
//                    try {
//                        os = new FileOutputStream(imageFile);
//                        drawingImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, os);
//                        os.flush();
//                        os.close();
//                    } catch (Exception e) {
//                        Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
//                    }
//                    if (from.equals("chatmedia") || from.equals("external_image")) {
//                        //copy the image to cache and do all the cropping and rotating on that image
//
//                        in.putExtra("image_address", imageFile.getPath());
//
//                    } else {
//                        in.putExtra("image_address", String.valueOf(imageFile));
//                    }
//
//                }

                String jid = getIntent().getStringExtra("jid");
                in.putExtra("jid", jid);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    startActivity(in);
                }
            } else if (id == R.id.action_save) {
                if (!isClicked) {
                    isClicked = true;
//                    String drawingTime1 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
//
//                    if (from.equals("chatmedia")) {
//
//                        saveFilePath = GlobalVariables.IMAGES_SENT_PATH;
//
//                    } else if (from.equals("chat")) {
//
//                        saveFilePath = getCacheDir().toString();
//                    }
//
//                    File imageFile = new File(saveFilePath, "dImg" + drawingTime1 + ".jpg");
//
//                    OutputStream os;
//                    try {
//                        os = new FileOutputStream(imageFile);
//                        drawingImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, os);
//                        os.flush();
//                        os.close();
//                    } catch (Exception e) {
//                        Log.e("jason", "Error writing bitmap" + e);
//                    }

//                    new SaveFile().execute(imageAddress.getPath());
                    new SaveFile().execute(imageAddress);
                    switch (from) {
                        case "chatmedia":
                            GalleryMainActivity.activity.finish();
                            OpenGallery.activity.finish();
                            break;

                        case "chat":
                            cameraHelper.finishCameraIntent();
                            break;

                        default:
                            break;
                    }
                    finish();

                }
            } else if (id == R.id.colorPen) {

//                if (sliderColor.getVisibility() == View.VISIBLE) {
//
//                    sliderColor.setVisibility(View.GONE);
//                    drawingImage.isdrawing(false);
//                    isdrawing = false;
//                } else {
//                    drawingImage.isdrawing(true);
//                    sliderColor.setVisibility(View.VISIBLE);
//                    drawingImage.setPaintColor(Color.BLACK);
//                    isdrawing = true;
//                }

            }
//            else if (id == R.id.undoDraw) {
//
//                drawingImage.undo();
//
//            }

        } else if (id == R.id.action_save) {
            if (!isClicked) {
                isClicked = true;

//                ProgressDialog pd_makeVideoThumb = ProgressDialog.show(ImageNormalPreviewActivity.this, null, "processing");
//                pd_makeVideoThumb.show();
                //if the video haven't done compress
                //user will view video from original path
                //else
                //user will view the compress video from our SENT folder

                //this is the video original file path
                new SaveVideoThumb(videoMainUri, videoOrientation, fromCamera).execute();

                //<<-------------- [note ryan ]-------------->>
                //need to confirm the height and width of the thumbnail, currently is 240 x 240

//                resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 240, 240);
//                if (thumb.getWidth() > thumb.getHeight()) {
//                    if (thumb.getHeight() <= 480) {
//                        fromCamera = true;
//                    }
//                    if (ratio.getX() == 16) {
//                        resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 427, 240);
//                    } else {
//                        resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 320, 240);
//                    }
//                    videoOrientation = "horizontal";
//                } else {
//                    if (thumb.getWidth() <= 480) {
//                        fromCamera = true;
//                    }
//                    if (ratio.getY() == 16) {
//                        resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 240, 427);
//                    } else {
//                        resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 240, 320);
//                    }
//                    videoOrientation = "vertical";
//                }
                if (from.equals("chatmedia")) {
                    GalleryMainActivity.activity.finish();
                    OpenGallery.activity.finish();
                } else {
                    cameraHelper.finishCameraIntent();
                }
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    // end menu thing do here

    //video related function
    public void playVideo() {
        if (normal_videoview.isPlaying()) return;
        normal_videoview.start();
    }

//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//
//        switch (view.getId()) {
//            case R.id.drawingImage:
//                if (isdrawing) {
//                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//
//                        sliderColor.setVisibility(View.GONE);
//
//                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//
//                        sliderColor.setVisibility(View.VISIBLE);
//
//                    }
//                }
//
//                break;
//
//        }
//
//        return false;
//    }

    //save file
    public class SaveFile extends AsyncTask<String, Void, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(String... params) {
            return saveFile(params[0]);
        }

        private File saveFile(String address) {
            final String jid = getIntent().getStringExtra("jid");
            final String uniqueId = UUID.randomUUID().toString();
            final long currentDate = System.currentTimeMillis();
            if (Util.isExternalStorageWritable()) {
                if (from.equals("chatmedia") || from.equals("chat") || from.equals("foodNearbyTabItem") || from.equals("external_image")) {
                    newFile = new File(GlobalVariables.IMAGES_PATH);
                    selectedImage = Uri.fromFile(newFile);
                }
                try {
                    switch (from) {
                        case "chatmedia": //media from gallery
                            //trying to do all the things in chat holder, address is the choosen image path
                            if (jid.length() == 12) {
                                Log.i("ryan","SaveFile chatmedia");
                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", false);
                            } else {
                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", false);
                            }
//                            GalleryMainActivity.activity.finish();
//                            OpenGallery.activity.finish();
//                            finish();
                            break;

                        case "chat": //media from camera
                            //trying to do all the things in chat holder, address is the choosen image path
                            if (jid.length() == 12) {
                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", true);
                            } else {
                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", true);

                            }
//                            cameraHelper.finishCameraIntent();
//                            finish();
                            break;

                        case "external_image":
                            if (jid.length() == 12) {
                                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", false);
                                Intent in = new Intent(ImageNormalPreviewActivity.this, IndiChatLog.class);
                                in.putExtra("jid", jid);
                                startActivity(in);
                            } else {
                                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, address, orient, "image", false);
                                Intent in = new Intent(ImageNormalPreviewActivity.this, GroupChatLog.class);
                                in.putExtra("jid", jid);
                                startActivity(in);
                            }

                            break;

                        case "Contact":
                            ContactUs.selectedImage = selectedImage;
                            finish();
                            break;

                        default:

                            break;
                    }

                } catch (NullPointerException e) {
                    Log.i("diao", e.toString());
                }
                return newFile;
            } else {
                // save to internal storage
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {

            super.onPostExecute(file);
        }
    }

    public class SaveVideoThumb extends AsyncTask<Void, Void, Void> {

        //        videoMainUri,videoOrientation,fromCamera
        Uri videoMainUri;
        String videoOrientation;
        Boolean fromCamera;

        public SaveVideoThumb(Uri videoMainUri, String videoOrientation, Boolean fromCamera) {
            this.videoMainUri = videoMainUri;
            this.videoOrientation = videoOrientation;
            this.fromCamera = fromCamera;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File videoMainFile = new File(videoMainUri.getPath());
            Bitmap resizedThumb;

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoMainFile.getPath(),
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);

            AspectRatio ratio = AspectRatio.of(thumb.getWidth(), thumb.getHeight());
            resizedThumb = ThumbnailUtils.extractThumbnail(thumb, 240, 240);

            String uniqueId = UUID.randomUUID().toString();

            if (thumb != null) {
                //thumbnailfile, use uniqueId as temporary name, need to rename after compressed
                File thumbnailFileWithUniqurIdName = new File(GlobalVariables.VIDEO_SENT_THUMBNAIL_PATH + "/" + uniqueId + ".jpg");
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(thumbnailFileWithUniqurIdName);
                    resizedThumb.compress(Bitmap.CompressFormat.JPEG, 25, fileOutputStream);
                } catch (FileNotFoundException e) {
                }

            }
            String jid = getIntent().getStringExtra("jid");
            long currentDate = System.currentTimeMillis();
            if (jid.length() == 12) { //indi chat
                databaseHelper.ImageOutputDatabase(jid, uniqueId, currentDate, videoMainFile.getPath(), videoOrientation, "video", fromCamera);
            } else {
                databaseHelper.GroupImageOutputDatabase(jid, uniqueId, currentDate, videoMainFile.getPath(), videoOrientation, "video", fromCamera);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }
    //end video related function


//    private void updateView(@ColorInt int color) {
//
//        drawingImage.setPaintColor(color);
//        if (color == Color.parseColor("#000000")) {
//
//            sliderColor.setBackgroundColor(getResources().getColor(R.color.white));
//        } else if (color == Color.parseColor("#FFFFFF")) {
//            sliderColor.setBackgroundColor(getResources().getColor(R.color.black));
//
//        }
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//
//            case 3000:
//
//                if (data != null) {
//                    if (data.hasExtra("cropImg")) {
//
//                        String cropImg = data.getStringExtra("cropImg");
//
//
//                        drawingImage.clearCanvas();
//                        drawingImage.setImagePath(setImgWH(cropImg));
//
//                    }
//                }
//
//                break;
//        }
//    }


//    public String setImgWH(String image) {
//
//        Bitmap checkSize = BitmapFactory.decodeFile(image);
//        if (checkSize.getHeight() < checkSize.getWidth()) {
//            DisplayMetrics metrics = this.getResources().getDisplayMetrics();
//            float dp = 250f;
//            float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
//            int pixels = Math.round(fpixels);
//
//            paramsDrawImg = new ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT, pixels);
//
//            paramsDrawImg.topToTop = drawingView.getId();
//            paramsDrawImg.startToStart = drawingView.getId();
//            paramsDrawImg.endToEnd = drawingView.getId();
//            paramsDrawImg.bottomToBottom = drawingView.getId();
//
//            drawingImage.setLayoutParams(paramsDrawImg);
//
//        } else if (checkSize.getHeight() > checkSize.getWidth()) {
//
//            paramsDrawImg = new ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.MATCH_PARENT);
//
//            drawingImage.setLayoutParams(paramsDrawImg);
//
//
//        }
//
//        return image;
//    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
    }
}
