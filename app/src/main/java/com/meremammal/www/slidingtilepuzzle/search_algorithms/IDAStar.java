package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by Fletcher on 15/10/2015.
 */
public class IDAStar implements Runnable {

    private static final int FOUND_SOLUTION = -1;
    private static final int NO_SOLUTION = Integer.MAX_VALUE;
    private static final int INTERRUPTED = -2;

    private enum DIRECTION {LEFT, RIGHT, UP, DOWN}
    private static DIRECTION[] DIRECTIONS = DIRECTION.values();

    private int[] mNode;
    private int mColumnCount;
    private int mDepth = 0;
    private int mBlankIndex;
    private int[] mMoves;
    private int mLimit;
    private long mStartTime;
    private Heuristic<int[]> mHeuristic;
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

        mStartTime = System.currentTimeMillis();
        mLimit = mHeuristic.getEstimatedCost(mNode);

        while (true) {
            Log.v("tag", "limit: " + mLimit);
            switch (mLimit = search(-1)) {
                case FOUND_SOLUTION:
                    mCallback.solutionFound(mMoves);
                    // Send mMoves via callback
                    return;
                case NO_SOLUTION:
                    // Send failure message
                    return;
                case INTERRUPTED:
                    // Send failure message if necessary
                    return;
                default:
            }
        }
    }

    private int search(int lastBlank) {

        /*
        First the cost is calculated and the node is checked for goal and limit.
        If the node is the goal (the heuristic must only return 0 for goal) create the move array
        and begin building from the last index to the first. Assign the last move to the last index.
        If the node cost exceeds the limit, return that cost.
        If the thread is interrupted, return INTERRUPTED
        */
        int cost = mDepth + mHeuristic.getEstimatedCost(mNode);
        if (cost - mDepth == 0) {
            Log.v("tag", "Solution found in " + (System.currentTimeMillis() - mStartTime) + "ms");
            Log.v("tag", "node: " + Arrays.toString(mNode));
            Log.v("tag", "depth: " + mDepth);
            Log.v("tag", "cost: " + mHeuristic.getEstimatedCost(mNode) + ", total: " + cost);
            mMoves = new int[mDepth];
            if (mDepth > 0) {
                mMoves[mDepth - 1] = mNode[lastBlank];
            }
            return FOUND_SOLUTION;
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

         */
        int move;
        for (DIRECTION direction : DIRECTIONS) {
            move = getPossibleMoves(lastBlank, direction);
            if (move != -1) {
                progressNode(move);
                switch (cost = Math.min(cost, search(currentBlank))) {
                    case FOUND_SOLUTION:
                        mDepth--;
                        if (mDepth > 0) {
                            revertNode(currentBlank, move);
                            mMoves[mDepth - 1] = mNode[lastBlank];
                        }
                        String nodeString = "";
                        for (int i = 0; i < mNode.length; i++) {
                            if (i % mColumnCount == 0) nodeString = nodeString.concat("\n");
                            nodeString = nodeString.concat(mNode[i] + ", ");
                        }
                        Log.v("tag", "node: " + nodeString);
                        Log.v("tag", "depth: " + mDepth);
                        Log.v("tag", "cost: " + mHeuristic.getEstimatedCost(mNode) + ", total: " + (mDepth + mHeuristic.getEstimatedCost(mNode)));
                        return FOUND_SOLUTION;
                    case INTERRUPTED:
                        return INTERRUPTED;
                    default:
                }
                revertNode(currentBlank, move);
            }
        }
        mDepth--;
        return cost;
    }

    private int getPossibleMoves(int lastBlank, DIRECTION direction) {

        switch (direction) {
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
