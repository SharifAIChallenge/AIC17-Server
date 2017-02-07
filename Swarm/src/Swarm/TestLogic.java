package Swarm;

import Swarm.models.Diff;
import Swarm.objects.*;
import debugUI.paintIt.MapFrame;
import debugUI.paintIt.MapPanel;
import model.Event;
import Swarm.models.GameConstants;
import Swarm.models.Map;
import network.data.Message;
import server.config.BooleanParam;
import server.config.Configs;
import server.config.FileParam;
import server.config.IntegerParam;
import server.core.GameLogic;
import server.core.GameServer;
import Swarm.map.Cell;
import util.Log;

import javax.swing.*;
import java.util.*;


/**
 * Created by pezzati on 1/28/16.
 */
public class TestLogic {
    private static final String TAG = "Swarn";

    /**
     //swarm Constatnts :::
     private int mapSize;
     private int teamNum = 2;
     private int teleportNum;
     private int initFishNum;
     private int initQueenNum;

     private double foodProb;
     private double trashProb;
     private double netProb;

     private int netActiveTime;
     private int netValidTime;

     //

     private int changeColorLimit;
     private int kStep;
     // or :
     private int colorCost;
     //
     private int sickCost;
     private int UpdateCost;
     private int detMoveCost;

     private int killQueenScore;
     private int killFishScore;
     private int queenCollisionScore;
     private int fishFoodScore;
     private int queenFoodScore;
     private int sickLifeTime;

     private int powerRatio;
     private double endRatio;

     private int disobeyPointerTeam0;
     private int disobeyPointerTeam1;
     private int disobeyNum;
     private double disobeyRatio;

     private int foodValidTime;
     private int trashValidTime;
     */
//////////
//    Diff diff = new Diff();
    private int idCounter;
    private int H,W;
    Map map;
    GameConstants gc;
    int[][][][][] update; // teamNum, colorNum, right, head, left

    int[] rowHeadDir = new int[]{0, -1,  0, 1};
    int[] colHeadDir = new int[]{1,  0, -1, 0};

    int[] rowLeftDir = new int[]{-1, -1,  1, 1};
    int[] colLeftDir = new int[]{ 1, -1, -1, 1};

    int[] rowRightDir = new int[]{1, -1,  -1,  1};
    int[] colRightDir = new int[]{1,  1,  -1, -1};

    private ArrayList <Fish>[] fishes;
    private ArrayList <GameObject> tempObjects;
    private ArrayList <Teleport> teleports;
    ArrayList<Cell>[] nextCell;
    //HashMap<Integer,Integer> detMoves = new HashMap<>();
    ArrayList<Event>[] detMoves;
    private Cell[][] cells;
    private ArrayList<Fish>[][][] attacks;
    private boolean[][][] newBorn;
    private Fish[][][] motherFish;

    private int score[];
    private int numberOfQueens[];
    HashMap<Integer, String> fishChanges;
    HashSet<Integer> fishAlters = new HashSet<>();
    private int turn = -1;

    public static void main(String[] args) throws InterruptedException {
        TestLogic lg = new TestLogic();

        lg.init();

        lg.simulateEvents();

        lg.simulateEvents();
    }

    public void init() {
        //this.map = new Map(PARAM_MAP.getValue());
        idCounter = 1;
        H = 5;
        W = 5;
        //
        cells = new Cell[H][W];
        for(int i=0;i<H;i++){
            for(int j=0;j<W;j++){
                cells[i][j] = new Cell(i,j,null,null,null);
            }
        }

        // fishes
        fishes = new ArrayList[2];
        fishes[0] = new ArrayList<>();
        fishes[1] = new ArrayList<>();

        //public Fish(int id, Cell position, int teamNumber, int direction, int colorNumber, boolean queen) {
        fishes[0].add(new Fish(idCounter++, cells[0][0], 0, 1, 0, false));
        cells[0][0].setContent(fishes[0].get(0));

        fishes[0].add(new Fish(idCounter++, cells[1][0], 0, 2, 0, false));
        cells[1][0].setContent(fishes[0].get(1));

        // FOOD & TRASH
        tempObjects = new ArrayList<>();
        tempObjects.add(new Trash(idCounter++, cells[2][0]));
        tempObjects.add(new Food(idCounter++, cells[0][1]));
        cells[2][0].setContent(tempObjects.get(0));
        cells[0][1].setContent(tempObjects.get(1));

        teleports = new ArrayList<>();

        gc = new GameConstants();
        score = new int[2];
        score[0] = score[1] = 100;
        initialize();

    }


