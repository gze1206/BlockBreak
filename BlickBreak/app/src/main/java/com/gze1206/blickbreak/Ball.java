package com.gze1206.blickbreak;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ggzzz on 2016-12-28.
 */

public class Ball implements IDrawable{
    private float mX, mY, mSpeedX, mSpeedY;
    private final float mRadius;

    private final float mInitialSpeedX, mInitialSpeedY;
    private final float mInitialX, mInitialY;

    public Ball(float radius, float initialX, float initialY)
    {
        mRadius = radius;
        mSpeedX = radius / 5;
        mSpeedY = radius / 5;
        mX = initialX;
        mY = initialY;
        mInitialSpeedX = mSpeedX;
        mInitialSpeedY = mSpeedY;
        mInitialX = mX;
        mInitialY = mY;
    }

    public void reset()
    {
        mX = mInitialX;
        mY = mInitialY;
        mSpeedX = mInitialSpeedX;
        mSpeedY = mInitialSpeedY;
    }

    public void move()
    {
        mX += mSpeedX;
        mY += mSpeedY;
    }

    public void draw(Canvas canvas, Paint paint)
    {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mX, mY, mRadius, paint);
    }

    public float getX() {return mX;}
    public float getY() {return mY;}
    public float getSpeedX() {return mSpeedX;}
    public float getSpeedY() {return mSpeedY;}

    public void setSpeedX(float speedX) {mSpeedX = speedX;}

    public void setSpeedY(float speedY) {mSpeedY = speedY;}
}
