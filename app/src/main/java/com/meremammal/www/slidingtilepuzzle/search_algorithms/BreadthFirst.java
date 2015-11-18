package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Fletcher on 11/11/2015.
 * Useful class for exploring the puzzle graph. I use it to examine the heuristic cost/actual moves
 */
public class BreadthFirst {


    private Queue<NPuzzleNode> mNodeQueue;
    private Set<NPuzzleNode> mNPuzzleNodes;
    private Heuristic<int[]> mHeuristic;

    public BreadthFirst(int[] goal, int columnCount) {
        mNodeQueue = new ArrayDeque<>();
        mNodeQueue.add(new NPuzzleNode(goal, columnCount));
        mNPuzzleNodes = new HashSet<>();
        mHeuristic = new ManhattanWithLinearConflictHeuristic(columnCount, goal);
        run();
    }

    private void run() {
        NPuzzleNode node;
        NPuzzleNode child;
        while ((node = mNodeQueue.poll()) != null) {
            if (node.getMoveCount() > 6) return;
            mNPuzzleNodes.add(node);
            node.setCost(mHeuristic.getEstimatedCost(node.getState()));
            Log.v("tag", node.toString());
            node.expand();
            while ((child = node.getChild()) != null) {
                if (!mNPuzzleNodes.contains(child)) mNodeQueue.add(child);
            }
        }
    }
}
