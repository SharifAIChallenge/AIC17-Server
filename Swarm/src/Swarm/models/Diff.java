package Swarm.models;

/**
 * Created by vahid 4/2/17
 */
public class Diff {
    int[][][] changes;
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
        changes[0][type0Num][0] = id;
        changes[0][type0Num][1] = 0;
        changes[0][type0Num][2] = x;
        changes[0][type0Num][3] = y;
        changes[0][type0Num][4] = dir;
        changes[0][type0Num][5] = color;
        changes[0][type0Num][6] = queen;
        changes[0][type0Num][7] = team;
        type0Num++;
    }

    public void add(int id, int type, int x, int y){
        changes[0][type0Num][0] = id;
        changes[0][type0Num][1] = type;
        changes[0][type0Num][2] = x;
        changes[0][type0Num][3] = y;
        type0Num++;
    }

    public void del(int id){
        changes[1][type1Num][0] = id;
        type1Num++;
    }


    public void mov(int id, int m){
        changes[2][type2Num][0] = id;
        changes[2][type2Num][1] = m;
        type2Num++;
    }

    public void alter(int id, int color, int sick){
        changes[3][type3Num][0] = id;
        changes[3][type3Num][1] = color;
        changes[3][type3Num][2] = sick;
        type3Num++;
    }

    public int[][][] getChanges() {
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
