package models;

import network.Json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pezzati on 1/27/16.
 */
public class Map {
    private int vertexNum;
    private boolean[][] graph;
    private int[] ownership;
    private int[] armyCount;
    private GameConstants gameConstants;

    private String mapName;
    private ArrayList<Node> nodes;

    public Map(File mapFile) {
        this.mapName = mapFile.getName();
//        this.vertexNum = vertexNum;
//        this.graph = new boolean[vertexNum][vertexNum];
//        this.ownership = new int[vertexNum];
//        for(int i = 0; i < vertexNum; i++)
//            this.ownership[i] = -1;
//        this.armyCount = new int[vertexNum];

        try {
            MapJson mapJson = Json.GSON.fromJson(new FileReader(mapFile), MapJson.class);
            this.vertexNum = mapJson.count;
            this.nodes = new ArrayList<>();
            this.graph = new boolean[this.vertexNum][this.vertexNum];
            this.ownership = new int[this.vertexNum];
            for (int i = 0; i < this.vertexNum; i++)
                this.ownership[i] = -1;
            this.armyCount = new int[this.vertexNum];

            for (int i = 0; i < this.vertexNum; i++) {
                Node node = new Node(mapJson.props[i][2], mapJson.props[i][3], i);
                this.ownership[i] = mapJson.props[i][0];
                node.setOwnership(this.ownership[i]);
                this.armyCount[i] = mapJson.props[i][1];
                node.setArmyCount(this.armyCount[i]);
                this.nodes.add(node);
            }

            int[][] adj = mapJson.adj;
            for (int i = 0; i < adj.length; i++) {
                for (int j = 0; j < adj[i].length; j++) {
                    this.graph[i][adj[i][j]] = true;
                    this.nodes.get(i).addNeighbor(this.nodes.get(adj[i][j]));
                }
            }

            gameConstants = new GameConstants(0, mapJson.escape, mapJson.nodeBonus, mapJson.edgeBonus, mapJson.firstlvl, mapJson.secondlvl, mapJson.lossRate1, mapJson.lossRate2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MapJson {
        private int count;
        private int[][] props;
        private int[][] adj;
        private int escape;
        private int nodeBonus;
        private int edgeBonus;
        private int firstlvl;
        private int secondlvl;
        private double lossRate1;
        private double lossRate2;
    }

    public int getVertexNum() {
        return vertexNum;
    }

    public boolean[][] getGraph() {
        return graph;
    }

    public int[] getOwnership() {
        return ownership;
    }

    public int[] getArmyCount() {
        return armyCount;
    }

    public GameConstants getGameConstants() {
        return gameConstants;
    }

    public boolean setGraph(int x, int y, boolean bool) {
        if (x >= this.vertexNum || x < 0 || y >= this.vertexNum || y < 0)
            return false;
        this.graph[x][y] = bool;
        this.nodes.get(x).addNeighbor(this.nodes.get(y));
        return true;
    }

    public boolean setOwnership(int index, int owner) {
        if (index >= this.vertexNum || index < 0 || owner > 1 || owner < -1)
            return false;
        this.ownership[index] = owner;
        return true;
    }

    public boolean setArmyCount(int index, int count) {
        if (index >= this.vertexNum || index < 0)
            return false;
        this.armyCount[index] = count;
        return true;
    }

//    public String getAdjacencyList(){
//        String out = "";
//        for(int i = 0; i < this.vertexNum; i++){
//            out = out + String.valueOf(i) + ":";
//            for(int j = i + 1; j < this.vertexNum; j++){
//                if(this.graph[i][j])
//                    out = out + String.valueOf(j) + ",";
//            }
//            out = out + "\n";
//        }
//        return out;
//    }

    public int[][] getAdjacencyList() {
        int[][] out = new int[this.vertexNum][];
        for (int i = 0; i < this.vertexNum; i++) {
            int degree = 0;
            for (int j = 0; j < this.vertexNum; j++) {
                if (this.graph[i][j])
                    degree++;
            }
            out[i] = new int[degree];
        }
        for (int i = 0; i < this.vertexNum; i++) {
            int index = 0;
            for (int j = 0; j < this.vertexNum; j++) {
                if (this.graph[i][j]) {
                    out[i][index] = j;
                    index++;
                }
            }
        }
        return out;
    }


    public String getMapName() {
        return mapName;
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

    public boolean isFinished() {
        int only_owner = -1;
        for (int i = 0; i < this.ownership.length; i++) {
            if (this.ownership[i] == -1)
                continue;
            if (only_owner == -1) {
                only_owner = this.ownership[i];
                continue;
            }
            if (this.ownership[i] != only_owner)
                return false;
        }
        return true;
    }


}
