package models;

import java.util.ArrayList;

/**
 * Created by pezzati on 1/28/16.
 */
public class Diff {
    public static final int max_low = 3;
    public static final int max_normal = 6;
    public static final int max_huge = 10;
    private int length;
    private int[] prev_ownership;
    private int[] prev_armyCount;

    private boolean[] diff_ownership;
    private boolean[] diff_armyCount;

    public Diff(int length) {
        this.length = length;
        this.prev_armyCount = new int[length];
        this.prev_ownership = new int[length];
        for(int i = 0; i < length; i++)
            this.prev_ownership[i] = -1;
        this.diff_ownership = new boolean[length];
        this.diff_armyCount = new boolean[length];
    }

    public void updateOwnership(int[] new_ownership){
        for(int i = 0; i < this.length; i++){
            this.diff_ownership[i] = false;
            if(new_ownership[i] != this.prev_ownership[i])
                this.diff_ownership[i] = true;
            this.prev_ownership[i] = new_ownership[i];
        }
    }

    public void updateArmyCount(int[] new_armyCount){
        for(int i = 0; i < this.length; i++){
            this.diff_armyCount[i] = false;
            if(new_armyCount[i] != this.prev_armyCount[i])
                this.diff_armyCount[i] = true;
            this.prev_armyCount[i] = new_armyCount[i];
        }
    }

    public boolean[] getDiff_ownership() {
        return diff_ownership;
    }

    public boolean[] getDiff_armyCount() {
        return diff_armyCount;
    }

    public ArrayList<DiffReport> getDiff(int owner){
        ArrayList<DiffReport> diff = new ArrayList<>();
        for(int i = 0; i < this.length; i++){
            if(this.diff_armyCount[i] || this.diff_ownership[i]){
                int tempArmyCount = 0;
                if(this.prev_ownership[i] == owner)
                    tempArmyCount = this.prev_armyCount[i];
                else{
                    if(this.prev_armyCount[i] <= max_low)
                        tempArmyCount = 1;
                    else if(this.prev_armyCount[i] <= max_normal)
                        tempArmyCount = 2;
                    else if(this.prev_armyCount[i] > max_normal)
                        tempArmyCount = 3;
                }
                diff.add(new DiffReport(i, this.prev_ownership[i], tempArmyCount));
            }
        }

        return diff;
    }

    public ArrayList<DiffReport> getUIDiff(){
        ArrayList<DiffReport> diff = new ArrayList<>();
        for(int i = 0; i < this.length; i++){
            diff.add(new DiffReport(i, this.prev_ownership[i], this.prev_armyCount[i]));
        }
        return diff;
    }

}
