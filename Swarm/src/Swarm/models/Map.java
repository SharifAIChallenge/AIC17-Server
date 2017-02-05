package Swarm.models;

import Swarm.objects.Fish;
import Swarm.objects.Food;
import Swarm.objects.GameObject;
import Swarm.objects.Trash;
import debugUI.paintIt.MapPanel;
import network.Json;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;

import Swarm.map.Cell;

import javax.swing.*;

/**
 * Created by pezzati on 1/27/16.
 */
public class Map {
    private int h,w;
    private ArrayList<Fish> fishes = new ArrayList<>();
    private ArrayList<GameObject> contents = new ArrayList<>();

    private int[] score = new int[2];

    public ArrayList<Fish> getFishes() {
        return fishes;
    }

    public void setFishes(ArrayList<Fish> fishes) {
        this.fishes = fishes;
    }

    public ArrayList<GameObject> getContents() {
        return contents;
    }

    public void setContents(ArrayList<GameObject> contents) {
        this.contents = contents;
    }

    private String mapName;
    private Cell[][] cells;

    public Cell[][] getCells(){
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
            Fish fish = new Fish(fishes[i][0],cells[fishes[i][1]][fishes[i][2]], fishes[i][7],fishes[i][3],b,fishes[i][4]);
            this.fishes.add(fish);
            cells[fishes[i][1]][fishes[i][2]].setContent(fish);

        }

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
                }
            }

            makeFish(mapJson.fishes);
            makeFood(mapJson.foods);
            makeTrash(mapJson.trashes);
            /*
            makeTeleport(mapJson.teleports);
            makeConstant(mapJson.constants);
            */

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Map(){
        Cell cells[][] = new Cell[20][20];
        Fish fish1 = new Fish(1, cells[0][0], 0, 2, false, 0);
        Fish fish2 = new Fish(1, cells[0][0], 0, 3, false, 0);
        Fish fish3 = new Fish(1, cells[0][0], 0, 0, false, 0);
        for(int i = 0; i<20; i++)
            for(int j = 0; j<20; j++)
                cells[i][j] = new Cell(fish1, false,false);
        //cells[0][0] = new Cell(fish1, true, false);
        //cells[0][1] = new Cell(null, false, false);
        //cells[1][0] = new Cell(fish2, false, false);
        //cells[1][1] = new Cell(fish3, true, false);
        this.setCells(cells);
        this.setW(cells.length);
        this.setH(cells[0].length);
        this.setMapName("test_map");
    }

    private void makeTrash(int[][] trashes) {
        for (int i = 0; i < trashes.length; i++) {
            Trash trash = new Trash(trashes[i][0],cells[trashes[i][1]][trashes[i][2]]);
            this.contents.add(trash);
            cells[trashes[i][1]][trashes[i][2]].setContent(trash);

        }
    }

    private void makeFood(int[][] foods) {

        for (int i = 0; i < foods.length; i++) {
            Food food = new Food(foods[i][0],cells[foods[i][1]][foods[i][2]]);
            this.contents.add(food);
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

    public static void main(String[] args) {
        Map map = new Map();
        System.out.println(map.getH());
        System.out.println(map.getW());
        MapPanel mapPanel = new MapPanel(map);
        JFrame frame = new JFrame();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(mapPanel);
        frame.revalidate();
        frame.pack();
        ((Fish)map.getCells()[0][0].getContent()).setDirection(3);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mapPanel.setMap(map);
        mapPanel.gameOver();
        //System.out.println(map.getH());
    }

}
