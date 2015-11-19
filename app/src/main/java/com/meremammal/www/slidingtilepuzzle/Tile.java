package com.meremammal.www.slidingtilepuzzle;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Fletcher on 18/11/2015.
 */
public class Tile extends ImageView {

    private int mValue;
    private float mXPos;
    private float mYPos;

    public Tile(Context context, float XPos, float YPos, int value) {
        super(context);
        mValue = value;
        mYPos = YPos;
        mXPos = XPos;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public float getXPos() {
        return mXPos;
    }

    public void setXPos(float XPos) {
        mXPos = XPos;
    }

    public float getYPos() {
        return mYPos;
    }

    public void setYPos(float YPos) {
        mYPos = YPos;
    }

    public int getRoundedXPos() {
        return Math.round(mXPos);
    }

    public int getRoundedYPos() {
        return Math.round(mYPos);
    }
}
