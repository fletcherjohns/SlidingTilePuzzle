package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

/**
 * Created by Fletcher on 5/10/2015.
 */
public class OldPuzzlePiece extends View {

    private Bitmap mBitmap;
    private Paint mPaint;
    private int mGoalIndex;
    private RectF mBounds;
    private float mCurrentX = -1;
    private float mCurrentY = -1;
    private int mColumnCount;
    private int mValue;

    public OldPuzzlePiece(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBounds = new RectF();
    }

    public void setColumnCount(int columnCount) {
        mColumnCount = columnCount;
    }

    public void setGoalIndex(int goalIndex) {
        mGoalIndex = goalIndex;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.v("tag", "onSizeChanged(" + w + ", " + h + ", " + oldw + ", " + oldh + ") " + mGoalIndex);
        if (mCurrentX == -1 || mCurrentY == -1) {
            mCurrentX = mGoalIndex % mColumnCount;
            mCurrentY = mGoalIndex / mColumnCount;
        }
        setX(mCurrentX * w);
        setY(mCurrentY * h);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.v("tag", "onSaveInstanceState() " + mGoalIndex);
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_instance_state", super.onSaveInstanceState());
        mCurrentX = getX() / getWidth();
        mCurrentY = getY() / getHeight();
        bundle.putFloat("current_x", mCurrentX);
        bundle.putFloat("current_y", mCurrentY);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.v("tag", "onRestoreInstanceState() " + mGoalIndex);
        Bundle bundle = (Bundle) state;
        mCurrentX = bundle.getFloat("current_x");
        mCurrentY = bundle.getFloat("current_y");
        state = bundle.getParcelable("super_instance_state");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    public int getRoundedX() {
        return Math.round(getX() / getWidth()) * getWidth();
    }

    public int getRoundedY() {
        return Math.round(getY() / getWidth()) * getWidth();
    }

    public RectF getBounds() {
        return mBounds;
    }

    public int getIndex() {
        return getRoundedX() / getWidth() + getRoundedY() / getHeight() * mColumnCount;
    }
    public boolean isAtGoal() {
        return getIndex() == mGoalIndex;
    }

    public int getGoalIndex() {
        return mGoalIndex;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
