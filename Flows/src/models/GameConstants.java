package models;

/**
 * Copyright (C) 2016 Hadi
 */
public class GameConstants {
    private int turns;
    private long turnTimeout;
    private int escape;
    private int nodeBonus;
    private int edgeBonus;
    private int firstlvl;
    private int secondlvl;
    private double lossRate1;
    private double lossRate2;

    public GameConstants(int turns, long turnTimeout, int escape, int nodeBonus, int edgeBonus, int firstlvl, int secondlvl, double lossRate1, double lossRate2) {
        this.turns = turns;
        this.turnTimeout = turnTimeout;
        this.escape = escape;
        this.nodeBonus = nodeBonus;
        this.edgeBonus = edgeBonus;
        this.firstlvl = firstlvl;
        this.secondlvl = secondlvl;
        this.lossRate1 = lossRate1;
        this.lossRate2 = lossRate2;
    }

    public void setTurnTimeout(long timeout) {
        this.turnTimeout = timeout;
    }

    public int getTurns() {
        return turns;
    }

    public long getTurnTimeout() {
        return turnTimeout;
    }

    public int getEscape() {
        return escape;
    }

    public int getNodeBonus() {
        return nodeBonus;
    }

    public int getEdgeBonus() {
        return edgeBonus;
    }

    public int getFirstlvl() {
        return firstlvl;
    }

    public int getSecondlvl() {
        return secondlvl;
    }

    public double getLossRate1() {
        return lossRate1;
    }

    public double getLossRate2() {
        return lossRate2;
    }
}
