package debugUI.paintIt;

import Swarm.map.Cell;
import Swarm.models.Map;
import Swarm.objects.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by miladink on 2/8/17.
 */
public class MapJsonExtra {
    private int  w;
    private int h;
    ArrayList<ArrayList<Integer>> fishes = new ArrayList<>();
    ArrayList<ArrayList<Integer>> foods = new ArrayList<>();
    ArrayList<ArrayList<Integer>> trashes = new ArrayList<>();
    ArrayList<ArrayList<Integer>> teleports = new ArrayList<>();
    ArrayList<ArrayList<Integer>>nets = new ArrayList<>();
    ArrayList<Double>constants = new ArrayList<>();

    public MapJsonExtra(Map map) {
        Cell cells[][] = map.getCells();
        w = map.getW();
        h = map.getH();
        for(int i = 0; i<map.getH(); i++)
            for(int j = 0; j<map.getW(); j++){
                Cell cell = cells[i][j];
                GameObject content = cell.getContent();
                if(content!= null){
                    if(content instanceof Fish){
                        Fish temp = (Fish)content;
                        ArrayList<Integer> fish = new ArrayList<>();
                        fish.add(temp.getId());
                        fish.add(temp.getPosition().getRow());
                        fish.add(temp.getPosition().getColumn());
                        fish.add(temp.getDirection());
                        fish.add(temp.getColorNumber());
                        fish.add(temp.isQueen()?1:0);
                        fish.add(temp.isSick()?1:0);
                        fish.add(temp.getTeamNumber());
                        fishes.add(fish);
                    }else if(content instanceof Food){
                        Food temp = (Food)content;
                        ArrayList<Integer> food = new ArrayList<>();
                        food.add(temp.getId());
                        food.add(temp.getPosition().getRow());
                        food.add(temp.getPosition().getColumn());
                        foods.add(food);
                    }else if(content instanceof Trash){
                        Trash temp = (Trash)content;
                        ArrayList<Integer> trash = new ArrayList<>();
                        trash.add(temp.getId());
                        trash.add(temp.getPosition().getRow());
                        trash.add(temp.getPosition().getColumn());
                        trashes.add(trash);
                    }

                }
                if(cell.getNet()!=null){
                    Net temp = cell.getNet();
                    ArrayList<Integer> net = new ArrayList<>();
                    net.add(temp.getId());
                    net.add(temp.getPosition().getRow());
                    net.add(temp.getPosition().getColumn());
                    nets.add(net);
                }
                if(cell.getTeleport()!=null){
                    Teleport temp = cell.getTeleport();
                    ArrayList<Integer> teleport = new ArrayList<>();
                    teleport.add(temp.getId());
                    teleport.add(temp.getPosition().getRow());
                    teleport.add(temp.getPosition().getColumn());
                    teleport.add(temp.getPair().getTeleport().getId());
                    teleports.add(teleport);
                }
            }
        //TODO:ui for constants
        constants.add(500.0);//turnTimeOut
        constants.add(0.005);//foodProb
        constants.add(0.003);//trashProb
        constants.add(0.001);//netProb
        constants.add(3.0);//netValidTime
        constants.add(30.0);//color cost
        constants.add(50.0);//sick cost
        constants.add(40.0);//update cost
        constants.add(300.0);//detMovCost
        constants.add(1500.0);//killQueenScore
        constants.add(1000.0);//killBothQueenScore
        constants.add(300.0);//killFishScore
        constants.add(600.0);//queenCollisionScore
        constants.add(15.0);//fishFoodScore
        constants.add(25.0);//queenFoodScore
        constants.add(10.0);//sickLifeTime
        constants.add(2.0);// powerRatio,
        constants.add(70.0);//endRatio
        constants.add(1.0);// disobeyNum//TODO:for debug turn it off
        constants.add(6.0);// foodValidTime
        constants.add(10.0);// trashValidTime]
    }
}
