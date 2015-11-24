package com.meremammal.www.slidingtilepuzzle;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.meremammal.www.slidingtilepuzzle.search_algorithms.ManhattanWithLinearConflictHeuristic;

public class MainActivity extends Activity {

    /* TODO Add display for moves taken, minimum moves to goal.
      Perhaps add temporary TextView to display Heuristic estimated cost for testing purposes.
    */

    private PuzzleFrame mPuzzleFrame;
    private Spinner mSpinnerTilesWide;
    private Spinner mSpinnerTilesHigh;
    private Button mButtonHint;
    private Button mButtonSolve;
    private Button mButtonPlay;
    private boolean mGameInProgress = false;

    // TODO mGoal and mColumnCount are temporary. They will be replaced by controls in the app.
    // User can control width and height of the board, and between a small set of goals.
    // To begin with, we'll stick to the standard goal setup.
    private int[] mGoal = {
            1,2,3,
            4,5,6,
            7,8,0
    };
    private int mColumnCount = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPuzzleFrame = (PuzzleFrame) findViewById(R.id.puzzle_frame);
        mPuzzleFrame.post(new Runnable() {
            @Override
            public void run() {
                mPuzzleFrame.init(mGoal, mColumnCount, new ManhattanWithLinearConflictHeuristic(mColumnCount, mGoal), new TileArea.Callback() {

                    @Override
                    public void stateChanged(int[] state) {
                        // TODO If Activity is in game mode, check for winner. Otherwise do nothing.
                    }

                    @Override
                    public void solutionFound(int[] moves) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mButtonSolve.setVisibility(View.VISIBLE);
                                mButtonHint.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        });
        mSpinnerTilesWide = (Spinner) findViewById(R.id.spinner_tiles_wide);
        mSpinnerTilesHigh = (Spinner) findViewById(R.id.spinner_tiles_high);
        mButtonHint = (Button) findViewById(R.id.button_hint);
        mButtonSolve = (Button) findViewById(R.id.button_solve);
        mButtonPlay = (Button) findViewById(R.id.button_play);
    }

    public void hint(View view) {

        //if (mGameInProgress) {
            mPuzzleFrame.hint();
        //}
    }

    public void solve(View view) {

        //if (mGameInProgress) {
            mPuzzleFrame.solve();
        //}
        mButtonPlay.setVisibility(View.VISIBLE);
    }

    public void play(View view) {
        //mGameInProgress = true;
        mButtonSolve.setVisibility(View.INVISIBLE);
        mButtonHint.setVisibility(View.INVISIBLE);
        mButtonPlay.setVisibility(View.INVISIBLE);
        mPuzzleFrame.shuffleTiles();
    }
}
