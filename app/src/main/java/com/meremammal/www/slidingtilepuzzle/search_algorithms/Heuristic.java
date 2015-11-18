package com.meremammal.www.slidingtilepuzzle.search_algorithms;

/**
 * Created by Fletcher on 13/10/2015.
 */
public abstract class Heuristic<T> {

    private int mColumnCount;
    private T mGoal;

    public Heuristic(int columnCount, T goal) {
        mColumnCount = columnCount;
        mGoal = goal;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public T getGoal() {
        return mGoal;
    }

    public abstract int getEstimatedCost(T tiles);
}
