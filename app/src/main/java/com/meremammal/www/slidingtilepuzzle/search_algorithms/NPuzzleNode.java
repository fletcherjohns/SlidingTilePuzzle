package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by Fletcher on 15/10/2015.
 */
public class NPuzzleNode extends Node<int[], NPuzzleNode> {
    private int mColumnCount;
    private int mBlankIndex;

    public NPuzzleNode(int[] state, int columnCount) {
        this(state, columnCount, null, 0, -1);
    }

    public NPuzzleNode(int[] state, int columnCount, NPuzzleNode parent, int moveCount, int blankIndex) {
        super(state, parent, moveCount);
        mColumnCount = columnCount;
        if (blankIndex == -1) {
            mBlankIndex = ArrayUtils.indexOf(state, 0);
        } else {
            mBlankIndex = blankIndex;
        }
    }

    @Override
    public void expand() {
        int blankX = mBlankIndex % mColumnCount;
        int blankY = mBlankIndex / mColumnCount;

        if (blankX > 0) {
            addChild(checkAndCreateChild(mBlankIndex - 1));
        }
        if (blankX < mColumnCount - 1) {
            addChild(checkAndCreateChild(mBlankIndex + 1));
        }
        if (blankY > 0) {
            addChild(checkAndCreateChild(mBlankIndex - mColumnCount));
        }
        if (blankY < getState().length / mColumnCount - 1) {
            addChild(checkAndCreateChild(mBlankIndex + mColumnCount));
        }
    }

    public NPuzzleNode checkAndCreateChild(int index) {
        if (getParent() != null && index == getParent().mBlankIndex) return null;

        int[] childState = getState().clone();
        childState[mBlankIndex] = childState[index];
        childState[index] = 0;
        return new NPuzzleNode(childState, mColumnCount, this, getMoveCount() + 1, index);
    }

    public int getBlankIndex() {

        return mBlankIndex;
    }

    @Override
    public int getLastMove() {
        if (getParent() != null) {
            return getState()[getParent().getBlankIndex()];
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        String s = "node... moves = " + getMoveCount() + ", cost = " + getCost();
        for (int i = 0; i < getState().length; i++) {
            if (i % mColumnCount == 0) s = s.concat("\n");

            s = s.concat(getState()[i] + ", ");
        }
        return s;
    }

    @Override
    public boolean isGoal(int[] goal) {
        return Arrays.equals(goal, getState());
        /*for (int i = 0; i < getTileState().length; i++) {
            if (getTileState()[i] > 0 && getTileState()[i] != goal[i]) {
                return false;
            }
        }
        return true;*/
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NPuzzleNode) {
            return Arrays.equals(getState(), ((NPuzzleNode) o).getState());
            /*NPuzzleNode other = (NPuzzleNode) o;
            for (int i = 0; i < getTileState().length; i++) {
                if (getTileState()[i] >= 0 && getTileState()[i] != other.getTileState()[i]) {
                    return false;
                }
            }
            return true;*/
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getState());
        /*int hashCode = 0;
        for (int i = 0; i < getTileState().length; i++) {
            if (getTileState()[i] >= 0) hashCode += (getTileState()[i] + 1) * (i + 1) * 13;
        }
        return hashCode;*/
    }
}
