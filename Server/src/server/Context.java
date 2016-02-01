package server;

import server.model.Diff;
import server.model.Map;

/**
 * Created by pezzati on 1/27/16.
 */
public class Context {
    private int map_size;
    private Map map;
    private Diff differ;
    private int turn;

    public Context(String name) {
        this.map = new Map(name);
        this.differ = new Diff(this.map.getVertexNum());
        this.map_size = this.map.getVertexNum();
        this.turn = 0;
    }

    public void flush(){
        this.differ.updateArmyCount(this.map.getArmyCount());
        this.differ.updateOwnership(this.map.getOwnership());
    }

    public int getTurn(){
        return this.turn;
    }

    public void turnUP(){
        this.turn++;
    }

    public int getMap_size() {
        return map_size;
    }

    public Map getMap() {
        return map;
    }

    public Diff getDiffer() {
        return differ;
    }
}
