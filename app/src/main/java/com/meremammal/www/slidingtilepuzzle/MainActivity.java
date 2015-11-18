package com.meremammal.www.slidingtilepuzzle;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private PuzzleFrame mPuzzleFrame;

    private int[] mGoal = {
            1, 2, 3, 4,
            5, 6, 7, 8,
            9,10,11,12,
            13,14,15,0
    };
    private int mColumnCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPuzzleFrame = (PuzzleFrame) findViewById(R.id.puzzle_frame);
        mPuzzleFrame.post(new Runnable() {
            @Override
            public void run() {
                mPuzzleFrame.init(mGoal, mColumnCount);
            }
        });
    }

}
