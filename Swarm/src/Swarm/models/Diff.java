package Swarm.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vahid 4/2/17
 */
public class Diff {
    private class Change {
        private Character type;
        private ArrayList<List<Integer>> args;

        public Change(Character type, ArrayList<List<Integer>> args) {
            this.type = type;
            this.args = args;
        }
    }
    ArrayList<List<Integer>> add = new ArrayList<>();
    ArrayList<List<Integer>> del = new ArrayList<>();
    ArrayList<List<Integer>> mov = new ArrayList<>();
    ArrayList<List<Integer>> alter = new ArrayList<>();
    int type0Num;
    int type1Num;
    int type2Num;
    int type3Num;

    public Diff() {
        this.type0Num = 0;
        this.type1Num = 0;
        this.type2Num = 0;
        this.type3Num = 0;
    }

    public void addFish(int id, int type, int x, int y, int dir, int color, int queen, int team){
        ArrayList<Integer> addFish = new ArrayList<>();
        addFish.add(id);
        addFish.add(0);
        addFish.add(x);
        addFish.add(y);
        addFish.add(dir);
        addFish.add(color);
        addFish.add(queen);
        addFish.add(team);
        add.add(addFish);
    }

    public void add(int id, int type, int x, int y){
        ArrayList<Integer> addItem = new ArrayList<>();
        addItem.add(id);
        addItem.add(type);
        addItem.add(x);
        addItem.add(y);
        add.add(addItem);
    }

    public void del(int id){
        ArrayList<Integer> delItem = new ArrayList<>();
        delItem.add(id);
        del.add(delItem);
    }


    public void mov(int id, int m){
        ArrayList<Integer> movItem = new ArrayList<>();
        movItem.add(id);
        movItem.add(m);
        mov.add(movItem);
    }

    public void alterFish(int id,int x,int y, int color, int sick){
        ArrayList<Integer> alterItem = new ArrayList<>();
        alterItem.add(id);
        alterItem.add(x);
        alterItem.add(y);
        alterItem.add(color);
        alterItem.add(sick);
        alter.add(alterItem);
    }

    public void alterItem(int id, int x, int y) {
        ArrayList<Integer> alterItems = new ArrayList<>();
        alterItems.add(id);
        alterItems.add(x);
        alterItems.add(y);
        alter.add(alterItems);
    }

    public Change[] getChanges() {
        Change[] changes = new Change[4];
        changes[0] = new Change('d', del);
        changes[1] = new Change('a', add);
        changes[2] = new Change('m', mov);
        changes[3] = new Change('c', alter);
        return changes;
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
