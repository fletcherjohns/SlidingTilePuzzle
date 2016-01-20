package com.meremammal.www.slidingtilepuzzle;

import android.graphics.Bitmap;

/**
 * Created by Fletcher on 20/01/2016.
 */
public interface TileAdapter {

    Bitmap getBitmap(int tileSize, int value, int columnCount, int rowCount);
}
