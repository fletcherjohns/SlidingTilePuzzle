package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class PuzzleFrame extends FrameLayout {

    private TileArea mTileArea;

    public PuzzleFrame(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void init(int[] goal, int columnCount, TileArea.Callback callback) {
        int size = Math.min(
                // Want total board size to be a multiple of columnCount
                // could also do: getWidth() - (getWidth() % columnCount)
                getWidth() / columnCount * columnCount,
                getHeight() / columnCount * columnCount
        );
        mTileArea = new TileArea(getContext(), goal, columnCount);
        mTileArea.setCallback(callback);
        FrameLayout.LayoutParams params = new LayoutParams(
                size,
                size
        );
        params.gravity = Gravity.CENTER;
        mTileArea.setLayoutParams(params);
        addView(mTileArea);
        invalidate();
    }
}
