package com.soapp.global.DateTime.DateTimePickerHelper.DateHelper.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class DotSpan1 implements LineBackgroundSpan {

    /**
     * Default radius used
     */
    public static final float DEFAULT_RADIUS = 3;

    private final float radius;
    private final int color;

    /**
     * Create a span to draw a dot using default radius and color
     *
     * @see #DotSpan1(float, int)
     * @see #DEFAULT_RADIUS
     */
    public DotSpan1() {
        this.radius = DEFAULT_RADIUS;
        this.color = 0;
    }

    /**
     * Create a span to draw a dot using a specified color
     *
     * @param color color of the dot
     * @see #DotSpan1(float, int)
     * @see #DEFAULT_RADIUS
     */
    public DotSpan1(int color) {
        this.radius = DEFAULT_RADIUS;
        this.color = color;
    }

    /**
     * Create a span to draw a dot using a specified radius
     *
     * @param radius radius for the dot
     * @see #DotSpan1(float, int)
     */
    public DotSpan1(float radius) {
        this.radius = radius;
        this.color = 0;
    }

    /**
     * Create a span to draw a dot using a specified radius and color
     *
     * @param radius radius for the dot
     * @param color  color of the dot
     */
    public DotSpan1(float radius, int color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        int oldColor = paint.getColor();
        if (color != 0) {
            paint.setColor(color);
        }

        int leftgo = 0;

        canvas.drawCircle((left + right) / 2 - leftgo, bottom + radius, radius, paint);
        paint.setColor(oldColor);
    }
}
