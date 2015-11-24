package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class Tile extends ImageView {

    private int mValue;
    private float mXPos;
    private float mYPos;
    private Rect mMoveBounds;
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

    public void setBounds(Rect bounds) {
        mMoveBounds = bounds;
    }
    public void moveTo(float x, float y) {

        setXPos(Math.max(mMoveBounds.left,
                Math.min(mMoveBounds.right, x)) / getWidth());
        setYPos(Math.max(mMoveBounds.top,
                Math.min(mMoveBounds.bottom, y)) / getWidth());
    }
    public void moveToPos(float x, float y) {
        moveTo(x * getWidth(), y * getWidth());
    }

    public void startThread() {
        mSnapThread = new Thread(new MoveTileRunnable());
        mSnapThread.start();
    }

    public void startThread(int xPos, int yPos) {
        mSnapThread = new Thread(new MoveTileRunnable(xPos, yPos));
        mSnapThread.start();
    }

    public void interruptThread() {
        if (mSnapThread != null && mSnapThread.isAlive()) {
            mSnapThread.interrupt();
        }
    }

    public boolean isMoving() {
        return mSnapThread != null && mSnapThread.isAlive();
    }

    /**
     * This Runnable can be used to snap a tile to closest position or just to move
     * it to specific position. This should be used wisely and after setting mBounds appropriately
     */
    private class MoveTileRunnable implements Runnable {

        private int mSnapToXPos = -1;
        private int mSnapToYPos = -1;

        /**
         * Use this constructor to move the tile. Be careful to only call it with snapToXPos and
         * snapToYPos of the blank space or the tiles current position. The tile also must be
         * adjacent to the blank.
         * @param snapToXPos
         * @param snapToYPos
         */
        public MoveTileRunnable(int snapToXPos, int snapToYPos) {
            mSnapToXPos = snapToXPos;
            mSnapToYPos = snapToYPos;
        }

        /**
         * Use this constructor to snap the tile into place after a TouchEvent
         */
        public MoveTileRunnable() {
            mSnapToXPos = getRoundedXPos();
            mSnapToYPos = getRoundedYPos();
        }

        @Override
        public void run() {

            float velocityX;
            float velocityY;
            while (Math.abs(getXPos() - mSnapToXPos) > 0.01 ||
                    Math.abs(getYPos() - mSnapToYPos) > 0.01) {

                velocityX = (mSnapToXPos - getXPos()) / 3;
                velocityY = (mSnapToYPos - getYPos()) / 3;

                moveToPos(mXPos + velocityX, mYPos + velocityY);

                post(new LayoutRunnable());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            moveToPos(mSnapToXPos, mSnapToYPos);
            post(new LayoutRunnable());
        }

    }

    private class LayoutRunnable implements Runnable {

        @Override
        public void run() {
            requestLayout();
        }
    }
}
