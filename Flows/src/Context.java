import models.Diff;
import models.DiffReport;
import models.Map;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by pezzati on 1/27/16.
 */
public class Context {
    private int map_size;
    private Map map;
    private Diff differ;
    private int turn;
    private UITest uiTest;

    public Context(File map) {
        super();

        this.map = new Map(map);
//        System.err.println(this.map.getVertexNum());
        int[][] xy = new int[2][this.map.getVertexNum()];
        for (int i = 0; i < this.map.getVertexNum(); i++) {
            xy[0][i] = this.map.getNode(i).getX();
            xy[1][i] = this.map.getNode(i).getY();
        }
        this.uiTest = new UITest(this.map.getVertexNum(), this.map.getAdjacencyList(), xy[0], xy[1]);
        this.differ = new Diff(this.map.getVertexNum());
        this.map_size = this.map.getVertexNum();
        this.turn = 0;
    }

    public void flush() {
        this.differ.updateArmyCount(this.map.getArmyCount());
        this.differ.updateOwnership(this.map.getOwnership());
        this.uiTest.update(this.map.getOwnership(), this.map.getArmyCount());
    }

    public int getTurn() {
        return this.turn;
    }

    public void turnUP() {
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

    public int[][] getDiffList(int owner) {
        ArrayList<DiffReport> diffs = this.differ.getDiff(owner);
        int[][] intDiff = new int[diffs.size()][3];
        for (int i = 0; i < diffs.size(); i++) {
            intDiff[i][0] = diffs.get(i).getVertex();
            intDiff[i][1] = diffs.get(i).getOwner();
            intDiff[i][2] = diffs.get(i).getArmyCount();
        }
        return intDiff;
    }

    public int[][] getUIDiffList() {
        ArrayList<DiffReport> diffs = this.differ.getUIDiff();
        int[][] intDiff = new int[diffs.size()][3];
        for (int i = 0; i < diffs.size(); i++) {
            int index = diffs.get(i).getVertex();
            intDiff[index][0] = diffs.get(i).getOwner();
            intDiff[index][1] = diffs.get(i).getArmyCount();
            intDiff[index][2] = map.getNode(index).getX();
            intDiff[index][3] = map.getNode(index).getY();
        }
        return intDiff;
    }

}
