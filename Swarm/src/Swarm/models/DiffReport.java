package Swarm.models;

/**
 * Created by pezzati on 1/28/16.
 */
public class DiffReport {
    private int vertex;
    private int owner;
    private int armyCount;

    public DiffReport(int vertex, int owner, int armyCount) {
        this.vertex = vertex;
        this.owner = owner;
        this.armyCount = armyCount;
    }

    public int getVertex() {
        return vertex;
    }

    public int getOwner() {
        return owner;
    }

    public int getArmyCount() {
        return armyCount;
    }
}
