package com.soapp.food.food_detail.images;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.soapp.R;
import com.soapp.food.food_detail.FoodDetailLog;
import com.soapp.global.GlobalVariables;
import com.soapp.global.ImagePreviewSlideFood;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * Created by rlwt on 5/18/18.
 */

public class SliderImageAdapter extends PagerAdapter {

    private static int videoDuration = 0;
    MediaPlayer mp;
    ImageButton mute_imgbtn;
    boolean muted = true;
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> images_only = new ArrayList<>();
    private String[] images_string_array;
    private LayoutInflater inflater;
    private Context context;
    private VideoView myVideo;
    private Boolean hasVideoPath;
    private static boolean wasHereClicked = false;

    public SliderImageAdapter(Context context, ArrayList<String> images, Boolean hasVideoPath) {
        this.context = context;
        this.images = new ArrayList<>(images);
        this.images_only = new ArrayList<>(images);
        inflater = LayoutInflater.from(context);
        this.hasVideoPath = hasVideoPath;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (images.get(position).contains(".jpg")) {
            View myImageLayout = inflater.inflate(R.layout.slide_image, container, false);
            ImageView myImage = myImageLayout.findViewById(R.id.image);
            String imgURL = images.get(position);
            myImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImagePreviewSlideFood.class);
                    intent.putExtra("url", imgURL);
                    if (hasVideoPath) {
                        if (!wasHereClicked) {
                            wasHereClicked = true;
                            images_only.remove(0);
                        }
                        intent.putExtra("position", position - 1);
                        images_string_array = new String[images_only.size()];
                        images_string_array = images_only.toArray(images_string_array);
                        ImagePreviewSlideFood.urlList = images_string_array;
                    } else {
                        intent.putExtra("position", position);
                        images_string_array = new String[images_only.size()];
                        images_string_array = images_only.toArray(images_string_array);
                        ImagePreviewSlideFood.urlList = images_string_array;
                    }
                    context.startActivity(intent);
                }
            });

            Glide.with(context)
                    .load(imgURL)
                    .transition(withCrossFade())
                    .into(myImage);
            container.addView(myImageLayout, 0);

            return myImageLayout;
        } else {
            View myVideoLayout = inflater.inflate(R.layout.slide_video, container, false);
            myVideo = myVideoLayout
                    .findViewById(R.id.image);
            mute_imgbtn = myVideoLayout.findViewById(R.id.mute_imgbtn);
            mute_imgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (muted) {
                        unmute();
                    } else {
                        mute();
                    }
                }
            });
            String path = images.get(position);
            File fileFVideo = new File(GlobalVariables.VIDEO_SENT_PATH + "/" + FoodDetailLog.resID + ".mp4"); //put the downloaded file here

            if (!fileFVideo.exists()) {
                Uri uri = Uri.parse(path);
                new DownloaderAsyncTask().execute(path, fileFVideo.getPath());
                myVideo.setVideoURI(uri);
            } else {
                myVideo.setVideoPath(String.valueOf(fileFVideo));
            }

            myVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    mediaPlayer.start();
                                                }
                                            }
            );

            myVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mp = mediaPlayer;
                    mute();
                    mediaPlayer.start();
                }
            });

            container.addView(myVideoLayout, 0);
            return myVideoLayout;
        }
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void mute() {
        mute_imgbtn.setImageResource(R.drawable.ic_mute_100px);
        setVolume(0);
        muted = true;
    }

    public void unmute() {
        mute_imgbtn.setImageResource(R.drawable.ic_unmute_100px);
        setVolume(100);
        muted = false;
    }

    private void setVolume(int amount) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        mp.setVolume(volume, volume);
    }

    class DownloaderAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(String... longs) {

            try {
                URL url = new URL(longs[0]); //you can write here any link
                File fileFVideo = new File(longs[1]);
                long startTime = System.currentTimeMillis();
                /* Open a connection to that URL. */
                URLConnection ucon = url.openConnection();

                /*
                 * Define InputStreams to read from the URLConnection.
                 */
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1).
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                /* Convert the Bytes read to a String. */
                FileOutputStream fos = new FileOutputStream(fileFVideo);
                fos.write(baf.toByteArray());
                fos.close();

            } catch (IOException e) {
            }

            return null;
        }
    }
}
