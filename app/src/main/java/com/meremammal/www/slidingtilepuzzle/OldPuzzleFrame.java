package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by Fletcher on 5/10/2015.
 */
public class OldPuzzleFrame extends FrameLayout {

    private static int mFlingVelocity;
    private int mColumnCount;
    private int mSideLength;
    private OldPuzzlePiece mSelectedPiece;
    private float mTouchOffsetX;
    private float mTouchOffsetY;
    private int[] mViewIds;
    private Thread mSnapThread;
    private float mVelocityX = 0;
    private float mVelocityY = 0;
    private Callback mCallback;

    public OldPuzzleFrame(Context context, AttributeSet attributes) {
        super(context, attributes);

        mFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        //Log.v("tag", "mFlingVelocity = " + mFlingVelocity);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 0;
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                // Intentionally falls through to next case
            case MeasureSpec.AT_MOST:
                switch (MeasureSpec.getMode(heightMeasureSpec)) {
                    case MeasureSpec.UNSPECIFIED:
                        // Again intentional fall through
                    case MeasureSpec.AT_MOST:
                        throw new IllegalArgumentException("wrong layout params for puzzle frame." +
                                "Should be one match_parent and one wrap_content");
                    case MeasureSpec.EXACTLY:
                        size = MeasureSpec.getSize(heightMeasureSpec);
                }
                break;
            case MeasureSpec.EXACTLY:
                Log.v("tag", "we're going by width");
                size = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(size, size);
    }

    public void init(int[] goal, int columnCount, Callback callback) {
        mColumnCount = columnCount;
        if (getChildCount() == 0) {
            OldPuzzlePiece piece;
            LayoutParams params = new LayoutParams(
                    mSideLength / mColumnCount,
                    mSideLength / mColumnCount
            );
            for (int i = 0; i < goal.length; i++) {
                if (goal[i] != 0) {
                    piece = new OldPuzzlePiece(getContext());
                    piece.setLayoutParams(params);
                    piece.setValue(goal[i]);
                    piece.setGoalIndex(i);
                    piece.setColumnCount(mColumnCount);
                    piece.setId(View.generateViewId());
                    addView(piece);
                }
            }
        }
        mCallback = callback;
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_instance_state", super.onSaveInstanceState());
        if (mViewIds == null) {
            mViewIds = new int[getChildCount()];
            for (int i = 0; i < mViewIds.length; i++) {
                mViewIds[i] = getChildAt(i).getId();
            }
        }
        bundle.putIntArray("view_ids", mViewIds);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable("super_instance_state");
        if ((mViewIds = bundle.getIntArray("view_ids")) != null) {
            for (int i = 0; i < mViewIds.length; i++) {
                getChildAt(i).setId(mViewIds[i]);
            }
        }
        super.onRestoreInstanceState(state);
    }

