package Swarm.models;

import java.util.ArrayList;

/**
 * Created by vahid 4/2/17
 */
public class Diff {
    int[][][] changes;
    ArrayList<Integer>[] addFish = new ArrayList[8];
    ArrayList<Integer>[] add = new ArrayList[4];
    ArrayList<Integer>[] del = new ArrayList[1];
    ArrayList<Integer>[] mov = new ArrayList[2];
    ArrayList<Integer>[] alter = new ArrayList[3];
    int type0Num;
    int type1Num;
    int type2Num;
    int type3Num;

    public Diff() {
        this.changes =new int[4][][];
        this.type0Num = 0;
        this.type1Num = 0;
        this.type2Num = 0;
        this.type3Num = 0;
    }

    public void addFish(int id, int type, int x, int y, int dir, int color, int queen, int team){
        addFish[0].add(id);
        addFish[1].add(0);
        addFish[2].add(x);
        addFish[3].add(y);
        addFish[4].add(dir);
        addFish[5].add(color);
        addFish[6].add(queen);
        addFish[7].add(team);
    }

    public void add(int id, int type, int x, int y){
        add[0].add(id);
        add[1].add(type);
        add[2].add(x);
        add[3].add(y);
    }

    public void del(int id){
        del[0].add(id);
    }


    public void mov(int id, int m){
        mov[0].add(id);
        mov[1].add(m);
    }

    public void alter(int id, int color, int sick){
        alter[0].add(id);
        alter[1].add(color);
        alter[2].add(sick);
        changes[3][type3Num][0] = id;
        changes[3][type3Num][1] = color;
        changes[3][type3Num][2] = sick;
        type3Num++;
    }

    public int[][][] getChanges() {
        changes = new int[4][][];
        changes[0] = new int[add[0].size()+addFish[0].size()][8];

        for (int i = 0; i < add[0].size(); i++) {
            for (int j = 0; j < 4; j++) {
                changes[0][i][j] = add[j].get(i);
            }

        }

        for (int i = add[0].size(); i < add[0].size()+addFish[0].size(); i++) {
            for (int j = 0; j < 8; j++) {
                changes[0][i][j] = addFish[j].get(i);
            }

        }

        changes[1] = new int[del[0].size()][1];

        for (int i = 0; i < del[0].size(); i++) {
            changes[1][i][0] = del[0].get(i);
        }

        changes[2] = new int[mov[0].size()][2];
        for (int i = 0; i < mov[0].size(); i++) {
            changes[2][i][0] = mov[0].get(i);
            changes[2][i][1] = mov[1].get(i);
        }

        changes[3] = new int[alter[0].size()][3];
        for (int i = 0; i < alter[0].size(); i++) {
            for (int j = 0; j < 3; j++) {

                changes[3][i][j] = alter[j].get(i);
            }
        }

        return changes;
    }

    public void setChanges(int[][][] changes) {
        this.changes = changes;
    }

    public int getType0Num() {
        return type0Num;
    }

    public void setType0Num(int type0Num) {
        this.type0Num = type0Num;
    }

    public int getType1Num() {
        return type1Num;
    }

    public void setType1Num(int type1Num) {
        this.type1Num = type1Num;
    }

    public int getType2Num() {
        return type2Num;
    }

    public void setType2Num(int type2Num) {
        this.type2Num = type2Num;
    }

    public int getType3Num() {
        return type3Num;
    }

    public void setType3Num(int type3Num) {
        this.type3Num = type3Num;
    }


}
