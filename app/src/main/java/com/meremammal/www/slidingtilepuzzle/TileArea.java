package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.meremammal.www.slidingtilepuzzle.search_algorithms.Heuristic;
import com.meremammal.www.slidingtilepuzzle.search_algorithms.IDAStar;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Fletcher on 18/11/2015.
 *
 * <p>It utilizes a {@link com.meremammal.www.slidingtilepuzzle.search_algorithms.Heuristic} to
 * solve the puzzle. </p>
 */
public class TileArea extends ViewGroup {

    private static final String SUPER_PARCELABLE = "super_parcelable";
    private static final String CURRENT_STATE = "current_state";
    private static final String CURRENT_SOLUTION = "current_solution";
    /*
        *TODO Need to implement either a boolean field mBusy or use the State Machine pattern to allow
        *  this ViewGroup to run background threads to shuffle or solve the tiles and block any touch
        *  input or button presses coming from the Activity. Simplest way is to set mBusy and use an
        *  if(!mBusy) in the onTouchEvent() method. Create method to move a tile from current position
        *  to blank. Create method to move many tiles in series (shuffle). ie: loop (move random tile
        *  adjacent to blank). Must keep track of last tile moved to avoid moving the same tile back and
        *  forth.
        *  Create public methods to be called from the Activity to start mThread to run shuffle or solve
        *  methods. Set mBusy to true while thread is running and set to false when finished. Again use
        *  if (!mBusy) to ignore call when thread already running.
        *  Could also just use if (mThread != null && mThread.isAlive()) to check if the thread is
        *  already in process.
        */
    private static int mFlingVelocity;

    private int[] mState;
    private int mColumnCount;
    private int mTileSize;
    private Paint mPaint;
    private Tile mSelectedTile;
    private Callback mCallback;
    private float mTouchOffsetX;
    private float mTouchOffsetY;
    private Heuristic<int[]> mHeuristic;
    private Thread mThread;
    private Thread mSearchThread;
    private int[] mSolution;

