/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soapp.global.DateTime.DateTimePickerHelper.TimeHelper;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates the spinnerwheel
 */
public abstract class WheelScroller {
    /**
     * Minimum delta for scrolling
     */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;
    public static final int SCROLL_DIRECTION_UP = 1;
    public static final int SCROLL_DIRECTION_DOWN = -1;
    /**
     * Scrolling duration
     */
    private static final int SCROLLING_DURATION = 400;
    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;
    protected Scroller scroller;
    // Listener
    private ScrollingListener listener;
    // Context
    private Context context;
    // Scrolling
    private GestureDetector gestureDetector;
    private int lastScrollPosition;
    private float lastTouchedPosition;
    private boolean isScrollingPerformed;
    // animation handler
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currPosition = getCurrentScrollerPosition();
            int delta = lastScrollPosition - currPosition;
            lastScrollPosition = currPosition;
            if (delta != 0) {
                listener.onScroll(delta);
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currPosition - getFinalScrollerPosition()) < MIN_DELTA_FOR_SCROLLING) {
                // currPosition = getFinalScrollerPosition();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Constructor
     *
     * @param context  the current context
     * @param listener the scrolling listener
     */
    public WheelScroller(Context context, ScrollingListener listener) {
        gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // Do scrolling in onTouchEvent() since onScroll() are not call immediately
                //  when user touch and move the spinnerwheel
                WheelScroller.this.listener.onFling(0);
                return true;
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                lastScrollPosition = 0;
                scrollerFling(lastScrollPosition, (int) velocityX, (int) velocityY);
                setNextMessage(MESSAGE_SCROLL);
                WheelScroller.this.listener.onFling(
                        velocityY < 0 ? SCROLL_DIRECTION_UP : SCROLL_DIRECTION_DOWN);
                return true;
            }

            // public boolean onDown(MotionEvent motionEvent);
        });
        gestureDetector.setIsLongpressEnabled(false);

        scroller = new Scroller(context);

        this.listener = listener;
        this.context = context;
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
    }

    /**
     * Scroll the spinnerwheel
     *
     * @param distance the scrolling distance
     * @param time     the scrolling duration
     */
    public void scroll(int distance, int time) {
        scroller.forceFinished(true);
        lastScrollPosition = 0;
        scrollerStartScroll(distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);
        startScrolling();
    }

    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }

    /**
     * Set the friction of the scroller. This function is available over Android 3.0 (API Level 11).
     *
     * @param friction the amount of friction
     */
    public void setFriction(float friction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scroller.setFriction(friction);
        }
    }

    /**
     * Handles Touch event
     *
     * @param event the motion event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchedPosition = getMotionEventPosition(event);
                scroller.forceFinished(true);
                clearMessages();
                listener.onTouch();
                break;

            case MotionEvent.ACTION_UP:
                if (scroller.isFinished()) listener.onTouchUp();
                break;


            case MotionEvent.ACTION_MOVE:
                // perform scrolling
                int distance = (int) (getMotionEventPosition(event) - lastTouchedPosition);
                if (distance != 0) {
                    startScrolling();
                    listener.onScroll(distance);
                    lastTouchedPosition = getMotionEventPosition(event);
                }
                break;
        }

        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }

        return true;
    }

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    /**
     * Justifies spinnerwheel
     */
    private void justify() {
        listener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY);
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            listener.onStarted();
        }
    }

    /**
     * Finishes scrolling
     */
    protected void finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished();
            isScrollingPerformed = false;
        }
    }

    protected abstract int getCurrentScrollerPosition();

    protected abstract int getFinalScrollerPosition();

    protected abstract float getMotionEventPosition(MotionEvent event);

    protected abstract void scrollerStartScroll(int distance, int time);

    protected abstract void scrollerFling(int position, int velocityX, int velocityY);

    /**
     * Scrolling listener interface
     */
    public interface ScrollingListener {

        void onFling(int direction);

        /**
         * Scrolling callback called when scrolling is performed.
         *
         * @param distance the distance to scroll
         */
        void onScroll(int distance);

        /**
         * This callback is invoked when scroller has been touched
         */
        void onTouch();

        /**
         * This callback is invoked when touch is up
         */
        void onTouchUp();

        /**
         * Starting callback called when scrolling is started
         */
        void onStarted();

        /**
         * Finishing callback called after justifying
         */
        void onFinished();

        /**
         * Justifying callback called to justify a view when scrolling is ended
         */
        void onJustify();
    }
}
