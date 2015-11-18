package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;

/**
 * Created by Fletcher on 15/10/2015.
 */
public abstract class Node<S, N extends Node<S, N>> {

    private S mState;
    private N mParent;
    private N[] mChildren;
    private int mCost = -1;
    private int mMoveCount;

    public Node(S state, N parent) {
        this(state, parent, 0);
        mMoveCount = 0;
    }

    public Node(S state, N parent, int moveCount) {
        mState = state;
        mParent = parent;
        mMoveCount = moveCount;
    }

    public int getCost() {
        return mCost;
    }

    public void setCost(int cost) {
        mCost = cost;
    }

    public S getState() {
        return mState;
    }

    public N getParent() {
        return mParent;
    }

    public abstract void expand();

    /**
     * Obtain a child state of this state. It is removed from the list of children in the process
     *
     * @return a child Node or null if none exist
     */
    public N getChild() {
        if (mChildren != null && mChildren.length > 0) {
            N child = mChildren[0];
            mChildren = ArrayUtils.remove(mChildren, 0);
            return child;
        }
        mChildren = null;
        return null;
    }

    public int getMoveCount() {
        return mMoveCount;
    }

    public int getDisjointMoveCount() {
        int moves = 0;
        if (mParent != null) {
            moves = mParent.getDisjointMoveCount();
            if (getLastMove() > 0) {
                moves++;
            }
        }
        return moves;
    }

    public int[] getMoves() {
        int[] moves = new int[getMoveCount()];
        moves[getMoveCount() - 1] = getLastMove();
        if (getParent() != null) {
            getParent().getMoves(moves);
        }
        return moves;
    }

    public void getMoves(int[] moves) {
        if (getParent() != null) {
            moves[getMoveCount() - 1] = getLastMove();
            getParent().getMoves(moves);
        }
    }

    public abstract int getLastMove();

    public void addChild(N child) {
        if (child != null) {
            mChildren = ArrayUtils.add(mChildren, child);
        }
    }

    public N[] getChildren() {
        return mChildren;
    }

    public String getHistory() {
        String history;
        if (getParent() != null) {
            history = getParent().getHistory() + "\n    Move: " + getLastMove() + "\n\n";
        } else {
            history = "";
        }
        return history.concat(toString());
    }

    public N[] getPath() {
        N[] path = (N[]) Array.newInstance(this.getClass(), getMoveCount() + 1);
        path[getMoveCount()] = (N) this;
        if (getParent() != null) {
            getParent().getPath(path);
        }
        return path;
    }

    protected void getPath(N[] path) {
        path[getMoveCount()] = (N) this;
        if (getParent() != null) {
            getParent().getPath(path);
        }
    }

    public abstract boolean isGoal(S goal);
}
