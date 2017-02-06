package Swarm.objects;
import Swarm.map.Cell;

public class Fish extends GameObject {

    // property :

    private int teamNumber; // 0 or 1
    private int direction; // 0: right, 1:up, 2:left, 3: down

    private int colorNumber; // 0 or 1
    private int power = 0;

    private boolean queen;
    private boolean sick = false;
    private boolean pregnant = false;

    /**
     * this.power = 0;
     * this.sick = false;
     * this.pregnant = false
     */

    public Fish(int id, Cell position, int teamNumber, int direction, int colorNumber, boolean queen) {
        super(id, position);
        this.teamNumber = teamNumber;
        this.direction = direction;
        this.colorNumber = colorNumber;
        this.queen = queen;

    }

    //    public Fish(int id, Cell position, int teamNumber, int direction, boolean queen, int colorNumber) {
//        super(id, position);
//        this.teamNumber = teamNumber;
//        this.direction = direction;
//        this.queen = queen;
//        this.colorNumber = colorNumber;
//        //
//        this.sickAge = 0;
//        this.power = 0;
//        this.pregnant = false;
//    }


    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public boolean isQueen() {
        return queen;
    }

    public void setQueen(boolean queen) {
        this.queen = queen;
    }

    public boolean isSick() {
        return sick;
    }

    public void setSick(boolean sick) {
        this.sick = sick;
    }

    public int getColorNumber() {
        return colorNumber;
    }

    public void setColorNumber(int colorNumber) {
        this.colorNumber = colorNumber;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public boolean isPregnant() {
        return pregnant;
    }

    public void setPregnant(boolean pregnant) {
        this.pregnant = pregnant;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
