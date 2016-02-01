package models;

import java.io.BufferedReader;
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

    private String mapName;
    private ArrayList<Node> nodes;

    public Map(String mapName) {
        this.mapName = mapName;
//        this.vertexNum = vertexNum;
//        this.graph = new boolean[vertexNum][vertexNum];
//        this.ownership = new int[vertexNum];
//        for(int i = 0; i < vertexNum; i++)
//            this.ownership[i] = -1;
//        this.armyCount = new int[vertexNum];

        try {
            FileReader fr = new FileReader(mapName + ".txt");
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            String[] inputs = line.split("[\0]")[0].split("[ ]+");

            this.vertexNum = Integer.valueOf(inputs[0]);

            this.nodes = new ArrayList<Node>();
            this.graph = new boolean[this.vertexNum][this.vertexNum];
            this.ownership = new int[this.vertexNum];
            for(int i = 0; i < this.vertexNum; i++)
                this.ownership[i] = -1;
            this.armyCount = new int[this.vertexNum];

            for(int i = 0; i < this.vertexNum; i++){
                String[] args = new String[4];
                System.arraycopy(inputs, 1 + (i * 4), args, 0, 4);
                Node node = new Node(Integer.valueOf(args[2]), Integer.valueOf(args[3]), i);

                this.ownership[i] = Integer.valueOf(args[0]);
                node.setOwnership(this.ownership[i]);
                this.armyCount[i] = Integer.valueOf(args[1]);
                node.setArmyCount(this.armyCount[i]);
                this.nodes.add(node);
            }
            int beginIndex = this.vertexNum * 4 + 1;
            int nodeIndex = 0;
            for(int i = beginIndex; i < inputs.length && nodeIndex < this.vertexNum; i++){
                int input = Integer.valueOf(inputs[i]);
                if(input == -1){
                    nodeIndex++;
                    continue;
                }
                this.graph[nodeIndex][input] = true;
                this.nodes.get(nodeIndex).addNeighbor(this.nodes.get(input));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public boolean setGraph(int x, int y, boolean bool){
        if(x >= this.vertexNum || x < 0 || y >= this.vertexNum || y < 0)
            return false;
        this.graph[x][y] = bool;
        this.nodes.get(x).addNeighbor(this.nodes.get(y));
        return true;
    }

    public boolean setOwnership(int index, int owner){
        if(index >= this.vertexNum || index < 0 || owner > 1 || owner < -1)
            return false;
        this.ownership[index] = owner;
        return true;
    }

    public boolean setArmyCount(int index, int count){
        if(index >= this.vertexNum || index < 0 )
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

    public int[][] getAdjacencyList(){
        int[][] out = new int[this.vertexNum][];
        for(int i = 0; i < this.vertexNum; i++){
            int degree = 0;
            for(int j = 0; j < this.vertexNum; j++){
                if(this.graph[i][j])
                    degree++;
            }
            out[i] = new int[degree];
        }
        for(int i =0; i < this.vertexNum; i++){
            int index = 0;
            for(int j = 0; j < this.vertexNum; j++){
                if(this.graph[i][j]){
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

    public boolean isFinished(){
        int only_owner = -1;
        for(int i = 0; i < this.ownership.length; i++){
            if(this.ownership[i] == -1)
                continue;
            if(only_owner == -1){
                only_owner = this.ownership[i];
                continue;
            }
            if(this.ownership[i] != only_owner)
                return false;
        }
        return true;
    }
}
