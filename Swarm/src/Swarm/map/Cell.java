package Swarm.map;

import Swarm.objects.Fish;
import Swarm.objects.GameObject;
import Swarm.objects.Net;
import Swarm.objects.Teleport;

import java.util.ArrayList;

public class Cell {


    // game object
    // x,y
    private int row, column;
    private GameObject content;
    private Teleport teleport;
    private ArrayList<Net> nets;
    private int contentAge = 0;
    private boolean hasFishnet = false;
    private boolean hasOutTeleport = false;
    private ArrayList<Fish> collidingFishes;

    // constructors

    public Cell() {
        this.collidingFishes = new ArrayList<>();
    }

    public Cell(GameObject content, boolean hasFishnet, boolean hasOutTeleport) {
        this.content = content;
        this.hasFishnet = hasFishnet;
        this.hasOutTeleport = hasOutTeleport;
        this.collidingFishes = new ArrayList<>();
    }

    public Cell(GameObject content, Fish fish) {
        this.content = content;
 //       this.fish = fish;
        this.collidingFishes = new ArrayList<>();
    }

    public Cell(GameObject content) {
        this.content = content;
        this.collidingFishes = new ArrayList<>();
    }

    /// getter & setter

    public GameObject getContent() {
        return content;
    }

    public void setContent(GameObject content) {
        this.content = content;
    }

    public int getContentAge() {
        return contentAge;
    }

    public void setContentAge(int contentAge) {
        this.contentAge = contentAge;
    }

    public boolean isHasFishnet() {
        return hasFishnet;
    }

    public void setHasFishnet(boolean hasFishnet) {
        this.hasFishnet = hasFishnet;
    }

    public boolean isHasOutTeleport() {
        return hasOutTeleport;
    }

    public void setHasOutTeleport(boolean hasOutTeleport) {
        this.hasOutTeleport = hasOutTeleport;
    }

    public ArrayList<Fish> getCollidingFishes() {
        return collidingFishes;
    }

    public void setCollidingFishes(ArrayList<Fish> collidingFishes) {
        this.collidingFishes = collidingFishes;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
