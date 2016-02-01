package models;

import java.util.ArrayList;

/**
 * Created by pezzati on 1/27/16.
 */
public class Node {
    private int id;
    private int x,y;
    private ArrayList<Node> neighbors;
    private int ownership;
    private int armyCount;
    private int range;

    public Node(int x, int y, int id) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.ownership = 0;
        this.armyCount = 0;
        this.neighbors = new ArrayList<Node>();
        this.range = 0;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void addNeighbor(Node node){
        this.neighbors.add(node);
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    public int getOwnership() {
        return ownership;
    }

    public void setOwnership(int ownership) {
        this.ownership = ownership;
    }

    public int getUnitCount() {
        return armyCount;
    }

    public void setArmyCount(int unitCount) {
        this.armyCount = unitCount;
    }
}
