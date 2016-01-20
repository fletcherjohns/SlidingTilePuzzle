package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Fletcher on 3/12/2015.
 */
public class MultithreadIDAStar {

    private List<NodeSeries> mNodeSeriesList;
    private int mRemainingNodeSeries;
    private int mColumnCount;
    private Heuristic<int[]> mHeuristic;
    private List<int[]> mSolutions;
    private Callback mCallback;
    private Thread[] mThreads;

    public MultithreadIDAStar(int[] node, int columnCount, Heuristic<int[]> heuristic, Callback callback) {
        mNodeSeriesList = new ArrayList<>();
        mNodeSeriesList.add(new NodeSeries(node, new int[0]));
        mColumnCount = columnCount;
        mHeuristic = heuristic;
        mCallback = callback;
        mSolutions = new ArrayList<>();
    }

    public void start() {

        NodeSeries node;
        int blankIndex;
        int lastMove;
        int[] child;
        List<NodeSeries> children;
        for (int i = 0; i < 1; i++) {
            children = new ArrayList<>();
            while (mNodeSeriesList.size() > 0) {
                node = mNodeSeriesList.remove(mNodeSeriesList.size() - 1);
                if (mHeuristic.getEstimatedCost(node.getNode()) == 0) {
                    children.add(node);
                    continue;
                }
                blankIndex = ArrayUtils.indexOf(node.getNode(), 0);
                if (node.getMoves().length > 0) {
                    lastMove = node.getMoves()[node.getMoves().length - 1];
                } else {
                    lastMove = -1;
                }
                if (blankIndex % mColumnCount > 0 &&
                        node.getNode()[blankIndex - 1] != lastMove) {

                    child = node.getNode().clone();
                    child[blankIndex] = node.getNode()[blankIndex - 1];
                    child[blankIndex - 1] = 0;
                    children.add(new NodeSeries(child, ArrayUtils.add(node.getMoves(), child[blankIndex])));
                }
                if (blankIndex % mColumnCount < mColumnCount - 1 &&
                        node.getNode()[blankIndex + 1] != lastMove) {

                    child = node.getNode().clone();
                    child[blankIndex] = node.getNode()[blankIndex + 1];
                    child[blankIndex + 1] = 0;
                    children.add(new NodeSeries(child, ArrayUtils.add(node.getMoves(), child[blankIndex])));
                }
                if (blankIndex / mColumnCount > 0 &&
                        node.getNode()[blankIndex - mColumnCount] != lastMove) {

                    child = node.getNode().clone();
                    child[blankIndex] = node.getNode()[blankIndex - mColumnCount];
                    child[blankIndex - mColumnCount] = 0;
                    children.add(new NodeSeries(child, ArrayUtils.add(node.getMoves(), child[blankIndex])));
                }
                if (blankIndex / mColumnCount < node.getNode().length / mColumnCount - 1 &&
                        node.getNode()[blankIndex + mColumnCount] != lastMove) {

                    child = node.getNode().clone();
                    child[blankIndex] = node.getNode()[blankIndex + mColumnCount];
                    child[blankIndex + mColumnCount] = 0;
                    children.add(new NodeSeries(child, ArrayUtils.add(node.getMoves(), child[blankIndex])));
                }
            }
            mNodeSeriesList = children;
        }
        mRemainingNodeSeries = mNodeSeriesList.size();
        mThreads = new Thread[mRemainingNodeSeries];
        NodeSeries nodeSeries;
        IDAStar.sLimitCap = Integer.MAX_VALUE;
        for (int i = 0; i < mRemainingNodeSeries; i++) {
            nodeSeries = mNodeSeriesList.get(i);
            (mThreads[i] = new Thread(new IDAStar(nodeSeries.getNode(), mColumnCount, mHeuristic,
                    new IDAStarCallback(nodeSeries.getMoves())))).start();

        }
    }

    public void stop() {
        for (Thread thread : mThreads) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    private class IDAStarCallback implements IDAStar.Callback {

        private int[] mPreMoves;

        public IDAStarCallback(int[] preMoves) {
            mPreMoves = preMoves;
        }

        @Override
        public synchronized void solutionFound(int[] moves) {

            if (moves == null) {
                mRemainingNodeSeries--;
            } else {
                mSolutions.add(ArrayUtils.addAll(mPreMoves, moves));
            }
            if (mRemainingNodeSeries == 0) {
                int moveCount = Integer.MAX_VALUE;
                int[][] bestSolutions = new int[0][];
                for (int[] solution : mSolutions) {
                    if (solution.length < moveCount) {
                        moveCount = solution.length;
                        bestSolutions = new int[][]{solution};
                    } else if (solution.length == moveCount) {
                        bestSolutions = ArrayUtils.add(bestSolutions, solution);
                    }
                }
                mCallback.solutionsFound(bestSolutions);
            }
        }
    }

    public interface Callback {
        void solutionsFound(int[][] solutions);
    }

    private class NodeSeries {
        private int[] mNode;
        private int[] mMoves;

        public NodeSeries(int[] nodes, int[] moves) {
            mNode = nodes;
            mMoves = moves;
        }

        public int[] getNode() {
            return mNode;
        }

        public void setNode(int[] node) {
            mNode = node;
        }

        public int[] getMoves() {
            return mMoves;
        }

        public void setMoves(int[] moves) {
            mMoves = moves;
        }

        public void addNode(int[] node, int move) {
            mNode = node;
            mMoves = ArrayUtils.add(mMoves, move);
        }
    }
}
