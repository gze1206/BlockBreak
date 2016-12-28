package com.gze1206.blickbreak;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ggzzz on 2016-12-28.
 */

public class Pad implements IDrawable{
    private final float mTop, mBottom;
    private float mLeft, mRight;

    public float getTop() {return mTop;}

    public Pad(float top, float bottom)
    {
        mTop = top;
        mBottom = bottom;
    }

    public void setLeftRight(float left, float right)
    {
        mLeft = left;
        mRight = right;
    }

    public void draw(Canvas canvas, Paint paint)
    {
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(mLeft, mTop, mRight, mBottom, paint);
    }
}
