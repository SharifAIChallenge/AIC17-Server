package server.model;

import java.util.ArrayList;

/**
 * Created by pezzati on 1/28/16.
 */
public class Diff {
    private static int max_low = 3;
    private static int max_normal = 6;
    private static int max_huge = 10;
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

    public ArrayList<Diff_report> getDiff(int owner){
        ArrayList<Diff_report> diff = new ArrayList<Diff_report>();
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
                    else if(this.prev_armyCount[i] <= max_huge)
                        tempArmyCount = 3;
                }
                diff.add(new Diff_report(i, this.prev_ownership[i], tempArmyCount));
            }
        }
        return diff;
    }

    public boolean isFinished(){
        int only_owner = -1;
        for(int i = 0; i < this.prev_ownership.length; i++){
            if(this.prev_ownership[i] == -1)
                continue;
            if(only_owner == -1){
                only_owner = this.prev_ownership[i];
                continue;
            }
            if(this.prev_ownership[i] != only_owner)
                return false;
        }
        return true;
    }
}
