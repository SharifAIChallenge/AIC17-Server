package server.model;

/**
 * Created by pezzati on 1/28/16.
 */
public class Diff_report {
    private int vertex;
    private int owner;
    private int armyCount;

    public Diff_report(int vertex, int owner, int armyCount) {
        this.vertex = vertex;
        this.owner = owner;
        this.armyCount = armyCount;
    }
}
