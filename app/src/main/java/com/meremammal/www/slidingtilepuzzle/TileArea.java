package com.meremammal.www.slidingtilepuzzle;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
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
import com.meremammal.www.slidingtilepuzzle.search_algorithms.MultithreadIDAStar;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Fletcher on 18/11/2015.
 *
 * <p>It utilizes a {@link com.meremammal.www.slidingtilepuzzle.search_algorithms.Heuristic} to
 * giveUp the puzzle. </p>
 */
public class TileArea extends ViewGroup {

    private static final String SUPER_PARCELABLE = "super_parcelable";
    private static final String CURRENT_STATE = "current_state";
    private static final String CURRENT_SOLUTION = "current_solution";

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
    private MultithreadIDAStar mMultithreadIDAStar;
    private int[][] mSolutions;

    private TileAdapter mTileAdapter = new IvoryTileAdapter();

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
                        tile.setImageBitmap(mTileAdapter.getBitmap(mTileSize, mState[i], mColumnCount, mState.length / mColumnCount));
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
        bundle.putSerializable(CURRENT_SOLUTION, mSolutions);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null) return;
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable(SUPER_PARCELABLE);
        mState = bundle.getIntArray(CURRENT_STATE);
        mSolutions = (int[][]) bundle.getSerializable(CURRENT_SOLUTION);
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
                    if (updateState()) {
                        searchForSolution();
                        mCallback.stateChanged(mState);
                    }
                mSelectedTile.startThread();
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

    public boolean updateState() {
        boolean changed = false;
        Tile tile;
        int tilePosition;
        boolean[] positionsFilled = new boolean[mState.length];
        for (int i = 0; i < getChildCount(); i++) {
            tile = (Tile) getChildAt(i);
            tilePosition = tile.getRoundedXPos() + tile.getRoundedYPos() * mColumnCount;
            positionsFilled[tilePosition] = true;
            if (mState[tilePosition] != tile.getValue()) {
                mState[tilePosition] = tile.getValue();
                changed = true;
            }
        }
        mState[ArrayUtils.indexOf(positionsFilled, false)] = 0;
        return changed;
    }

    private void searchForSolution() {

        mSolutions = null;
        if (mMultithreadIDAStar != null) mMultithreadIDAStar.stop();
        mMultithreadIDAStar = new MultithreadIDAStar(mState.clone(), mColumnCount, mHeuristic,
                new MultithreadIDAStar.Callback() {
                    @Override
                    public void solutionsFound(int[][] moves) {
                        mSolutions = moves;
                        mCallback.solutionsFound(moves);
                        mMultithreadIDAStar = null;
                        Log.v("tag", "callback solution : " + Arrays.deepToString(moves));
                    }
                });
        mMultithreadIDAStar.start();
    }

    private ObjectAnimator getHintAnimator(Tile tile) {
        ObjectAnimator animator;
        animator = ObjectAnimator.ofInt(tile, "alpha", 255, 0, 255);
        animator.setDuration(200);
        animator.setEvaluator(new ArgbEvaluator());

        return animator;
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
        if (mThread != null && mThread.isAlive()) {
            return;
        }
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
                mCallback.doneShuffle(mState);
                searchForSolution();
            }
        })).start();
    }

    public boolean solve() {
        if ((mThread != null && mThread.isAlive()) || mSolutions == null) {
            return false;
        }
        (mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.v("tag", "solution?");
                if (mSolutions != null) {
                    Log.v("tag", "we have a solution");
                    int blankIndex;
                    int blankX;
                    int blankY;
                    int tileIndex;

                    int[] solution = mSolutions[0];
                    for (int move : solution) {
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
                    mSolutions = null;
                    mCallback.stateChanged(mState);
                }
            }
        })).start();
        return true;
    }

    public boolean hint() {
        if (mSolutions == null) return false;

        Tile hintTile;
        for (int i = 0; i < getChildCount(); i++) {
            for (int[] solution : mSolutions) {
                if ((hintTile = (Tile) getChildAt(i)).getValue() == solution[0]) {
                    getHintAnimator(hintTile).start();
                }
            }
        }
        return true;
    }

    public interface Callback {
        void doneShuffle(int[] state);
        void stateChanged(int[] state);
        void solutionsFound(int[][] moves);
    }
}
