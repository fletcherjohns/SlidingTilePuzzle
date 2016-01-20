package com.meremammal.www.slidingtilepuzzle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Fletcher on 20/01/2016.
 */
public class BasicTileAdapter implements TileAdapter {

    @Override
    public Bitmap getBitmap(int tileSize, int value, int columnCount, int rowCount) {
        Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(tileSize * 0.8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        Rect bounds = new Rect();
        String text = String.valueOf(value);
        // use text bounds to calculate the y position of the text.
        paint.getTextBounds(text, 0, text.length(), bounds);
        // measureText gives a better result for x position of text.
        float textWidth = paint.measureText(text);

        canvas.drawRect(1, 1, tileSize - 1, tileSize - 1, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, tileSize / 2f - textWidth / 2f, tileSize / 2f + bounds.height() / 2f, paint);
        return bitmap;
    }
}
