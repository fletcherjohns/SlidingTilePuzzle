package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class TileArea extends ViewGroup {

    private static int mFlingVelocity;

    private final int[] mGoal;
    private int[] mState;
    private int mColumnCount;
    private int mTileSize;
    private Paint mPaint;
    private Tile mSelectedTile;
    private Callback mCallback;
    private Rect mMoveBounds;
    private float mTouchOffsetX;
    private float mTouchOffsetY;
    private float mVelocityX;
    private float mVelocityY;

    public TileArea(Context context, final int[] goal, int columnCount) {
        super(context);
        mFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

        mState = mGoal = goal;
        mColumnCount = columnCount;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMoveBounds = new Rect();
        post(new Runnable() {
            @Override
            public void run() {

                mTileSize = getWidth() / mColumnCount;
                //Log.v("tag", "tileSize = " + mTileSize);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mTileSize, mTileSize);
                Tile tile;
                for (int i = 0; i < mGoal.length; i++) {
                    if (mGoal[i] > 0) {
                        tile = new Tile(getContext(), i % mColumnCount, i / mColumnCount, mGoal[i]);
                        tile.setLayoutParams(params);
                        tile.setBackgroundColor(Color.BLUE);
                        tile.setImageBitmap(getBitmap(mGoal[i]));
                        //tile.setDrawingCacheEnabled(true);
                        addView(tile);
                    }
                }
            }
        });
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void updateState() {
        Tile tile;
        mState = new int[mState.length];
        for (int i = 0; i < getChildCount(); i++) {
            tile = (Tile) getChildAt(i);
            mState[tile.getRoundedXPos() + tile.getRoundedYPos() * mColumnCount] = tile.getValue();
        }
    }

    private void movePieceTo(float x, float y) {

        mSelectedTile.setXPos(
                Math.max(mMoveBounds.left,
                        Math.min(mMoveBounds.right, x)) / mTileSize);
        mSelectedTile.setYPos(
                Math.max(mMoveBounds.top,
                        Math.min(mMoveBounds.bottom, y)) / mTileSize);
    }

    private void calculateBounds() {
        int blankIndex = ArrayUtils.indexOf(mState, 0);
        Log.v("tag", "blankIndex = " + blankIndex);
        int blankX = blankIndex % mColumnCount * mTileSize;
        int blankY = blankIndex / mColumnCount * mTileSize;
        Log.v("tag", "blankX = " + blankX + ", blankY = " + blankY);
        Log.v("tag", "tileX = " + mSelectedTile.getRoundedXPos() + ", tileY = " + mSelectedTile.getRoundedYPos());
        int tileX = mSelectedTile.getRoundedXPos() * mTileSize;
        int tileY = mSelectedTile.getRoundedYPos() * mTileSize;

        /*
        It may appear wrong at first as this Rect is initialized as a point, not a rectangle.
        Bounds do not represent the bounds of the tile. It is the range that the top left corner
        of the tile can move. It will only ever be a horizontal or vertical line mTileSize long,
        but is easily represented by a Rect.
         */
        mMoveBounds = new Rect(
                tileX,
                tileY,
                tileX,
                tileY);

        if (blankY == tileY && blankX == tileX - mTileSize) {
            Log.v("tag", "blank to left");
            mMoveBounds.left = blankX;
        } else if (blankX == tileX && blankY == tileY - mTileSize) {
            Log.v("tag", "blank above");
            mMoveBounds.top = blankY;
        } else if (blankY == tileY && blankX == tileX + mTileSize) {
            Log.v("tag", "blank to right");
            mMoveBounds.right = blankX;
        } else if (blankX == tileX && blankY == tileY + mTileSize) {
            Log.v("tag", "blank below");
            mMoveBounds.bottom = blankY;
        }
    }

    private Tile intersectsView(float x, float y) {
        Tile tile;
        float tileX;
        float tileY;
        for (int i = 0; i < getChildCount(); i++) {
            tile = (Tile) getChildAt(i);
            tileX = tile.getXPos() * mTileSize;
            tileY = tile.getYPos() * mTileSize;
            if (tileX < x && (tileX + mTileSize) > x &&
                    tileY < y && (tileY + mTileSize) > y) {
                return tile;
            }
        }
        return null;
    }

    public Bitmap getBitmap(int value) {

        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(mTileSize * 0.8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        Rect bounds = new Rect();
        String text = String.valueOf(value);
        // use text bounds to calculate the y position of the text.
        paint.getTextBounds(text, 0, text.length(), bounds);
        // measureText gives a better result for x position of text.
        float textWidth = paint.measureText(text);

        canvas.drawRect(1, 1, mTileSize - 1, mTileSize - 1, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, mTileSize / 2f - textWidth / 2f, mTileSize / 2f + bounds.height() / 2f, paint);
        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int left;
        int top;
        Tile tile;
        for (int i = 0; i < getChildCount(); i++) {
            tile = (Tile) getChildAt(i);
            left = (int) (tile.getXPos() * mTileSize);
            top = (int) (tile.getYPos() * mTileSize);
            Log.v("tag", "tile" + tile.getValue() + ".layout(" +
                    left + ", " + top + ", " + (left + mTileSize) + ", " + (top + mTileSize) + ")");
            tile.layout(
                    left,
                    top,
                    left + mTileSize,
                    top + mTileSize
            );
        }
        //invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                // Sorry I'm a big fan of assigning variables inside of equality checks, maybe I overdo it:
                if ((mSelectedTile = intersectsView(event.getX(), event.getY())) == null) {
                    Log.v("tag", "nothing intersected");
                    return false;
                } else {
                    Log.v("tag", "intersected tile" + mSelectedTile.getValue());
                    mSelectedTile.interruptThread();
                    mTouchOffsetX = event.getX() - mSelectedTile.getX();
                    mTouchOffsetY = event.getY() - mSelectedTile.getY();
                    Log.v("tag", "TouchOffsetX = " + mTouchOffsetX + ", TouchOffsetY = " + mTouchOffsetY);
                    calculateBounds();
                    Log.v("tag", "bounds: " + mMoveBounds.left + ", " + mMoveBounds.top + ", " + mMoveBounds.right + ", " + mMoveBounds.bottom);
                    return true;
                }

            case MotionEvent.ACTION_MOVE:
                int size = event.getHistorySize();
                movePieceTo(event.getX() - mTouchOffsetX, event.getY() - mTouchOffsetY);
                requestLayout();
                if (size > 0) {
                    long time = event.getEventTime() - event.getHistoricalEventTime(size - 1);
                    Log.v("tag", "event time = " + time);
                    try {
                        mVelocityX = (event.getX() - event.getHistoricalX(size - 1)) / time;
                        mVelocityY = (event.getY() - event.getHistoricalY(size - 1)) / time;
                        Log.v("tag", "mVelocityX = " + mVelocityX + ", mVelocityY = " + mVelocityY);
                    } catch (Exception | Error e) {
                        Log.e("tag", e.toString());
                    }
                } else {
                    mVelocityX = mVelocityY = 0;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (mVelocityX > mFlingVelocity || mVelocityY > mFlingVelocity) {
                    Log.v("tag", "FLING!!! x: " + mVelocityX + ", y: " + mVelocityY);
                }
                mSelectedTile.startThread();
                updateState();
                mCallback.stateChanged(mState);
                return true;
            default:
                return false;
        }
    }


    public interface Callback {
        void stateChanged(int[] state);
    }
}
