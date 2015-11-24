package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.meremammal.www.slidingtilepuzzle.search_algorithms.Heuristic;

/**
 * Created by Fletcher on 18/11/2015.
 * <p>This class acts as a wrapper for {@link TileArea}. It fills a space provided and calculates the
 * maximum size the TileArea can be and centres it.</p>
 * <p>It also provides methods to manage the TileArea.</p>
*/
public class PuzzleFrame extends FrameLayout {

    private static final String SUPER_PARCELABLE = "super_parcelable";
    private static final String TILE_AREA_PARCELABLE = "tile_area_parcelable";

    private TileArea mTileArea;
    private Parcelable mTileAreaInstanceState;

    public PuzzleFrame(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void init(int[] goal, int columnCount, Heuristic<int[]> heuristic, TileArea.Callback callback) {
        int rowCount = goal.length / columnCount;
        int size = Math.min(
                // Want total board size to be a multiple of columnCount
                // could also do: getWidth() - (getWidth() % columnCount)
                getWidth() / columnCount * columnCount,
                getHeight() / rowCount * rowCount
        );
        mTileArea = new TileArea(getContext(), goal, columnCount, heuristic);
        mTileArea.setCallback(callback);
        FrameLayout.LayoutParams params = new LayoutParams(
                size,
                size
        );
        params.gravity = Gravity.CENTER;
        mTileArea.setLayoutParams(params);
        addView(mTileArea);
        mTileArea.onRestoreInstanceState(mTileAreaInstanceState);
        invalidate();
    }

    public void shuffleTiles() {
        mTileArea.shuffleTiles();
    }

    public void solve() {
        mTileArea.solve();
    }

    public void hint() {
        mTileArea.hint();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.v("tag", "Puzzle save");
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_PARCELABLE, super.onSaveInstanceState());
        bundle.putParcelable(TILE_AREA_PARCELABLE, mTileArea.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.v("tag", "Puzzle restore");
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable(SUPER_PARCELABLE);
        mTileAreaInstanceState = bundle.getParcelable(TILE_AREA_PARCELABLE);
        super.onRestoreInstanceState(state);
    }
}
