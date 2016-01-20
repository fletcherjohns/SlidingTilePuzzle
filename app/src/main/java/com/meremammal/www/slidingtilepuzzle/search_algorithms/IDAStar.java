package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by Fletcher on 15/10/2015.
 *
 */
public class IDAStar implements Runnable {

    private static final int FOUND_SOLUTION = -1;
    private static final int NO_SOLUTION = Integer.MAX_VALUE;
    private static final int INTERRUPTED = -2;

    private enum DIRECTION {LEFT, RIGHT, UP, DOWN}

    private static DIRECTION[] DIRECTIONS = DIRECTION.values();

    public static int sLimitCap = Integer.MAX_VALUE;

    /**
     * This array is initialised in the constructor and modified during the recursive process.
     * The only other array created is mMoves which is only created once when a solution is found.
     */
    private int[] mNode;
    private int mColumnCount;
    /**
     * This int counts the depth of the search tree and represents the move count.
     */
    private int mDepth = 0;
    /**
     * This int keeps track of the blank space to calculate possible moves.
     */
    private int mBlankIndex;
    /**
     * This array is initialised to length mDepth when a solution is found and is progressively
     * filled from end to start as the recursive search returns to the root.
     */
    private int[] mMoves;
    /**
     * This int is the heuristic cost limit for each iteration of the search. After each iteration
     * it is set to the cheapest cost of all nodes exceeding the current limit.
     */
    private int mLimit;
    private long mStartTime;
    /**
     * This Heuristic is used to calculate the cost of each node during the search.
     */
    private Heuristic<int[]> mHeuristic;
    /**
     * All results are sent via this callback. Note it may be called from a background thread.
     */
    private Callback mCallback;

    public IDAStar(int[] node, int columnCount, Heuristic<int[]> heuristic, Callback callback) {
        mNode = node;
        mBlankIndex = ArrayUtils.indexOf(mNode, 0);

        mColumnCount = columnCount;
        mHeuristic = heuristic;
        mCallback = callback;
    }

    @Override
    public void run() {
        // This is where the search process begins.
        // Set start time.
        mStartTime = System.currentTimeMillis();
        // Set mLimit to the cost of the root node.
        mLimit = mHeuristic.getEstimatedCost(mNode);
        // Main loop. Update mLimit each iteration until FOUND_SOLUTION, NO_SOLUTION or INTERRUPTED.
        // Any positive int < MAX_VALUE will be accepted as new limit.
        while (true) {
            //Log.v("tag", "limit: " + mLimit);
            mMoves = new int[mLimit];
            // Just set mLimit to whatever search(-1) returns, then run it through a switch block.
            switch (mLimit = search(-1)) {
                case FOUND_SOLUTION:
                    // We don't need to continue. Send solution via callback.
                    //Log.v("tag", "moves: " + Arrays.toString(mMoves));
                    mCallback.solutionFound(mMoves);
                    return;
                case NO_SOLUTION:
                    // Send failure message
                    mCallback.solutionFound(null);
                    return;
                case INTERRUPTED:
                    // Send failure message if necessary
                    mCallback.solutionFound(null);
                    return;
                default:
                    // Lowest failed node cost returned. mLimit already updated, continue loop.
            }
        }
    }

    private int search(int lastBlank) {

        if (mLimit > sLimitCap) return INTERRUPTED;

        /*
        First the cost is calculated and the node is checked for goal and limit.
        If the node is the goal (the heuristic must only return 0 for goal) create the move array
        and begin building from the last index to the first. Assign the last move to the last index.

        If the node cost exceeds the limit, return that cost.

        If the thread is interrupted, return INTERRUPTED
        */
        // cost = moves + estimated moves left
        int cost = mDepth + mHeuristic.getEstimatedCost(mNode);
        // subtract moves back off that and check for goal.
        if (cost - mDepth == 0) {
            Log.v("tag", "Solution found in " + (System.currentTimeMillis() - mStartTime) + "ms");
            mCallback.solutionFound(mMoves.clone());
            sLimitCap = Math.min(cost, sLimitCap);
            return Integer.MAX_VALUE;
        } else if (cost > mLimit) {
            return cost;
        }
        if (Thread.interrupted()) return INTERRUPTED;

        /*
        If the node can be explored further, recursion is used with no parameters other than the
        previous blank index to allow backtracking. Most variables are fields to minimise object
        creation. There is no repetitive creation of arrays.

        To prepare for the progression to child nodes which will be compared, store the current
        blank index, set cost to MAX_VALUE for use with Math.min() to calculate cheapest cost
        returned, and increment the depth.
         */
        int currentBlank = mBlankIndex;
        cost = NO_SOLUTION;
        mDepth++;
        /*
        Iterate over DIRECTION enum to check for all valid moves.
         */
        int move;
        for (DIRECTION direction : DIRECTIONS) {
            // for each direction check if move is valid.
            move = getPossibleMoves(lastBlank, direction);
            // returns -1 if that move is off the board or if it would undo last move.
            if (move != -1) {
                mMoves[mDepth - 1] = mNode[move];
                // convenience method to swap relevant tile with blank
                progressNode(move);
                if (mDepth > 0) {
                }
                // Here's where the recursion begins/continues.
                // Calling search() will search this new node for acceptable children, and will
                // return lowest failed node cost or other constant.
                switch (cost = Math.min(cost, search(currentBlank))) {
                    case INTERRUPTED:
                        return INTERRUPTED;
                    default:
                }
                // If no solution or problem, revertNode and continue searching different directions.
                revertNode(currentBlank, move);
            }
        }
        // After all directions are searched and still no solution, decrement mDepth and return
        // cost. This steps back up one level in the recursion.
        mDepth--;
        return cost;
    }

    private int getPossibleMoves(int lastBlank, DIRECTION direction) {

        // Depending on the direction passed in, make sure there is a tile on that side of the
        // blank (not against the side of board) and that that tile is not the last tile moved.
        // If it is sitting in the spot the blank was last, it is the last tile moved.
        switch (direction) {
        /*
        All of these branches have the following structure:

        if (the blank is not in the far left column
                AND the space to the left of it isn't the last blank) {
            return index to left of blank;
        } else {
            return -1 which is invalid move;
        }
        */
            case LEFT:
                return mBlankIndex % mColumnCount > 0 &&
                        mBlankIndex - 1 != lastBlank ?
                        mBlankIndex - 1 : -1;
            case RIGHT:
                return mBlankIndex % mColumnCount < mColumnCount - 1 &&
                        mBlankIndex + 1 != lastBlank ?
                        mBlankIndex + 1 : -1;
            case UP:
                return mBlankIndex / mColumnCount > 0 &&
                        mBlankIndex - mColumnCount != lastBlank ?
                        mBlankIndex - mColumnCount : -1;
            case DOWN:
                return mBlankIndex / mColumnCount < mNode.length / mColumnCount - 1 &&
                        mBlankIndex + mColumnCount != lastBlank ?
                        mBlankIndex + mColumnCount : -1;
            default:
                return -1;
        }
    }

    private void progressNode(int move) {

        mNode[mBlankIndex] = mNode[move];
        mNode[move] = 0;
        mBlankIndex = move;
    }

    private void revertNode(int originalBlank, int move) {
        mBlankIndex = originalBlank;
        mNode[move] = mNode[mBlankIndex];
        mNode[mBlankIndex] = 0;
    }

    public interface Callback {
        void solutionFound(int[] moves);
    }

}