    private Bitmap getBitmap(int value) {
        int bitmapSize = mSideLength / mColumnCount;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(bitmapSize * 0.8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        Rect bounds = new Rect();
        String text = String.valueOf(value);
        paint.getTextBounds(text, 0, text.length(), bounds);
        float textWidth = paint.measureText(text);
        canvas.drawRect(1, 1, bitmapSize - 1, bitmapSize - 1, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, bitmapSize / 2f - textWidth / 2f, bitmapSize / 2f + bounds.height() / 2f, paint);
        return bitmap;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.v("tag", "onSizeChanged(" + w + ", " + h + ", " + oldw + ", " + oldh + ") frame");
        super.onSizeChanged(w, h, oldw, oldh);

        mSideLength = w;

        OldPuzzlePiece piece;
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            (piece = (OldPuzzlePiece) getChildAt(i)).setBitmap(getBitmap(piece.getValue()));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.v("tag", "onLayout() frame");
        if (getChildCount() > 0) {
            int pieceWidth = (right - left) / mColumnCount;
            OldPuzzlePiece piece;
            int pieceLeft;
            int pieceTop;
            for (int i = 0; i < getChildCount(); i++) {
                piece = (OldPuzzlePiece) getChildAt(i);
                pieceLeft = (int) (piece.getX());
                pieceTop = (int) (piece.getY());
                Log.v("tag", "piece.layout(" + pieceLeft + ", " + pieceTop + ", " + (pieceLeft + pieceWidth) + ", " + (pieceTop + pieceWidth) + ")");
                piece.layout(
                        pieceLeft,
                        pieceTop,
                        pieceLeft + pieceWidth,
                        pieceTop + pieceWidth
                );
                Log.v("tag", "child " + piece.getValue() + " position " + piece.getX() + ", " + piece.getY());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                OldPuzzlePiece touchedPiece;
                if ((touchedPiece = intersectsView(event.getX(), event.getY())) == mSelectedPiece
                        && mSnapThread != null) {
                    Log.v("tag", "interrupting...................");
                    mSnapThread.interrupt();
                } else if ((mSelectedPiece = touchedPiece) == null) {
                    return false;
                } else {
                    mTouchOffsetX = event.getX() - mSelectedPiece.getX();
                    mTouchOffsetY = event.getY() - mSelectedPiece.getY();
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                setPieceBounds(mSelectedPiece, mSelectedPiece.getBounds());
                int size = event.getHistorySize();
                for (int i = 0; i < size; i++) {
                    movePieceTo(event.getHistoricalX(i) - mTouchOffsetX,
                            event.getHistoricalY(i) - mTouchOffsetY);
                }
                movePieceTo(event.getX() - mTouchOffsetX,
                        event.getY() - mTouchOffsetY);
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
                (mSnapThread = new SnapThread()).start();
                return true;
            default:
                return false;
        }
    }

    private void movePieceTo(float x, float y) {

        mSelectedPiece.setX(
                Math.max(mSelectedPiece.getBounds().left,
                        Math.min(mSelectedPiece.getBounds().right, x)));
        mSelectedPiece.setY(
                Math.max(mSelectedPiece.getBounds().top,
                        Math.min(mSelectedPiece.getBounds().bottom, y)));
    }

    private boolean checkForAWinner() {
        int size = getChildCount();
        OldPuzzlePiece piece;
        for (int i = 0; i < size; i++) {
            piece = (OldPuzzlePiece) getChildAt(i);
            if (!piece.isAtGoal()) {
                return false;
            }
        }

        return true;
    }

    public OldPuzzlePiece intersectsView(float x, float y) {
        OldPuzzlePiece piece;
        for (int i = 0; i < getChildCount(); i++) {
            piece = (OldPuzzlePiece) getChildAt(i);
            if (x > piece.getX() && x < piece.getX() + piece.getWidth()
                    && y > piece.getY() && y < piece.getY() + piece.getWidth()) {
                return piece;
            }
        }
        return null;
    }

    public int[] getTileState() {
        int[] state = new int[getChildCount() + 1];
        OldPuzzlePiece piece;
        for (int i = 0; i < getChildCount(); i++) {
            piece = (OldPuzzlePiece) getChildAt(i);
            Log.v("tag", "piece.getIndex() = " + piece.getIndex());
             state[piece.getIndex()] = piece.getValue();
        }
        return state;
    }

    public void setPieceBounds(OldPuzzlePiece piece, RectF bounds) {

        bounds.left = 0;
        bounds.top = 0;
        bounds.right = getWidth() - piece.getWidth();
        bounds.bottom = getHeight() - piece.getHeight();

        float left = piece.getRoundedX();
        float top = piece.getRoundedY();
        float right = left + piece.getWidth();
        float bottom = top + piece.getHeight();

        OldPuzzlePiece other;
        float otherLeft;
        float otherTop;
        float otherRight;
        float otherBottom;

        for (int i = 0; i < getChildCount(); i++) {
            other = (OldPuzzlePiece) getChildAt(i);
            if (other == piece) continue;

            otherLeft = other.getRoundedX();
            otherTop = other.getRoundedY();
            otherRight = otherLeft + other.getWidth();
            otherBottom = otherTop + other.getHeight();


            if (otherBottom > top && otherTop < bottom) {

                if (otherRight <= left) {
                    bounds.left = Math.max(bounds.left, otherRight);
                } else if (otherLeft >= right) {
                    bounds.right = Math.min(bounds.right, otherLeft - piece.getWidth());
                }
            }
            if (otherRight > left && otherLeft < right) {
                if (otherBottom <= top) {
                    bounds.top = Math.max(bounds.top, otherBottom);
                } else if (otherTop >= bottom) {
                    bounds.bottom = Math.min(bounds.bottom, otherTop - piece.getHeight());
                }
            }
        }
        Log.v("tag", "bounds = " + bounds.left + ", " + bounds.top + ", " + bounds.right + ", " + bounds.bottom);
    }

    private class SnapThread extends Thread {

        private OldPuzzlePiece mPiece;
        private float mVelocity;
        private int mSnapTo;

        public SnapThread() {
            mPiece = mSelectedPiece;
        }

        @Override
        public void run() {
            super.run();
            if (mPiece.getX() % mPiece.getWidth() != 0) {
                mSnapTo = Math.round(mPiece.getX() / mPiece.getWidth())
                        * mPiece.getWidth();
                while (Math.abs(mPiece.getX() - mSnapTo) > 1) {
                    mVelocity = (mSnapTo - mPiece.getX()) / 5;
                    mPiece.setX(mPiece.getX() + mVelocity);
                    try {
                        sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                mPiece.setX(mSnapTo);
            }
            if (mPiece.getY() % mPiece.getWidth() != 0) {
                mSnapTo = Math.round(mPiece.getY() / mPiece.getWidth())
                        * mPiece.getWidth();
                while (Math.abs(mPiece.getY() - mSnapTo) > 1) {
                    mVelocity = (mSnapTo - mPiece.getY()) / 5;
                    mPiece.setY(mPiece.getY() + mVelocity);
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        Log.v("tag", "I've been interrupted :(");
                        e.printStackTrace();
                        return;
                    }
                }
                mPiece.setY(mSnapTo);
            }
            mCallback.stateChanged(getTileState());
            if (checkForAWinner()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Winner!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public interface Callback {
        void stateChanged(int[] state);
    }
}
