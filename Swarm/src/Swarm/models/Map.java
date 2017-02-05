package Swarm.models;

import Swarm.objects.*;
import network.Json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import Swarm.map.Cell;
/**
 * Created by pezzati on 1/27/16.
 */
public class Map {
    private int idCounter = 0;
    private ArrayList<Teleport> teleports = new ArrayList<>();

    public int getIdCounter() {
        return idCounter;
    }

    public void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }

    private int h,w;
    private ArrayList<Fish>[] fishes;
    private ArrayList<GameObject> tempObjects = new ArrayList<>();

    private int[] score = new int[2];

    private String mapName;
    private Cell[][] cells;

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    private void makeFish(int[][] fishes) {
        /*
        [id, x, y, direction, color, queen, sick, team]
         */
        for (int i = 0; i < fishes.length; i++) {
            boolean b = false;
            if(fishes[i][5] == 1){
                b = true;
            }
            Fish fish = new Fish(idCounter,cells[fishes[i][0]][fishes[i][1]], fishes[i][6],fishes[i][2],b,fishes[i][3]);

            idCounter++;

            if(fish.getTeamNumber() == 0)
                this.fishes[0].add(fish);
            else
                this.fishes[1].add(fish);

            cells[fishes[i][1]][fishes[i][2]].setContent(fish);

        }

    }

    public ArrayList<Fish>[] getFishes() {
        return fishes;
    }

    public void setFishes(ArrayList<Fish>[] fishes) {
        this.fishes = fishes;
    }

    public ArrayList<GameObject> getTempObjects() {
        return tempObjects;
    }

    public void setTempObjects(ArrayList<GameObject> tempObjects) {
        this.tempObjects = tempObjects;
    }

    public Map(File mapFile) {
        this.mapName = mapFile.getName();


        try {
            MapJson mapJson = Json.GSON.fromJson(new FileReader(mapFile), MapJson.class);
            this.w = mapJson.w;
            this.h = mapJson.h;

            cells= new Cell[w][];
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    cells[i][j] = new Cell();
                    cells[i][j].setRow(i);
                    cells[i][j].setColumn(j);
                }
            }

            makeFish(mapJson.fishes);
            makeFood(mapJson.foods);
            makeTrash(mapJson.trashes);
            makeNets(mapJson.nets);

            makeTeleport(mapJson.teleports);
            /*
            makeConstant(mapJson.constants);
            */

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Teleport> getTeleports() {
        return teleports;
    }

    public void setTeleports(ArrayList<Teleport> teleports) {
        this.teleports = teleports;
    }

    private void makeTeleport(int[][] teleports) {
        for (int i = 0; i < teleports.length; i++) {
            Teleport teleport1 = new Teleport(idCounter,cells[teleports[i][0]][teleports[i][1]]);
            idCounter++;
            Teleport teleport2 = new Teleport(idCounter,cells[teleports[i][2]][teleports[i][3]]);
            idCounter++;
            teleport1.setPair(cells[teleports[i][2]][teleports[i][3]]);
            teleport2.setPair(cells[teleports[i][0]][teleports[i][1]]);

            this.tempObjects.add(teleport1);
            this.tempObjects.add(teleport2);
            cells[teleports[i][0]][teleports[i][1]].setTeleport(teleport1);
            cells[teleports[i][2]][teleports[i][3]].setTeleport(teleport2);

        }
    }

    private void makeNets(int[][] nets) {
        for (int i = 0; i < nets.length; i++) {
            Net net = new Net(idCounter,cells[nets[i][0]][nets[i][1]]);
            idCounter++;
            this.tempObjects.add(net);
            cells[nets[i][1]][nets[i][2]].setNet(net);

        }
    }

    private void makeTrash(int[][] trashes) {
        for (int i = 0; i < trashes.length; i++) {
            Trash trash = new Trash(idCounter,cells[trashes[i][0]][trashes[i][1]]);
            idCounter++;
            this.tempObjects.add(trash);
            cells[trashes[i][1]][trashes[i][2]].setContent(trash);

        }
    }

    private void makeFood(int[][] foods) {

        for (int i = 0; i < foods.length; i++) {
            Food food = new Food(idCounter,cells[foods[i][0]][foods[i][1]]);
            idCounter++;
            this.tempObjects.add(food);
            cells[foods[i][1]][foods[i][2]].setContent(food);

        }
    }

    private class MapJson {
        private int  w;
        private int h;
        private int[][] fishes;
        private int[][] foods;
        private int[][] trashes;
        private int[][] teleports;
        private int[][] constants;
        private int[][] nets;
    }


    public String getMapName() {
        return mapName;
    }


    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int[] getScore() {
        return score;
    }

    public void setScore(int[] score) {
        this.score = score;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

}
