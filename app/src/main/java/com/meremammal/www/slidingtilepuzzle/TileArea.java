package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class TileArea extends ViewGroup {

    private int[] mGoal;
    private int mColumnCount;
    private int mTileSize;
    private Paint mPaint;

    public TileArea(Context context, int[] goal, int columnCount) {
        super(context);
        mGoal = goal;
        mColumnCount = columnCount;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        post(new Runnable() {
            @Override
            public void run() {

                mTileSize = getWidth() / mColumnCount;
                Log.v("tag", "tileSize = " + mTileSize);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mTileSize, mTileSize);
                Tile tile;
                for (int i = 0; i < mGoal.length; i++) {
                    if (mGoal[i] == 0) continue;
                    tile = new Tile(getContext(), i % mColumnCount, i / mColumnCount);
                    tile.setLayoutParams(params);
                    tile.setImageBitmap(getBitmap(mGoal[i]));
                    //tile.setDrawingCacheEnabled(true);
                    addView(tile);
                }
                invalidate();
            }
        });
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
            left = tile.getXPos() * mTileSize;
            top = tile.getYPos() * mTileSize;
            tile.layout(
                    left,
                    top,
                    left + mTileSize,
                    top + mTileSize
            );
        }
        invalidate();
    }
}
