package Swarm.map;

import Swarm.objects.Fish;
import Swarm.objects.GameObject;
import Swarm.objects.Net;
import Swarm.objects.Teleport;

import java.util.ArrayList;

public class Cell {

    private Teleport teleport;
    private Net net;
    private int row, column;
    private GameObject content;
    private int contentAge = 0;

    // constructors

    public Cell() {

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



}
