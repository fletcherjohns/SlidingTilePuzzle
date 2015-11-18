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

    public void init(int[] goal, int columnCount) {
        int size = Math.min(
                getWidth() / columnCount * columnCount,
                getHeight() / columnCount * columnCount
        );
        mTileArea = new TileArea(getContext(), goal, columnCount);
        FrameLayout.LayoutParams params = new LayoutParams(
                size,
                size
        );
        params.gravity = Gravity.CENTER;
        mTileArea.setLayoutParams(params);
        mTileArea.setBackgroundColor(Color.BLUE);
        addView(mTileArea);
        invalidate();
    }
}
