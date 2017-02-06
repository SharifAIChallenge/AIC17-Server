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
 * Created by pezzati on 1/27/16.
 */
public class Map implements Serializable{
    private String mapName;
    private int turn;
    private int h,w;
    private int idCounter = 0;
    private Cell[][] cells;
    private int[][] initialTrashes;
    private int[][] initialNets;
    private int[][] initialTeleports;
    private int[][] initialFoods;
    private int[][] initialFishes;
    private int[] score;


    private ArrayList<Teleport> teleports = new ArrayList<>();
    private ArrayList<Fish>[] fishes ;
    private ArrayList<GameObject> tempObjects = new ArrayList<>();
    private GameConstants constants = new GameConstants();



    public Map(){
    }

    public Map(File mapFile) {
        this.mapName = mapFile.getName();


        try {
            MapJson mapJson = Json.GSON.fromJson(new FileReader(mapFile), MapJson.class);
            this.w = mapJson.w;
            this.h = mapJson.h;

            cells= new Cell[w][h];
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    cells[i][j] = new Cell();
                    cells[i][j].setRow(i);
                    cells[i][j].setColumn(j);
                }
            }
            this.constants = makeConstants(mapJson.constants);
            this.score = new int[2];

            makeFish(mapJson.fishes);
            makeFood(mapJson.foods);
            makeTrash(mapJson.trashes);
            makeNets(mapJson.nets);

            makeTeleport(mapJson.teleports);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private GameConstants makeConstants(int[] constants) {

        GameConstants gameConstants = new GameConstants();
        gameConstants.setTurnTimeout(constants[0]);
        gameConstants.setFoodProb(constants[1]);
        gameConstants.setTrashProb(constants[2]);
        gameConstants.setNetProb(constants[3]);
        gameConstants.setNetValidTime(constants[4]);
        gameConstants.setColorCost(constants[5]);
        gameConstants.setSickCost(constants[6]);
        gameConstants.setUpdateCost(constants[7]);
        gameConstants.setDetMoveCost(constants[8]);
        gameConstants.setKillQueenScore(constants[9]);
        gameConstants.setKillBothQueenScore(constants[10]);
        gameConstants.setKillFishScore(constants[11]);
        gameConstants.setQueenCollisionScore(constants[12]);
        gameConstants.setFishFoodScore(constants[13]);
        gameConstants.setQueenFoodScore(constants[14]);
        gameConstants.setSickLifeTime(constants[15]);
        gameConstants.setPowerRatio(constants[16]);
        gameConstants.setEndRatio(constants[17]);
        gameConstants.setDisobeyNum(constants[18]);
        gameConstants.setFoodValidTime(constants[19]);
        gameConstants.setTrashValidTime(constants[20]);
        return gameConstants;

    }


    private class MapJson {
        private int  w;
        private int h;
        private int[][] fishes;
        private int[][] foods;
        private int[][] trashes;
        private int[][] teleports;
        private int[][] nets;
        private int[] constants;
    }


    private void makeFish(int[][] fishes) {

        initialFishes = fishes;
        idCounter += fishes.length;
        this.fishes = new ArrayList[this.constants.getTeamNum()];
        for (int i = 0; i < this.constants.getTeamNum(); i++) {
            this.fishes[i] = new ArrayList<>();
        }

        /*
        [id, x, y, direction, color, queen, sick, team]
         */
        for (int i = 0; i < fishes.length; i++) {
            boolean b = false;
            if(fishes[i][5] == 1){
                b = true;
            }


            Fish fish = new Fish(fishes[i][0],cells[fishes[i][1]][fishes[i][2]], fishes[i][7],fishes[i][3],fishes[i][4],b);

            this.fishes[fish.getTeamNumber()].add(fish);

            cells[fishes[i][1]][fishes[i][2]].setContent(fish);

        }

    }

    private void makeTeleport(int[][] teleports) {

        initialTeleports = teleports;
        idCounter+=teleports.length;

        HashMap<Integer,Integer> idPair = new HashMap<>();

        for (int i = 0; i < teleports.length; i++) {

            Teleport teleport1 = new Teleport(teleports[i][0],cells[teleports[i][1]][teleports[i][2]]);
            idPair.put(teleports[i][0],teleports[i][3]);
            this.teleports.add(teleport1);
            cells[teleports[i][1]][teleports[i][2]].setTeleport(teleport1);
        }
        for (int i = 0; i < teleports.length; i++) {
            int id = this.teleports.get(i).getId();
            for (int j = 0; j < teleports.length; j++) {
                if(idPair.get(id).equals(this.teleports.get(j).getId())){
                    this.teleports.get(i).setPair(this.teleports.get(j).getPosition());
                }
            }
        }
    }

    private void makeNets(int[][] nets) {

        initialNets = nets;
        idCounter+=nets.length;

        for (int i = 0; i < nets.length; i++) {
            Net net = new Net(nets[i][0],cells[nets[i][1]][nets[i][2]]);

            this.tempObjects.add(net);

            cells[nets[i][1]][nets[i][2]].setNet(net);

        }
    }

    private void makeTrash(int[][] trashes) {

        initialTrashes = trashes;
        idCounter+=trashes.length;

        for (int i = 0; i < trashes.length; i++) {
            Trash trash = new Trash(trashes[i][0],cells[trashes[i][1]][trashes[i][2]]);

            this.tempObjects.add(trash);
            cells[trashes[i][1]][trashes[i][2]].setContent(trash);

        }
    }

    private void makeFood(int[][] foods) {
        initialFoods = foods;
        idCounter+=foods.length;

        for (int i = 0; i < foods.length; i++) {

            Food food = new Food(foods[i][0],cells[foods[i][1]][foods[i][2]]);
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

    public void setTempObjects(ArrayList<GameObject> tempObjects) {
        this.tempObjects = tempObjects;
    }

    public GameConstants getConstants() {
        return constants;
    }

    public void setConstants(GameConstants constants) {
        this.constants = constants;
    }

    public ArrayList<Teleport> getTeleports() {
        return teleports;
    }

    public void setTeleports(ArrayList<Teleport> teleports) {
        this.teleports = teleports;
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

    public void setMapName(String mapName) {
        this.mapName = mapName;
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

    public void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
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




}
