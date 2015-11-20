package com.meremammal.www.slidingtilepuzzle;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private PuzzleFrame mPuzzleFrame;

    // TODO mGoal and mColumnCount are temporary. They will be replaced by controls in the app.
    // User can control width and height of the board, and between a small set of goals.
    // To begin with, we'll stick to the standard goal setup.
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
                mPuzzleFrame.init(mGoal, mColumnCount, new TileArea.Callback() {

                    @Override
                    public void stateChanged(int[] state) {
                        // If Activity is in game mode, check for winner. Otherwise do nothing.
                    }
                });
            }
        });
    }

}