    private void initialize(){

        update = new int[gc.getTeamNum()][2][3][2][3];
        //Arrays.fill(update, 1);
        for(int ind =0 ;ind<2;ind++){
            for(int c=0;c<2;c++){
                for(int i=0;i<3;i++){
                    for(int j=0;j<2;j++){
                        for(int k=0;k<3;k++){
                            update[ind][c][i][j][k] = 1;
                        }
                    }
                }
            }
        }

        fishChanges = new HashMap<>();
        fishAlters = new HashSet<>();
        attacks = new ArrayList[H][W][2];
        //this.numberOfQueens =  new int[gc.getTeamNum()];
        //this.update = new int[gc.getTeamNum()][2][3][2][3];

        for(int i=0;i<H;i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind] = new ArrayList<>();
                }
            }
        }

        detMoves = new ArrayList[2];
        detMoves[0] = new ArrayList<>();
        detMoves[1] = new ArrayList<>();

        nextCell = new ArrayList[2];
        nextCell[0] = new ArrayList<>();
        nextCell[1] = new ArrayList<>();

    }

    public void simulateEvents(/*Event[] environmentEvent, Event[][] clientsEvent*/) {
        //map.setTurn(map.getTurn()+1);

//        diff = new Diff();

        nextCell[0].clear();
        nextCell[1].clear();

        detMoves[0].clear();
        detMoves[1].clear();
        //detMoves = new ArrayList[2];
        //detMoves[0] = new ArrayList<>();
        //detMoves[1] = new ArrayList<>();

        for(int i=0;i<H;i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind].clear();
                }
            }
        }

        newBorn = new boolean[gc.getTeamNum()][H][W];
        motherFish = new Fish[gc.getTeamNum()][H][W];

        fishChanges.clear();
        fishAlters.clear();


        // NEXTCELL ACCORDING TO FISHES
        for(int ind=0;ind<gc.getTeamNum();ind++){
            for(int i=0;i<fishes[ind].size();i++){
                int curId = fishes[ind].get(i).getId();
                Cell cell = getNextCellViaUpdate(fishes[ind].get(i));
                nextCell[ind].add(cell);
                //System.out.println(cell.getRow() + " " + cell.getColumn());
                /**
                 * check
                 */
            }
        }

        // DESTINATIONS
        /**
         * not checked
         */
        for(int ind=0;ind<gc.getTeamNum();ind++){
            for(int i=0; i<detMoves[ind].size(); i++){
                /**
                 * handle correct type
                 */
                int id = Integer.parseInt(detMoves[ind].get(i).getArgs()[0]),
                        mv = Integer.parseInt(detMoves[ind].get(i).getArgs()[1]);
                for(int j=0;j<fishes[ind].size();j++){
                    if(fishes[ind].get(j).getId() == id){
                        nextCell[ind].set(j, getNextCellViaMove(fishes[ind].get(j), mv));
                        //score[ind] -= gc.getDetMoveCost();
                        changeScores(ind, gc.getDetMoveCost());
                    }
                }
            }
        }

        // ATTACKS !!!

        for(int i=0;i<H;i++){
            for(int j=0;j<W;j++) {
                // move fishes
                /**
                 * for each cell compare summation of each team power
                 * if it was more than twice of the other team remove all of them
                 * and fix scores/
                 */
                int[] powerSum = new int[2];
                boolean[] uniqueAttacker = new boolean[2];
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                        Fish fish = attacks[i][j][ind].get(k);
                        powerSum[ind] += fish.getPower();
                    }
                    /**
                     * fixed
                     */
                }
                int[] queens = new int[2];
                Fish[] lastNormalFish = new Fish[2];

                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                        Fish fish = attacks[i][j][ind].get(k);
                        /**
                         * CALC cost of queen in both cells ??
                         * BUG
                         * BUG
                         */
                        if (fish.isQueen() && attacks[i][j][1-ind].size() > 0 && !fishChanges.containsKey(fish.getId())) {
                            fishChanges.put(fish.getId(), "delete");
                            queens[ind]++;
                            // handle later
                            //score[ind] -= gc.getKillQueenScore();
                        }
                        else if (!fish.isQueen()){
                            lastNormalFish[ind]=fish;
                        }
                    }
                }
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    if (powerSum[1-ind] > 2 * powerSum[ind] || uniqueAttacker[1-ind]) {
                        /**
                         * fixed
                         */
                        //System.out.println(uniqueAttacker[0] + " u " + uniqueAttacker[1]);
                        for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                            Fish fish =attacks[i][j][ind].get(k);
                            if(!fish.isQueen() && !fishChanges.containsKey(fish.getId())){
                                fishChanges.put(fish.getId(), "delete");
                                //score[ind] -= gc.getKillFishScore();
                                changeScores(ind, gc.getKillFishScore());
                            }
                        }
                        //System.out.println(attacks[i][j][1-ind].size()-queens[1-ind]);
                        //System.out.println(lastNormalFish[1-ind].getPosition().getRow() + " " +
                        //    lastNormalFish[1-ind].getPosition().getColumn());
                        if((attacks[i][j][1-ind].size()-queens[1-ind])==1){
                            if( !fishChanges.containsKey(lastNormalFish[1-ind].getId()));
                            /**
                             * fixed
                             */
                            fishChanges.put(lastNormalFish[1-ind].getId(), "move");
                            //System.out.println(lastNormalFish[1-ind].getId());
                        }
                    }
                }
                if(queens[0]>0 && queens[1]>0){
                    //int[] tempA = {map.getScore()[0]+gc.getKillBothQueenScore(),map.getScore()[1]+gc.getKillBothQueenScore()};
                    //map.setScore(tempA);
                    score[0] += gc.getKillBothQueenScore();
                    score[1] += gc.getKillBothQueenScore();
                }
                else if( (queens[0]==0 && queens[1]>0)){
                    changeScores(1,gc.getKillQueenScore());
                }
                else if( (queens[1]==0 && queens[0]>0)){
                    changeScores(0,gc.getKillQueenScore());
                }

            }
        }
        // DELETION && MOVES
        for(int ind=0;ind<gc.getTeamNum();ind++) {
            for(int i=fishes[ind].size()-1;i>=0;i--) {
                if(fishChanges.containsKey(fishes[ind].get(i).getId())) {
                    String str = fishChanges.get(fishes[ind].get(i).getId());
                    Fish fish = fishes[ind].get(i);
                    if(str.equals("delete")) {
                        fishes[ind].remove(i);
                        nextCell[ind].remove(i);
                        // queens --
                        if(fish.isQueen())
                            numberOfQueens[ind]--;
                        //DIFF.delete();
//                        diff.del(fish.getId());
                        // cell
                        /**
                         * handle null
                         */
                        fish.getPosition().setContent(null);
                    }
                    else if(str.equals("move")) {
                        ///// define nextCell of fish & content of oldCell
                        boolean moved = false;
                        if(!fish.getPosition().equals(nextCell[ind].get(i))) {
                            moved = true;
                            fish.setPower(fish.getPower()+1);
                        }
                        //ATTENTION TARTIB MOHEM DAR OBJECT CELL
                        if(fish.equals(fish.getPosition().getContent()) && moved) {
                            fish.getPosition().setContent(null); /// NULL
                        }
                        //////////////// if fish is pregnant add new baby on old position
                        if(moved && fish.isPregnant()) {
                            newBorn[fish.getTeamNumber()][fish.getPosition().getRow()][fish.getPosition().getColumn()] = true;
                            motherFish[fish.getTeamNumber()][fish.getPosition().getRow()][fish.getPosition().getColumn()] = fish;
                            fish.setPregnant(false);
                        }
                        // NEW POSITION
                        fish.setPosition(nextCell[ind].get(i));
                        ////// define changes after fish moved
                        // set sick & deadtime & fishAlters & trash remove & diff trash
                        if(nextCell[ind].get(i).getContent() instanceof Trash && !fish.isSick()) {
                            Trash trash = (Trash) nextCell[ind].get(i).getContent();
                            /**
                             * turn
                             */
                            fish.setSick(true);
                            fish.setDeadTime(turn + gc.getSickLifeTime());

                            fishAlters.add(fish.getId());

//                            diff.del(trash.getId());
                            tempObjects.remove(trash);
                        }
                        else if(nextCell[ind].get(i).getContent() instanceof Food ) {
                            Food food = (Food) nextCell[ind].get(i).getContent();

                            if(fish.isQueen()) {
                                int tempA[] = new int[2];
                                //tempA[ind] = map.getScore()[ind]+gc.getQueenFoodScore();
                                //tempA[1-ind] = map.getScore()[1-ind];
                                //map.setScore(tempA);
                                score[ind] += gc.getQueenFoodScore();
                            }
                            else {
                                int tempA[] = new int[2];
                                //tempA[ind] = map.getScore()[ind]+gc.getFishFoodScore();
                                //tempA[1-ind] = map.getScore()[1-ind];
                                //map.setScore(tempA);
                                //score[ind] += gc.getFishFoodScore();
                            }
                            fish.setPregnant(true);

//                            diff.del(food.getId());
                            tempObjects.remove(food);
                            // DIF FOOD DELETE
                        }
                        nextCell[ind].get(i).setContent(fish);
                        // DIFF.move
                        /**
                         * mv r dir
                         */
//                        diff.mov(fish.getId(),fish.getDirection());
                    }
                }
            }
        }


        /// NEW BORN
        for(int ind=0;ind<gc.getTeamNum();ind++) {
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    /**
                     * == or .equals
                     */
                    if (newBorn[ind][i][j] && cells[i][j].getContent() == null) {
                        makeBabyFish(cells[i][j], motherFish[ind][i][j]);
                    }
                }
            }
        }

        // NET DEATH

        for(int i=0;i<H;i++){
            for(int j=0;j<W;j++){
                // NET death just for fish ?
                if(cells[i][j].getContent() instanceof Fish) {
                    if(isNetDeadTime(cells[i][j])){
                        Fish fish = (Fish) cells[i][j].getContent();
                        fishes[fish.getTeamNumber()].remove(fish);
//                        diff.del(fish.getId());
                        cells[i][j].setContent(null);
                    }

                }
            }
        }

        // SICK DEATH

        for(int ind=0; ind<gc.getTeamNum(); ind++){
            for(int i=0;i<fishes[ind].size();i++){
                if(fishes[ind].get(i).getDeadTime() == turn) {
                    Fish fish = fishes[ind].get(i);
                    fishes[fish.getTeamNumber()].remove(fish);
//                    diff.del(fish.getId());
                    cells[fish.getPosition().getRow()][fish.getPosition().getColumn()].setContent(null);
                }
            }
        }

        // FOOD & TRASH & NET

        for(int i=0; i<tempObjects.size(); i++) {
            if(tempObjects.get(i).getDeadTime() == turn) {
                tempObjects.remove(i);
//                diff.del(tempObjects.get(i).getId());
                cells[tempObjects.get(i).getPosition().getRow()][tempObjects.get(i).getPosition().getColumn()].setContent(null);
            }
        }
        //// OUTTTTTTTTTTTTTTTT
        for(int ind=0;ind<2;ind++){
            for(int i=0;i<fishes[ind].size();i++){
                Fish fish = fishes[ind].get(i);
                System.out.println(fish.getPosition().getRow() + " " + fish.getPosition().getColumn()
                        + " " + fish.getDirection() + " " + fish.isSick() + " " +
                        fish.isPregnant());
            }
        }
        for(int i=0;i<tempObjects.size();i++){
            System.out.println(tempObjects.get(i).getPosition().getRow() + " " +
                    tempObjects.get(i).getPosition().getColumn());
        }
        System.out.println();
        /**
        for(int i=0;i<H;i++){
            for(int j=0;j<W;j++){
                if(cells[i][j].getContent() instanceof Fish){
                    System.out.print("F"+((Fish) cells[i][j].getContent()).getDirection());
                }
                System.out.print(cells[i][j].getContent() + " ");
            }
            System.out.println();
        }
        */

        // ADD RANDOM THINGS

        // update power

        //uiMessage = new Message(Message.NAME_TURN, uiMessages.toArray());
    }

    private boolean isNetDeadTime(Cell cell){
        int row = cell.getRow(),neighbourRow;
        int col = cell.getColumn(),neighbourCol;
        for(int i=-1;i<=1;i++){
            for(int j=-1;j<=1;j++){
                /**
                 * getDeadTime == turn is true ?
                 */
                neighbourRow = makeValidIndex(row+i,H);
                neighbourCol = makeValidIndex(col+j,W);
                if(cells[neighbourRow][neighbourCol].getNet() != null &&
                        cells[neighbourRow][neighbourCol].getNet().getDeadTime() == turn){
                    return true;
                }
            }
        }

        return false;
    }

    private void makeBabyFish(Cell cell, Fish fish){
        /**
         * new FISH ?
         */
        //public Fish(int id, Cell position, int teamNumber, int direction, int colorNumber, boolean queen);
        Fish baby =new Fish(idCounter++, cell, fish.getTeamNumber(), fish.getDirection(), fish.getColorNumber(), fish.isQueen());
        cell.setContent(baby);
        fishes[fish.getTeamNumber()].add(baby);
        idCounter++;
    }

    private Cell getNextCellViaUpdate(Fish fish){

        int left,right,head,mv;
        int row,col;
        // Right - neighbour
        // 0: ally 1:enemy 2:blank
        row = (fish.getPosition().getRow() + rowRightDir[fish.getDirection()] + H)%H;
        col = (fish.getPosition().getColumn() + colRightDir[fish.getDirection()] + W)%W;
        right = getUpdateINdex(fish, cells[row][col].getContent());

        // left - neighbour
        row = (fish.getPosition().getRow() + rowLeftDir[fish.getDirection()] + H)%H;
        col = (fish.getPosition().getColumn() + colLeftDir[fish.getDirection()] + W)%W;
        left = getUpdateINdex(fish, cells[row][col].getContent());

        // Head

        row = (fish.getPosition().getRow() + rowHeadDir[fish.getDirection()] + H)%H;
        col = (fish.getPosition().getColumn() + colHeadDir[fish.getDirection()] + W)%W;

        while(!(cells[row][col].getContent() instanceof Fish)){
            row = (row + rowHeadDir[fish.getDirection()] + H)%H;
            col = (col + colHeadDir[fish.getDirection()] + W)%W;
            //System.out.println(row + " " + col);
        }
        head = getUpdateINdex(fish, cells[row][col].getContent());
        // delete
        //if(head > 1) head =0; // tofmali :)
        /**
         * is possible that values not be 0 to 2
         */
        // set direction & return next
        mv = update[fish.getTeamNumber()][fish.getColorNumber()][right][head][left];

        return getNextCellViaMove(fish, mv);

    }

    // FISHCHANGES MOVE
    private Cell getNextCellViaMove(Fish fish, int mv){
        int row,col,dir;
        row = fish.getPosition().getRow();
        col = fish.getPosition().getColumn();
        dir = fish.getDirection();
        switch (mv){
            case 0:
                dir = makeValidIndex(dir-1,4);
                break;
            case 1:
                row = (fish.getPosition().getRow() + rowHeadDir[fish.getDirection()] + H)%H;
                col = (fish.getPosition().getColumn() + colHeadDir[fish.getDirection()] + W)%W;
                break;
            case 2:
                dir = makeValidIndex(dir+1,4);

        }
        if(dir != fish.getDirection()){
            fishChanges.put(fish.getId(), "move");
        }
        fish.setDirection(dir);

        //
        Cell destination;
        if(cells[row][col].getContent() instanceof Teleport){
            destination = ((Teleport) cells[row][col].getContent()).getPair();
            row = destination.getRow();
            col = destination.getColumn();
        }

        attacks[row][col][fish.getTeamNumber()].add(fish);

        return cells[row][col];
    }

    private void changeScores(int ind, int val){
        int tempA[] = new int[2];
        //tempA[ind] = map.getScore()[ind] - val;
        //tempA[1-ind] = map.getScore()[1-ind] + val;
        //map.setScore(tempA);
        score[ind]-=val;
        score[1-ind]+=val;
    }
    private int makeValidIndex(int val, int mod){
        return (val+mod)%mod;
    }

    private int getUpdateINdex(Fish fish, GameObject neighbour){

        if(neighbour instanceof Fish){
            if(((Fish) neighbour).getTeamNumber() == fish.getTeamNumber())
                return 0;
            else
                return 1;
        }
        else
            return 2;
    }

}
