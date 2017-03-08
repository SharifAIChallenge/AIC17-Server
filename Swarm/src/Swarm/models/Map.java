package Swarm.models;

import Swarm.objects.*;
import network.Json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import Swarm.map.Cell;

/**
 * Created by vahid
 */
public class Map implements Serializable {
    private String mapName;
    private int turn = 0;
    private int h, w;
    private int idCounter = 0;
    private Cell[][] cells;
    private int[][] initialTrashes;
    private int[][] initialNets;
    private int[][] initialTeleports;
    private int[][] initialFoods;
    private int[][] initialFishes;

    private double[] initialConstants;

    private int[] score;


    private ArrayList<Teleport> teleports = new ArrayList<>();
    private ArrayList<Fish>[] fishes;

    private ArrayList<GameObject> tempObjects = new ArrayList<>();
    private GameConstants constants = new GameConstants();



    public Map() {
    }

    public Map(File mapFile) {
        this.mapName = mapFile.getName();

        try {
            MapJson mapJson = Json.GSON.fromJson(new FileReader(mapFile), MapJson.class);
            this.w = mapJson.w;
            this.h = mapJson.h;
            cells = new Cell[h][w];

            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    cells[i][j] = new Cell();
                    cells[i][j].setRow(i);
                    cells[i][j].setColumn(j);
                }
            }
            this.score = new int[2];

            makeConstants(mapJson.constants);
            makeFish(mapJson.fishes);
            makeFood(mapJson.foods);
            makeTrash(mapJson.trashes);
            makeNets(mapJson.nets);
            makeTeleport(mapJson.teleports);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class MapJson {
        private int w;
        private int h;
        private int[][] fishes;
        private int[][] foods;
        private int[][] trashes;
        private int[][] teleports;
        private int[][] nets;
        private double[] constants;
    }



    private void makeConstants(double[] constants) {
        initialConstants = new double[24];
        for (int i = 0; i < 21; i++) {
            initialConstants[i] = constants[i];
        }
        GameConstants gameConstants = new GameConstants();
        gameConstants.setTurnTimeout((int) constants[0]);
        gameConstants.setFoodProb(constants[1]);
        gameConstants.setTrashProb(constants[2]);
        gameConstants.setNetProb(constants[3]);
        gameConstants.setNetValidTime((int) constants[4]);
        gameConstants.setColorCost((int) constants[5]);
        gameConstants.setSickCost((int) constants[6]);
        gameConstants.setUpdateCost((int) constants[7]);
        gameConstants.setDetMoveCost((int) constants[8]);
        gameConstants.setKillQueenScore((int) constants[9]);
        gameConstants.setKillBothQueenScore((int) constants[10]);
        gameConstants.setKillFishScore((int) constants[11]);
        gameConstants.setQueenCollisionScore((int) constants[12]);
        gameConstants.setFishFoodScore((int) constants[13]);
        gameConstants.setQueenFoodScore((int) constants[14]);
        gameConstants.setSickLifeTime((int) constants[15]);
        gameConstants.setPowerRatio((int) constants[16]);
        gameConstants.setEndRatio(constants[17]);
        gameConstants.setDisobeyNum((int) constants[18]);
        gameConstants.setFoodValidTime((int) constants[19]);
        gameConstants.setTrashValidTime((int) constants[20]);
        if(constants.length == 21) {
            gameConstants.setTotalTurn(600);
            initialConstants[21] = 600;
            gameConstants.setDetMovLimit(50);
            initialConstants[22] = 50;
            gameConstants.setChangeColorLimit(50);
            initialConstants[23] = 50;
        }
        if(constants.length == 22) {
            gameConstants.setTotalTurn((int) constants[21]);
            initialConstants[21] = constants[21];
            gameConstants.setDetMovLimit(50);
            initialConstants[22] = 50;
            gameConstants.setChangeColorLimit(50);
            initialConstants[23] = 50;
        }
        if(constants.length == 23) {
            gameConstants.setTotalTurn((int) constants[21]);
            initialConstants[21] = constants[21];
            gameConstants.setDetMovLimit((int) constants[22]);
            initialConstants[22] = constants[22];
            gameConstants.setChangeColorLimit(50);
            initialConstants[23] = 50;
        }
        if(constants.length == 24) {
            gameConstants.setTotalTurn((int) constants[21]);
            initialConstants[21] = constants[21];
            gameConstants.setDetMovLimit((int) constants[22]);
            initialConstants[22] = constants[22];
            gameConstants.setChangeColorLimit((int) constants[23]);
            initialConstants[23] = constants[23];
        }

        this.constants = gameConstants;

    }

