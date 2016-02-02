import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import models.Diff;
import models.Diff_report;
import models.Map;
import server.core.model.ClientInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private ClientInfo[] clientsInfo;

    public Context(String name, String clientIntoDir) {
        super();

        this.map = new Map(name);
//        System.err.println(this.map.getVertexNum());
        int[][] xy = new int[2][this.map.getVertexNum()];
        for(int i = 0; i < this.map.getVertexNum(); i++){
            xy[0][i] = this.map.getNode(i).getX();
            xy[1][i] = this.map.getNode(i).getY();
        }
        this.uiTest = new UITest(this.map.getVertexNum(), this.map.getAdjacencyList(), xy[0], xy[1]);
        this.differ = new Diff(this.map.getVertexNum());
        this.map_size = this.map.getVertexNum();
        this.turn = 0;

        Gson gson = new Gson();
        try {
            File file = new File(clientIntoDir);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            clientsInfo = gson.fromJson(bufferedReader, ClientInfo[].class);
        } catch (FileNotFoundException notFound) {
            throw new RuntimeException("flows/clients config file not found");
        } catch (JsonParseException e) {
            throw new RuntimeException("flows/clients file does not meet expected syntax");
        }

        //make teams
        clientsInfo[0].setID(0);
        clientsInfo[1].setID(1);

    }

    public void flush(){
        this.differ.updateArmyCount(this.map.getArmyCount());
        this.differ.updateOwnership(this.map.getOwnership());
        this.uiTest.update(this.map.getOwnership(), this.map.getArmyCount());
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

    public int[][] getDiffList(int owner){
        ArrayList<Diff_report> diffs = this.differ.getDiff(owner);
        int[][] intDiff = new int[diffs.size()][3];
        for(int i = 0; i < diffs.size(); i++){
            intDiff[i][0] = diffs.get(i).getVertex();
            intDiff[i][1] = diffs.get(i).getOwner();
            intDiff[i][2] = diffs.get(i).getArmyCount();
        }
        return  intDiff;
    }

    public ClientInfo[] getClientsInfo() {
        return clientsInfo;
    }
}
