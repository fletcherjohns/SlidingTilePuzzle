package com.meremammal.www.slidingtilepuzzle;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.meremammal.www.slidingtilepuzzle.search_algorithms.ManhattanWithLinearConflictHeuristic;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

public class MainActivity extends Activity {

    /* TODO Add display for moves taken, minimum moves to goal.
      Perhaps add temporary TextView to display Heuristic estimated cost for testing purposes.
    */

    private TileArea mTiles;
    private TextView mTextTime;
    private TextView mTextMoves;
    private TextView mTextScore;
    private ImageView mButtonHint;
    private ImageView mButtonGiveUp;
    private ImageView mButtonPlay;

    private Thread mGameTimer;
    private GameState mGameState;

    // TODO mGoal and mColumnCount are temporary. They will be replaced by controls in the app.
    // User can control width and height of the board, and between a small set of goals.
    // To begin with, we'll stick to the standard goal setup.
    private int[] mGoal = {
            1, 2, 3,
            4, 5, 6,
            7, 8, 0
    };
    private int mColumnCount = 3;
    public static final long DURATION = 200;
    private FrameLayout mButtonFrame;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mGameState = new GameState();
        }
        mButtonFrame = (FrameLayout) findViewById(R.id.button_layout);
        final PuzzleFrame puzzleFrame = (PuzzleFrame) findViewById(R.id.puzzle_frame);
        puzzleFrame.post(new Runnable() {
            @Override
            public void run() {
                puzzleFrame.init(mGoal.clone(), mColumnCount,
                        new ManhattanWithLinearConflictHeuristic(mColumnCount, mGoal),
                        new TileArea.Callback() {

                    @Override
                    public void doneShuffle(int[] state) {

                        mGameState = new GameState(System.currentTimeMillis());
                        if (mGameTimer != null && mGameTimer.isAlive()) mGameTimer.interrupt();
                        mGameTimer = new Thread(new TimerRunnable(mGameState.startTime));
                        mGameTimer.start();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextMoves.setText(String.valueOf(mGameState.moveCount));
                                mButtonHint.setAlpha(0.5f);
                                mButtonGiveUp.setAlpha(0.5f);

                                animatorHintIn().start();
                                animatorGiveUpIn().start();
                            }
                        });
                    }

                    @Override
                    public void stateChanged(final int[] state) {
                        mGameState.moveCount++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextMoves.setText(String.valueOf(mGameState.moveCount));
                                mButtonHint.setAlpha(0.5f);
                                mButtonGiveUp.setAlpha(0.5f);

                                if (Arrays.equals(state, mGoal)) {
                                    animatorPlayIn().start();
                                    animatorHintOut().start();
                                    animatorGiveUpOut().start();
                                }
                            }
                        });
                    }

                    @Override
                    public void solutionsFound(final int[][] moves) {

                        if (mGameState.initialOptimalMovesToGoal == -1) {
                            mGameState.initialOptimalMovesToGoal = moves[0].length;
                        }
                        mGameState.currentOptimalMovesToGoal = moves[0].length;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                calculateAndDisplayScore();
                                if (moves.length > 0 && moves[0].length > 0) {
                                    mButtonHint.setAlpha(1f);
                                    mButtonGiveUp.setAlpha(1f);
                                } else {
                                    // if game in progress, show win dialog
                                    if (mGameTimer != null && mGameTimer.isAlive()) {
                                        mGameTimer.interrupt();
                                        mGameState = new GameState();
                                        showWinningDialog();
                                    }
                                }
                            }
                        });
                    }
                });
                mTiles = puzzleFrame.getTileArea();

                int layoutWidth = mButtonFrame.getWidth();

                if (savedInstanceState == null) {
                    mButtonPlay.setX(layoutWidth / 2f - mButtonPlay.getWidth() / 2f);
                    mButtonHint.setX(-2 * mButtonHint.getWidth());
                    mButtonGiveUp.setX(-mButtonGiveUp.getWidth());
                }

                if (mGameState.startTime != -1) {
                    mButtonPlay.setX(layoutWidth);
                    mButtonHint.setX(layoutWidth / 2f - mButtonHint.getWidth());
                    mButtonGiveUp.setX(layoutWidth / 2f);
                } else {
                    mButtonPlay.setX(layoutWidth / 2f - mButtonPlay.getWidth() / 2f);
                    mButtonHint.setX(-2 * mButtonHint.getWidth());
                    mButtonGiveUp.setX(-mButtonGiveUp.getWidth());
                }
            }

        });

        mTextTime = (TextView) findViewById(R.id.text_time);
        mTextMoves = (TextView) findViewById(R.id.text_moves);
        mTextScore = (TextView) findViewById(R.id.text_score);

        mButtonHint = (ImageView) findViewById(R.id.button_hint);
        mButtonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hint();
            }
        });

        mButtonGiveUp = (ImageView) findViewById(R.id.button_solve);
        mButtonGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveUp();
            }
        });

        mButtonPlay = (ImageView) findViewById(R.id.button_play);
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mGameState = new GameState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameState.startTime != -1) {
            mGameState.unPause();
            mTextMoves.setText(String.valueOf(mGameState.moveCount));

            calculateAndDisplayScore();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameState.pauseTime = System.currentTimeMillis();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGameState.putInBundle(outState);
        if (mGameTimer != null && mGameTimer.isAlive()) {
            mGameTimer.interrupt();
        }
    }

    private void calculateAndDisplayScore() {
        // Score won't drop below 0.
        // You get 50 points for each move
        // This is multiplied by the ratio of (initial optimal moves / (moves + current optimal moves))
        // Subtract 25 points for each hint and each move over optimal.
        int score = (int) (Math.max(0,

                50. * mGameState.moveCount

                        * mGameState.initialOptimalMovesToGoal
                        / (mGameState.currentOptimalMovesToGoal + mGameState.moveCount)

                        - 25. * mGameState.hintCount

                        - 25. * (Math.max(0, mGameState.moveCount - mGameState.initialOptimalMovesToGoal)))
        );

        mTextScore.setText(String.valueOf(score));
        Log.v("tag", "update score: " + score);
    }

    public void hint() {

        mGameState.hintCount++;
        calculateAndDisplayScore();
        mTiles.hint();
    }

    public void giveUp() {
        if (mGameTimer != null && mGameTimer.isAlive()) {
            mGameTimer.interrupt();
        }
        if (mTiles.solve()) {
            mGameState = new GameState();
            mButtonHint.setAlpha(0.5f);
            mButtonGiveUp.setAlpha(0.5f);
        }
    }

    public void play() {
        if (mGameTimer != null && mGameTimer.isAlive()) {
            mGameTimer.interrupt();
        }
        mTiles.shuffleTiles();
        animatorPlayOut().start();
    }

    private void showWinningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You scored " + mTextScore.getText().toString());
        builder.setTitle("Winner!!!");
        builder.create().show();
    }

    private ObjectAnimator animatorHintIn() {
        int layoutWidth = mButtonFrame.getWidth();

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonHint, "x", layoutWidth / 2f - mButtonHint.getWidth());
        animator.setDuration(DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        return animator;
    }

    private ObjectAnimator animatorHintOut() {

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonHint, "x", -2f * mButtonHint.getWidth());
        animator.setDuration(DURATION);
        animator.setInterpolator(new AccelerateInterpolator());

        return animator;
    }

    private ObjectAnimator animatorGiveUpIn() {
        int layoutWidth = mButtonFrame.getWidth();

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonGiveUp, "x", layoutWidth / 2f);
        animator.setDuration(DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        return animator;
    }

    private ObjectAnimator animatorGiveUpOut() {

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonGiveUp, "x", -mButtonGiveUp.getWidth());
        animator.setDuration(DURATION);
        animator.setInterpolator(new AccelerateInterpolator());

        return animator;
    }

    private ObjectAnimator animatorPlayIn() {
        int layoutWidth = mButtonFrame.getWidth();

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonPlay, "x", layoutWidth / 2f - mButtonPlay.getWidth() / 2f);
        animator.setDuration(DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        return animator;
    }

    private ObjectAnimator animatorPlayOut() {
        int layoutWidth = mButtonFrame.getWidth();

        ObjectAnimator animator;
        animator = ObjectAnimator.ofFloat(mButtonPlay, "x", layoutWidth);
        animator.setDuration(DURATION);
        animator.setInterpolator(new AccelerateInterpolator());

        return animator;
    }

    private class GameState {
        private static final String START_TIME = "start_time";
        private static final String PAUSE_TIME = "pause_time";
        private static final String INITIAL_OPTIMAL_MOVES_TO_GOAL = "initial_optimal_moves_to_goal";
        private static final String MOVE_COUNT = "move_count";
        private static final String CURRENT_OPTIMAL_MOVES_TO_GOAL = "current_optimal_moves_to_goal";
        private static final String HINT_COUNT = "hint_count";

        long startTime;
        long pauseTime;
        int initialOptimalMovesToGoal;
        int currentOptimalMovesToGoal;
        int moveCount;
        int hintCount;

        /**
         * This empty constructor sets an invalid (idle) state
         */
        public GameState() {
            this(-1);
        }

        public GameState(long startTime) {
            this(startTime, -1, -1, -1, 0, 0);
        }

        public GameState(long startTime, int pauseTime, int initialOptimalMovesToGoal, int currentOptimalMovesToGoal, int moveCount, int hintCount) {
            this.startTime = startTime;
            this.pauseTime = pauseTime;
            this.initialOptimalMovesToGoal = initialOptimalMovesToGoal;
            this.currentOptimalMovesToGoal = currentOptimalMovesToGoal;
            this.moveCount = moveCount;
            this.hintCount = hintCount;
        }

        public GameState(Bundle bundle) {
            startTime = bundle.getLong(START_TIME);
            pauseTime = bundle.getLong(PAUSE_TIME);
            initialOptimalMovesToGoal = bundle.getInt(INITIAL_OPTIMAL_MOVES_TO_GOAL);
            currentOptimalMovesToGoal = bundle.getInt(CURRENT_OPTIMAL_MOVES_TO_GOAL);
            moveCount = bundle.getInt(MOVE_COUNT);
            hintCount = bundle.getInt(HINT_COUNT);
        }

        public void putInBundle(Bundle bundle) {
            bundle.putLong(START_TIME, startTime);
            bundle.putLong(PAUSE_TIME, pauseTime);
            Log.v("tag", "START_TIME = " + startTime);
            Log.v("tag", "PAUSE_TIME = " + bundle.getLong(PAUSE_TIME));
            bundle.putInt(INITIAL_OPTIMAL_MOVES_TO_GOAL, initialOptimalMovesToGoal);
            bundle.putInt(CURRENT_OPTIMAL_MOVES_TO_GOAL, currentOptimalMovesToGoal);
            bundle.putInt(MOVE_COUNT, moveCount);
            bundle.putInt(HINT_COUNT, hintCount);
        }

        public void unPause() {
            if (pauseTime != -1) {
                startTime += (System.currentTimeMillis() - pauseTime);
                pauseTime = -1;
                if (mGameTimer != null && mGameTimer.isAlive()) {
                    mGameTimer.interrupt();
                }
                mGameTimer = new Thread(new TimerRunnable(startTime));
                mGameTimer.start();
            }
        }
    }

    private class TimerRunnable implements Runnable {

        private long mStartTime = System.currentTimeMillis();
        private NumberFormat format = new DecimalFormat("00");

        public TimerRunnable(long startTime) {
            mStartTime = startTime;
        }

        @Override
        public void run() {
            long elapsedSecs;
            while (true) {
                elapsedSecs = (System.currentTimeMillis() - mStartTime) / 1000;
                final String displayTime = format.format(elapsedSecs / 60) +
                        ":" + format.format(elapsedSecs % 60);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextTime.setText(displayTime);
                    }
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
