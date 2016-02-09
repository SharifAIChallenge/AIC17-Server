package models;

import java.util.ArrayList;

/**
 * Created by pezzati on 1/28/16.
 */
public class Diff {
    private int length;
    private int[] prevOwnership;
    private int[] prevArmyCount;

    private boolean[] diffOwnership;
    private boolean[] diffArmyCount;

    public Diff(int length) {
        this.length = length;
        this.prevArmyCount = new int[length];
        this.prevOwnership = new int[length];
        for(int i = 0; i < length; i++)
            this.prevOwnership[i] = -1;
        this.diffOwnership = new boolean[length];
        this.diffArmyCount = new boolean[length];
    }

    public void updateOwnership(int[] new_ownership){
        for(int i = 0; i < this.length; i++){
            this.diffOwnership[i] = false;
            if(new_ownership[i] != this.prevOwnership[i])
                this.diffOwnership[i] = true;
            this.prevOwnership[i] = new_ownership[i];
        }
    }

    public void updateArmyCount(int[] new_armyCount){
        for(int i = 0; i < this.length; i++){
            this.diffArmyCount[i] = false;
            if(new_armyCount[i] != this.prevArmyCount[i])
                this.diffArmyCount[i] = true;
            this.prevArmyCount[i] = new_armyCount[i];
        }
    }

    public boolean[] getDiffOwnership() {
        return diffOwnership;
    }

    public boolean[] getDiffArmyCount() {
        return diffArmyCount;
    }

    public ArrayList<DiffReport> getDiff(int owner, int max_low, int max_normal){
        ArrayList<DiffReport> diff = new ArrayList<>();
        for(int i = 0; i < this.length; i++){
            if(this.diffArmyCount[i] || this.diffOwnership[i]){
                int tempArmyCount = 0;
                if(this.prevOwnership[i] == owner)
                    tempArmyCount = this.prevArmyCount[i];
                else{
                    if(this.prevArmyCount[i] <= max_low)
                        tempArmyCount = 1;
                    else if(this.prevArmyCount[i] <= max_normal)
                        tempArmyCount = 2;
                    else
                        tempArmyCount = 3;
                }
                diff.add(new DiffReport(i, this.prevOwnership[i], tempArmyCount));
            }
        }

        return diff;
    }

    public ArrayList<DiffReport> getUIDiff(){
        ArrayList<DiffReport> diff = new ArrayList<>();
        for(int i = 0; i < this.length; i++){
            diff.add(new DiffReport(i, this.prevOwnership[i], this.prevArmyCount[i]));
        }
        return diff;
    }

}
