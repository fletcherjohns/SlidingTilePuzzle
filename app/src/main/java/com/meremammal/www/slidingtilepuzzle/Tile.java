package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class Tile extends ImageView {

    private int mXPos;
    private int mYPos;

    public Tile(Context context, int xPos, int yPos) {
        super(context);
        mXPos = xPos;
        mYPos = yPos;
    }

    public int getXPos() {
        return mXPos;
    }

    public void setXPos(int XPos) {
        mXPos = XPos;
    }

    public int getYPos() {
        return mYPos;
    }

    public void setYPos(int YPos) {
        mYPos = YPos;
    }

    public int getRoundedXPos() {
        return Math.round(mXPos);
    }

    public int getRoundexYPos() {
        return Math.round(mYPos);
    }
}
