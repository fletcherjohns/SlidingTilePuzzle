package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class Tile extends ImageView {

    private int mValue;
    private float mXPos;
    private float mYPos;
    private Thread mSnapThread;

    public Tile(Context context, float XPos, float YPos, int value) {
        super(context);
        mValue = value;
        mYPos = YPos;
        mXPos = XPos;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public float getXPos() {
        return mXPos;
    }

    public void setXPos(float XPos) {
        mXPos = XPos;
    }

    public float getYPos() {
        return mYPos;
    }

    public void setYPos(float YPos) {
        mYPos = YPos;
    }

    public int getRoundedXPos() {
        return Math.round(mXPos);
    }

    public int getRoundedYPos() {
        return Math.round(mYPos);
    }

    public void startThread() {
        mSnapThread = new SnapThread();
        mSnapThread.start();
    }

    public void interruptThread() {
        if (mSnapThread != null && mSnapThread.isAlive()) {
            mSnapThread.interrupt();
        }
    }

    private class SnapThread extends Thread {

        private float mVelocity;
        private int mSnapTo;

        public SnapThread() {
        }

        @Override
        public void run() {
            super.run();
            if (getXPos() % 1 != 0) {
                mSnapTo = Math.round(getXPos());
                while (Math.abs(getXPos() - mSnapTo) > 0.001) {
                    mVelocity = (mSnapTo - getXPos()) / 5;
                    setXPos(getXPos() + mVelocity);
                    post(new LayoutRunnable());
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                setXPos(mSnapTo);
                post(new LayoutRunnable());
            }
            if (getYPos() % 1 != 0) {
                mSnapTo = Math.round(getYPos());
                while (Math.abs(getYPos() - mSnapTo) > 0.001) {
                    mVelocity = (mSnapTo - getYPos()) / 5;
                    setYPos(getYPos() + mVelocity);
                    post(new LayoutRunnable());
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        Log.v("tag", "I've been interrupted :(");
                        e.printStackTrace();
                        return;
                    }
                }
                setYPos(mSnapTo);
                post(new LayoutRunnable());
            }
        }
    }

    private class LayoutRunnable implements Runnable {

        @Override
        public void run() {
            requestLayout();
        }
    }
}
