package com.meremammal.www.slidingtilepuzzle.search_algorithms;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Fletcher on 15/10/2015.
 */
public class ManhattanWithLinearConflictHeuristic extends Heuristic<int[]> {

    public ManhattanWithLinearConflictHeuristic(int columnCount, int[] goal) {
        super(columnCount, goal);
    }

    @Override
    public int getEstimatedCost(int[] state) {
        return getManhattan(state) + getLinearConflicts(state) * 2;
    }

    private int getManhattan(int[] state) {
        int manhattan = 0;
        int stateX, stateY, goalIndex, goalX, goalY;
        for (int i = 0; i < state.length; i++) {
            if (state[i] > 0) {
                stateX = i % getColumnCount();
                stateY = i / getColumnCount();
                goalIndex = ArrayUtils.indexOf(getGoal(), state[i]);
                goalX = goalIndex % getColumnCount();
                goalY = goalIndex / getColumnCount();
                manhattan += (Math.abs(stateX - goalX) + Math.abs(stateY - goalY));
            }
        }
        return manhattan;

    }

    /*
    Iterate over the array. If a tile is within 2 manhattan distance from goal
     */

    private int getTileRotations(int[] state) {
        int index;
        int[] tiles;
        for (int x = 0; x < getColumnCount() - 1; x++) {
            for (int y = 0; y < state.length / getColumnCount() - 1; y++) {

            }
        }
        return 0;
    }

    private int getLinearConflicts(int[] state) {

        double linearConflicts = 0;
        int pos1;
        int pos2;

        for (int strip = 0;
             strip < getColumnCount() || strip < getGoal().length / getColumnCount();
             strip++) {

            for (int firstTile = 0;
                 firstTile < getColumnCount() || firstTile < getGoal().length / getColumnCount();
                 firstTile++) {
                for (int secondTile = firstTile + 1;
                     secondTile < getColumnCount() || secondTile < getGoal().length / getColumnCount();
                     secondTile++) {

                    // COLUMNS
                    if (strip < getColumnCount() && secondTile < getGoal().length / getColumnCount()
                            && getGoal()[strip + firstTile * getColumnCount()] > 0
                            && getGoal()[strip + secondTile * getColumnCount()] > 0) {

                        pos1 = ArrayUtils.indexOf(state, getGoal()[strip + firstTile * getColumnCount()]);
                        pos2 = ArrayUtils.indexOf(state, getGoal()[strip + secondTile * getColumnCount()]);

                        /*if (pos1 % getColumnCount() == strip &&
                                pos2 % getColumnCount() == strip &&
                                pos1 / getColumnCount() > pos2 / getColumnCount()) {
                            linearConflicts += 1;
                        }*/
                        if (pos1 % getColumnCount() == pos2 % getColumnCount() &&
                                pos1 / getColumnCount() > pos2 / getColumnCount()) {
                            if (pos1 % getColumnCount() == strip) {
                                linearConflicts += 2;
                            } else {
                                //linearConflicts += 0.5;
                            }
                        }
                    }
                    // ROWS
                    if (strip < getGoal().length / getColumnCount() && secondTile < getColumnCount()
                            && getGoal()[firstTile + strip * getColumnCount()] > 0
                            && getGoal()[secondTile + strip * getColumnCount()] > 0) {

                        pos1 = ArrayUtils.indexOf(state, getGoal()[firstTile + strip * getColumnCount()]);
                        pos2 = ArrayUtils.indexOf(state, getGoal()[secondTile + strip * getColumnCount()]);

                        /*if (pos1 / getColumnCount() == strip &&
                                pos2 / getColumnCount() == strip &&
                                pos1 % getColumnCount() > pos2 % getColumnCount()) {
                            linearConflicts += 1;
                        }*/
                        if (pos1 / getColumnCount() == pos2 / getColumnCount() &&
                                pos1 % getColumnCount() > pos2 % getColumnCount()) {
                            if (pos1 / getColumnCount() == strip) {
                                linearConflicts += 2;
                            } else {
                                //linearConflicts += 0.5;
                            }
                        }
                    }
                }
            }
        }
        return (int) linearConflicts;
    }
}