    public TileArea(Context context, final int[] goal, int columnCount, Heuristic<int[]> heuristic) {
        super(context);
        mFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

        if (mState == null) mState = goal;
        Log.v("tag", "state in onCreate() " + Arrays.toString(mState));
        mColumnCount = columnCount;
        mHeuristic = heuristic;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        post(new Runnable() {
            @Override
            public void run() {

                mTileSize = getWidth() / mColumnCount;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mTileSize, mTileSize);
                Tile tile;
                for (int i = 0; i < mState.length; i++) {
                    if (mState[i] > 0) {
                        tile = new Tile(getContext(), i % mColumnCount, i / mColumnCount, mState[i]);
                        tile.setLayoutParams(params);
                        tile.setBackgroundColor(Color.GRAY);
                        tile.setImageBitmap(getBitmap(mState[i]));
                        addView(tile);
                    }
                }
                updateState();
            }
        });

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_PARCELABLE, super.onSaveInstanceState());
        Log.v("tag", "state in saveInstanceState() " + Arrays.toString(mState));
        bundle.putIntArray(CURRENT_STATE, mState);
        bundle.putIntArray(CURRENT_SOLUTION, mSolution);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null) return;
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable(SUPER_PARCELABLE);
        mState = bundle.getIntArray(CURRENT_STATE);
        mSolution = bundle.getIntArray(CURRENT_SOLUTION);
        Log.v("tag", "state in restoreInstanceState() " + Arrays.toString(mState));
        super.onRestoreInstanceState(state);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
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

            tile.layout(
                    left,
                    top,
                    left + mTileSize,
                    top + mTileSize
            );
        }
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
    public boolean onTouchEvent(MotionEvent event) {

        if (mThread == null || !mThread.isAlive()) {
            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    // Sorry I'm a big fan of assigning variables inside of equality checks, maybe I overdo it:
                    if ((mSelectedTile = intersectsView(event.getX(), event.getY())) == null) {
                        return false;
                    } else {
                        mSelectedTile.interruptThread();
                        mTouchOffsetX = event.getX() - mSelectedTile.getX();
                        mTouchOffsetY = event.getY() - mSelectedTile.getY();
                        setTileBounds();
                        return true;
                    }

                case MotionEvent.ACTION_MOVE:
                    mSelectedTile.moveTo(event.getX() - mTouchOffsetX, event.getY() - mTouchOffsetY);
                    requestLayout();
                    return true;

                case MotionEvent.ACTION_UP:
                    mSelectedTile.startThread();
                    updateState();
                    mCallback.stateChanged(mState);
                    return true;
                default:
            }
        }
        return false;
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

    public void updateState() {
        Tile tile;
        mState = new int[mState.length];
        for (int i = 0; i < getChildCount(); i++) {
            tile = (Tile) getChildAt(i);
            mState[tile.getRoundedXPos() + tile.getRoundedYPos() * mColumnCount] = tile.getValue();
        }
        searchForSolution();
    }

    private void searchForSolution() {

        mSolution = null;
        if (mSearchThread != null && mSearchThread.isAlive()) mSearchThread.interrupt();
        mSearchThread = new Thread(new IDAStar(mState.clone(), mColumnCount, mHeuristic,
                new IDAStar.Callback() {
                    @Override
                    public void solutionFound(int[] moves) {
                        mSolution = moves;
                        mCallback.solutionFound(moves);
                        Log.v("tag", "callback solution : " + Arrays.toString(moves));
                    }
                }));
        mSearchThread.start();
    }

    private void setTileBounds() {

        Rect bounds;
        int blankIndex = ArrayUtils.indexOf(mState, 0);
        int blankX = blankIndex % mColumnCount * mTileSize;
        int blankY = blankIndex / mColumnCount * mTileSize;
        int tileX = mSelectedTile.getRoundedXPos() * mTileSize;
        int tileY = mSelectedTile.getRoundedYPos() * mTileSize;

        /*
        It may appear wrong at first as this Rect is initialized as a point, not a rectangle.
        Bounds do not represent the bounds of the tile. It is the range that the top left corner
        of the tile can move. It will only ever be a horizontal or vertical line mTileSize long,
        but is easily represented by a Rect.
         */
        bounds = new Rect(
                tileX,
                tileY,
                tileX,
                tileY);

        if (blankY == tileY && blankX == tileX - mTileSize) {
            bounds.left = blankX;
        } else if (blankX == tileX && blankY == tileY - mTileSize) {
            bounds.top = blankY;
        } else if (blankY == tileY && blankX == tileX + mTileSize) {
            bounds.right = blankX;
        } else if (blankX == tileX && blankY == tileY + mTileSize) {
            bounds.bottom = blankY;
        }
        mSelectedTile.setBounds(bounds);
    }

    public void shuffleTiles() {
        (mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int blankIndex, blankX, blankY, tileX, tileY, direction;
                int lastDirection = -1;

                Random r = new Random();
                int count = (int) Math.pow(mState.length, 1.7);
                while (count > 0) {
                    blankIndex = ArrayUtils.indexOf(mState, 0);
                    blankX = tileX = blankIndex % mColumnCount;
                    blankY = tileY = blankIndex / mColumnCount;
                    direction = r.nextInt(4);
                    switch (direction) {
                        case 0:
                            if (blankX > 0 && lastDirection != 1) {
                                Log.v("tag", "direction: " + direction);
                                tileX = blankX - 1;
                                count--;
                                break;
                            }
                            continue;
                        case 1:
                            if (blankX < mColumnCount - 1 && lastDirection != 0) {
                                Log.v("tag", "direction: " + direction);
                                tileX = blankX + 1;
                                count--;
                                break;
                            }
                            continue;
                        case 2:
                            if (blankY > 0 && lastDirection != 3) {
                                Log.v("tag", "direction: " + direction);
                                tileY = blankY - 1;
                                count--;
                                break;
                            }
                            continue;
                        case 3:
                            if (blankY < (mState.length / mColumnCount) - 1 && lastDirection != 2) {
                                Log.v("tag", "direction: " + direction);
                                tileY = blankY + 1;
                                count--;
                                break;
                            }
                            continue;
                    }

                    lastDirection = direction;
                    for (int i = 0; i < getChildCount(); i++) {
                        mSelectedTile = (Tile) getChildAt(i);
                        while (mSelectedTile.isMoving()) {
                            // just wait
                        }
                        if (mSelectedTile.getRoundedXPos() == tileX &&
                                mSelectedTile.getRoundedYPos() == tileY) {
                            setTileBounds();
                            mSelectedTile.startThread(blankX, blankY);
                            mState[blankIndex] = mState[tileX + tileY * mColumnCount];
                            mState[tileX + tileY * mColumnCount] = 0;
                            break;
                        }
                    }
                }
                searchForSolution();
            }
        })).start();
    }

    public void solve() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
        (mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.v("tag", "solution?");
                if (mSolution != null) {
                    Log.v("tag", "we have a solution");
                    int blankIndex;
                    int blankX;
                    int blankY;
                    int tileIndex;

                    for (int move : mSolution) {
                        blankIndex = ArrayUtils.indexOf(mState, 0);
                        blankX = blankIndex % mColumnCount;
                        blankY = blankIndex / mColumnCount;
                        tileIndex = ArrayUtils.indexOf(mState, move);
                        for (int i = 0; i < getChildCount(); i++) {
                            mSelectedTile = (Tile) getChildAt(i);
                            if (mSelectedTile.getValue() == move) {
                                setTileBounds();
                                mSelectedTile.startThread(blankX, blankY);
                                mState[blankIndex] = mState[tileIndex];
                                mState[tileIndex] = 0;
                                while (mSelectedTile.isMoving()) {
                                    // just wait
                                }
                                break;
                            }
                        }
                    }
                    mSolution = null;
                }
            }
        })).start();
    }

    public void hint() {

    }

    public interface Callback {
        void stateChanged(int[] state);
        void solutionFound(int[] moves);
    }
}
