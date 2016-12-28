package com.gze1206.blickbreak;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by ggzzz on 2016-12-27.
 */

public class GameView extends TextureView implements TextureView.SurfaceTextureListener, View.OnTouchListener{

    private Thread mThread;
    volatile private boolean mIsRunnable;

    volatile private  float mTouchedX, mTouchedY;

    private ArrayList<Block> mBlockList;
    private ArrayList<IDrawable> mItemList;
    private Pad mPad;
    private float mPadHalfWidth;
    private Ball mBall;
    private float mBallRadius;

    private float mBlockWidth, mBlockHeight;
    private int mLife;
    private long mGameStartTime;
    private Handler mHandler;

    static final int BLOCK_COUNT = 100;

    public GameView(final Context context)
    {
        super(context);
        setSurfaceTextureListener(this);
        setOnTouchListener(this);
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message message)
            {
                Intent intent = new Intent(context, ClearActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtras(message.getData());
                context.startActivity(intent);
            }
        };
    }

    public void start()
    {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                while (true)
                {
                    long startTime = System.currentTimeMillis();
                    synchronized (GameView.this)
                    {
                        if (!mIsRunnable) break;
                        Canvas canvas = lockCanvas();
                        if (canvas == null) continue;
                        canvas.drawColor(Color.BLACK);

                        float padLeft = mTouchedX - mPadHalfWidth;
                        float padRight = mTouchedX + mPadHalfWidth;
                        mPad.setLeftRight(padLeft, padRight);
                        mBall.move();

                        float ballTop = mBall.getY() - mBallRadius;
                        float ballLeft = mBall.getX() - mBallRadius;
                        float ballBottom = mBall.getY() + mBallRadius;
                        float ballRight = mBall.getX() + mBallRadius;
                        if (ballLeft < 0 && mBall.getSpeedX() < 0 || ballRight >= getWidth() && mBall.getSpeedX() > 0)
                            mBall.setSpeedX((-mBall.getSpeedX()));
                        if (ballTop < 0)
                            mBall.setSpeedY((-mBall.getSpeedY()));
                        if (ballBottom > getHeight())
                        {
                            if (mLife > 0)
                            {
                                mLife--;
                                mBall.reset();
                            }
                            else
                            {
                                unlockCanvasAndPost(canvas);
                                Message message = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, false);
                                bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, getBlockCount());
                                bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);
                                message.setData(bundle);
                                mHandler.sendMessage(message);
                                return;
                            }
                        }

                        Block leftBlock = getBlock(ballLeft, mBall.getY());
                        Block topBlock = getBlock(mBall.getX(), ballTop);
                        Block rightBlock = getBlock(ballRight, mBall.getY());
                        Block bottomBlock = getBlock(mBall.getX(), ballBottom);

                        boolean isCollision = false;
                        if (leftBlock != null)
                        {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            leftBlock.collision();
                            isCollision = true;
                        }

                        if (topBlock != null)
                        {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            topBlock.collision();
                            isCollision = true;
                        }

                        if (rightBlock != null)
                        {
                            mBall.setSpeedX(-mBall.getSpeedX());
                            rightBlock.collision();
                            isCollision = true;
                        }

                        if (bottomBlock != null)
                        {
                            mBall.setSpeedY(-mBall.getSpeedY());
                            bottomBlock.collision();
                            isCollision = true;
                        }

                        float padTop = mPad.getTop();
                        float ballSpeedY = mBall.getSpeedY();
                        if (
                                        ballBottom > padTop &&
                                        ballBottom - ballSpeedY < padTop &&
                                        padLeft < ballRight &&
                                        padRight > ballLeft
                                )
                        {
                            if (ballSpeedY < mBlockHeight / 3) ballSpeedY *= -1.05f;
                            else ballSpeedY = -ballSpeedY;

                            float ballSpeedX = mBall.getSpeedX() + (mBall.getX() - mTouchedX) / 10;
                            if (ballSpeedX > mBlockWidth / 5) ballSpeedX = mBlockHeight / 5;

                            mBall.setSpeedX(ballSpeedX);
                            mBall.setSpeedY(ballSpeedY);
                        }

                        for (IDrawable item : mItemList) {
                            item.draw(canvas, paint);
                        }
                        unlockCanvasAndPost(canvas);

                        if (isCollision && getBlockCount() == 0)
                        {
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, true);
                            bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, 0);
                            bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                    }
                    long sleepTime = 16 - System.currentTimeMillis() + startTime;
                    if (sleepTime > 0)
                    {
                        try {
                            Thread.sleep(sleepTime);
                        }catch (InterruptedException e) {
                        }

                    }
                }
            }
        });
        mIsRunnable = true;
        mThread.start();
    }

    public void stop()
    {
        mIsRunnable = false;
    }

    public void readyObjects(int width, int height)
    {
        mBlockHeight = height / 20;
        mBlockWidth = width / 10;
        mItemList = new ArrayList<IDrawable>();
        mBlockList = new ArrayList<Block>();
        for (int i = 0; i < BLOCK_COUNT; i++)
        {
            float blockTop = i / 10 * mBlockHeight;
            float blockLeft = i % 10 * mBlockWidth;
            float blockBottom = blockTop + mBlockHeight;
            float blockRight = blockLeft + mBlockWidth;
            mBlockList.add(new Block(blockTop, blockLeft, blockBottom, blockRight));
        }
        mItemList.addAll(mBlockList);

        mPad = new Pad(height * 0.8f, height * 0.85f);
        mItemList.add(mPad);
        mPadHalfWidth = width / 10;

        mBallRadius = width < height ? width / 40 : height / 40;
        mBall = new Ball(mBallRadius, width / 2, height / 2);
        mItemList.add(mBall);

        mLife = 5;
        mGameStartTime = System.currentTimeMillis();
    }

    private Block getBlock(float x, float y)
    {
        int index = (int) (x / mBlockWidth) + (int) (y / mBlockHeight) * 10;
        if (0 <= index && index < BLOCK_COUNT)
        {
            Block block = (Block) mItemList.get(index);
            if (block.IsExist()) return block;
        }
        return null;
    }

    private int getBlockCount()
    {
        int count = 0;
        for (Block block : mBlockList)
            if (block.IsExist()) count++;
        return count;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        readyObjects(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        readyObjects(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (this) { return true; }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mTouchedX = event.getX();
        mTouchedY = event.getY();
        return true;
    }
}
