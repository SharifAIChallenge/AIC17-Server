package Swarm.map;

import Swarm.objects.Fish;
import Swarm.objects.GameObject;
import Swarm.objects.Net;
import Swarm.objects.Teleport;

import java.io.Serializable;
import java.util.ArrayList;

public class Cell implements Serializable {

    private int row, column;
    private GameObject content = null;
    private Teleport teleport = null;
    private Net net = null;

    // constructors
    public Cell(){

    }

    public Cell(int row, int column, GameObject content, Teleport teleport, Net net) {
        this.row = row;
        this.column = column;
        this.content = content;
        this.teleport = teleport;
        this.net = net;
    }

    public Cell(int row, int column, GameObject content, Teleport teleport) {
        this.row = row;
        this.column = column;
        this.content = content;
        this.teleport = teleport;
    }

    /// getter & setter

    public GameObject getContent() {
        return content;
    }

    public void setContent(GameObject content) {
        this.content = content;
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


    public Teleport getTeleport() {
        return teleport;
    }

    public void setTeleport(Teleport teleport) {
        this.teleport = teleport;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", row, column);
    }
}