    private void makeFish(int[][] fishes) {

        initialFishes = fishes;

        idCounter += fishes.length;
        this.fishes = new ArrayList[2];
        for (int i = 0; i < 2; i++) {
            this.fishes[i] = new ArrayList<>();
        }

        for (int i = 0; i < fishes.length; i++) {

            boolean bQueen;
            boolean bSick;
            if (fishes[i][5] == 1) {
                bQueen = true;
            } else {
                bQueen = false;
            }

            if (fishes[i][6] == 1) {
                bSick = true;
            } else {
                bSick = false;
            }

            Fish fish = new Fish(fishes[i][0], cells[fishes[i][1]][fishes[i][2]], fishes[i][7], fishes[i][3], fishes[i][4], bSick, bQueen);

            if (bSick) {
                fish.setDeadTime(this.constants.getSickLifeTime());
            }

            this.fishes[fish.getTeamNumber()].add(fish);

            cells[fishes[i][1]][fishes[i][2]].setContent(fish);

        }

    }

    private void makeTeleport(int[][] teleports) {

        idCounter += teleports.length;

        initialTeleports = teleports;


        HashMap<Integer, Integer> idPair = new HashMap<>();

        for (int i = 0; i < teleports.length; i++) {

            Teleport teleport1 = new Teleport(teleports[i][0], cells[teleports[i][1]][teleports[i][2]]);

            idPair.put(teleports[i][0], teleports[i][3]);

            this.teleports.add(teleport1);

            cells[teleports[i][1]][teleports[i][2]].setTeleport(teleport1);
        }

        for (int i = 0; i < teleports.length; i++) {

            int id = this.teleports.get(i).getId();

            for (int j = 0; j < teleports.length; j++) {

                if (idPair.get(id).equals(this.teleports.get(j).getId())) {
                    this.teleports.get(i).setPair(this.teleports.get(j).getPosition());
                }
            }
        }
    }

    private void makeNets(int[][] nets) {

        initialNets = nets;

        idCounter += nets.length;

        for (int i = 0; i < nets.length; i++) {

            Net net = new Net(nets[i][0], cells[nets[i][1]][nets[i][2]]);

            net.setDeadTime(constants.getNetValidTime());

            this.tempObjects.add(net);

            cells[nets[i][1]][nets[i][2]].setNet(net);

        }
    }

    private void makeTrash(int[][] trashes) {

        initialTrashes = trashes;

        idCounter += trashes.length;

        for (int i = 0; i < trashes.length; i++) {

            Trash trash = new Trash(trashes[i][0], cells[trashes[i][1]][trashes[i][2]]);

            trash.setDeadTime(this.constants.getTrashValidTime());

            this.tempObjects.add(trash);

            cells[trashes[i][1]][trashes[i][2]].setContent(trash);

        }
    }

    private void makeFood(int[][] foods) {

        initialFoods = foods;
        idCounter += foods.length;

        for (int i = 0; i < foods.length; i++) {

            Food food = new Food(foods[i][0], cells[foods[i][1]][foods[i][2]]);

            food.setDeadTime(this.constants.getFoodValidTime());

            this.tempObjects.add(food);

            cells[foods[i][1]][foods[i][2]].setContent(food);

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


    public GameConstants getConstants() {
        return constants;
    }


    public ArrayList<Teleport> getTeleports() {
        return teleports;
    }


    public int[][] getInitialFishes() {
        return initialFishes;
    }

    public int[][] getInitialTrashes() {
        return initialTrashes;
    }

    public int[][] getInitialNets() {
        return initialNets;
    }

    public int[][] getInitialTeleports() {
        return initialTeleports;
    }

    public int[][] getInitialFoods() {
        return initialFoods;
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


    public int getIdCounter() {
        return idCounter;
    }


    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public double[] getInitialConstants() {
        return initialConstants;
    }

    public void setInitialConstants(double[] initialConstants) {
        this.initialConstants = initialConstants;
    }


}
