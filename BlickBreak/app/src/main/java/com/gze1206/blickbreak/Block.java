package com.gze1206.blickbreak;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by ggzzz on 2016-12-28.
 */

public class Block implements IDrawable{
    private final float mTop, mLeft, mBottom, mRight;
    private int mHard;

    private boolean mIsCollision = false, mIsExist = true;

    public boolean IsExist() {return mIsExist;}

    public Block(float top, float left, float bottom, float right)
    {
        mTop = top;
        mLeft = left;
        mBottom = bottom;
        mRight = right;
        mHard = 3;
    }

    public void collision()
    {
        mIsCollision = true;
    }

    public void draw(Canvas canvas, Paint paint)
    {
        if (mIsExist)
        {
            if (mIsCollision)
            {
                mHard--;
                mIsCollision = false;
                if (mHard <= 0)
                {
                    mIsExist = false;
                    return;
                }
            }
            if (mHard == 3) paint.setColor(Color.BLUE);
            else if (mHard == 2) paint.setColor(Color.GREEN);
            else paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mLeft, mTop, mRight, mBottom, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4f);
            canvas.drawRect(mLeft, mTop, mRight, mBottom, paint);
        }
    }
}
