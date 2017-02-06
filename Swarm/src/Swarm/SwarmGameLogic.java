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
public class SwarmGameLogic implements GameLogic {
    private static final String TAG = "Swarn";

    private MapFrame debugUI;
    private Event[][] lastClientEvents;

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
    Diff diff = new Diff();
    private int idCounter;
    private int H,W;
    Map map;
    GameConstants gc;
    int[][][][][] update;

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
    private int turn;
    private Message uiMessage;

    public static final IntegerParam PARAM_CLIENT_TIMEOUT = new IntegerParam("ClientTimeout", 500);
    public static final IntegerParam PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", 1000);
    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static final BooleanParam PARAM_SHOW_DEBUG_UI = new BooleanParam("ShowDebugUI", true);

    public static void main(String[] args) throws InterruptedException {
        GameServer gameServer = new GameServer(new SwarmGameLogic(), args);
        System.err.println("start");
        gameServer.start();
        gameServer.waitForFinish();
    }


    @Override
    public int getClientsNum() {
        return 2;
    }

    @Override
    public long getClientResponseTimeout() {
        return PARAM_CLIENT_TIMEOUT.getValue();
    }

    @Override
    public long getTurnTimeout() {
        return PARAM_TURN_TIMEOUT.getValue();
    }

    @Override
    public void init() {
        this.map = new Map(PARAM_MAP.getValue());
        this.H = this.map.getH();
        this.W = this.map.getW();
        this.fishes = this.map.getFishes();
        this.tempObjects = this.map.getTempObjects();
        this.cells = this.map.getCells();
        this.idCounter = this.map.getIdCounter();
        this.teleports = this.map.getTeleports();
        this.gc = this.map.getConstants();
        this.idCounter = this.map.getIdCounter();
        this.score = this.map.getScore();

        initialize();

        if (PARAM_SHOW_DEBUG_UI.getValue() == Boolean.TRUE) {
            debugUI = new MapFrame(this.map);
        }
    }

    /*
    It is remained :getUIInitialMessage
     */
    @Override
    public Message getUIInitialMessage() {
        GameConstants constants = this.map.getConstants();
        constants.setTurnTimeout(PARAM_TURN_TIMEOUT.getValue());

        Message msg;
        String name0 = Message.NAME_INIT;
        Object[] args0 = {0,this.map.getW(),this.map.getH(),this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(),this.map.getInitialTeleports(), constants};
        msg = new Message(name0, args0);
        return msg;
    }

    @Override
    public Message[] getClientInitialMessages() {
        GameConstants constants = this.map.getConstants();
        constants.setTurnTimeout(PARAM_TURN_TIMEOUT.getValue());

        Message[] msg = new Message[2];
        String name0 = Message.NAME_INIT;
        int[] size = {this.map.getW(),this.map.getH()};
        Object[] args0 = {0,size,this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(),this.map.getInitialTeleports(), constants};
        msg[0] = new Message(name0, args0);

        String name1 = Message.NAME_INIT;
        Object[] args1 = {1,size,this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(),this.map.getInitialTeleports(), constants};
        msg[1] = new Message(name1, args1);
        return msg;
    }

    private void initialize(){

        //handle map.getScore()[0];

        fishChanges = new HashMap<>();
        fishAlters = new HashSet<>();

        attacks = new ArrayList[H][W][2];
        this.numberOfQueens =  new int[gc.getTeamNum()];
        this.update = new int[gc.getTeamNum()][2][3][2][3];

        for(int i=0;i<H;i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind] = new ArrayList<>();
                }
            }
        }

        //////////////////
        update = new int[gc.getTeamNum()][2][3][2][3];
        //Arrays.fill(update, 1);
        for(int ind =0 ;ind<2;ind++){
            for(int c=0;c<2;c++){
                for(int i=0;i<3;i++){
                    for(int j=0;j<2;j++){
                        for(int k=0;k<3;k++){
                            update[ind][c][i][j][k] = 0;
                        }
                    }
                }
            }
        }

        detMoves = new ArrayList[2];
        detMoves[0] = new ArrayList<>();
        detMoves[1] = new ArrayList<>();

        nextCell = new ArrayList[2];
        nextCell[0] = new ArrayList<>();
        nextCell[1] = new ArrayList<>();

        numberOfQueens = new int[2];
        for(int ind=0;ind<2;ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                if(fishes[ind].get(i).isQueen())
                    numberOfQueens[ind]++;
            }
        }
        // numberOfQueens[0] ;

    }

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
        map.setTurn(map.getTurn()+1);

        diff = new Diff();

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


        // get argomans and make what we want
        for(int ind=0;ind < gc.getTeamNum(); ind++){
            for(int i=0;i<clientsEvent[ind].length;i++){
                if(clientsEvent[ind][i].getType().equals("s") ){
                    if(clientsEvent[ind][i].getArgs().length != 5) continue;
                    int[] t = new int[5];
                    for(int j=0;j < 5; j++){
                        t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                    }
                    update[ind][t[0]][t[1]][t[2]][t[3]] = t[4];
                    //score[ind] -= gc.getUpdateCost();
                    changeScores(ind, gc.getUpdateCost());
                }
                else if(clientsEvent[ind][i].getType().equals("m") ){
                    /**
                     * handling that the type of args is correct or not
                     */
                    if(clientsEvent[ind][i].getArgs().length != 2) continue;
                    detMoves[ind].add(clientsEvent[ind][i]);
                }
                else if(clientsEvent[ind][i].getType().equals("c") ){
                    if(clientsEvent[ind][i].getArgs().length != 2) continue;
                    int[] t = new int[2];
                    for(int j=0;j < 2; j++){
                        t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                    }
                    for(int j=0; j<fishes[ind].size();j++){
                        /**
                         *  we check containing in fishAlters
                         *  so do not need to get HASHSET
                         */
                        if(fishes[ind].get(j).getId() == t[0] && !fishAlters.contains(fishes[ind].get(j).getId())){
                            fishes[ind].get(j).setColorNumber(t[1]);
                            //score[ind] -= gc.getColorCost();
                            changeScores(ind,gc.getColorCost());
                            fishAlters.add(fishes[ind].get(j).getId());
                        }
                    }
                }
            }
        }

        // NEXTCELL ACCORDING TO FISHES
        for(int ind=0;ind<gc.getTeamNum();ind++){
            for(int i=0;i<fishes[ind].size();i++){
                int curId = fishes[ind].get(i).getId();
                nextCell[ind].add(getNextCellViaUpdate(fishes[ind].get(i)));
            }
        }

        // DESTINATIONS
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
        for(int i=0;i<H;i++) {
            for (int j = 0; j < W; j++) {
                // move fishes
                /**
                 * for each cell compare summation of each team power
                 * if it was more than twice of the other team remove all of them
                 * and fix scores/
                 */
                int[] powerSum = new int[2];
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                        Fish fish = attacks[i][j][ind].get(k);
                        powerSum[ind] += fish.getPower();
                    }
                 }
                ////
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    if (powerSum[1-ind] > 2 * powerSum[ind] && attacks[i][j][ind].size()>0) {
                        for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                            Fish fish =attacks[i][j][ind].get(k);
                            //score[ind] -= gc.getKillFishScore();
                            if(fish.isQueen() && !fishChanges.containsKey(fish.getId())) {
                                changeScores(ind, gc.getKillQueenScore());
                            }
                            if(!fish.isQueen() && !fishChanges.containsKey(fish.getId())) {
                                changeScores(ind, gc.getKillFishScore());
                            }
                            fishChanges.put(fish.getId(), "delete");
                        }
                        if(attacks[i][j][1-ind].size()==1){
                            fishChanges.put(attacks[i][j][1-ind].get(0).getId(), "move");
                        }
                    }
                    if(attacks[i][j][ind].size()==0 && attacks[i][j][1-ind].size()==1){
                        fishChanges.put(attacks[i][j][1-ind].get(0).getId(), "move");
                    }
                }
            }
        }

                /////////////////////////////////////////////////////////////

        // DELETION && MOVES
        for(int ind=0;ind<gc.getTeamNum();ind++) {
            if(fishes[ind].size() == 0) {
                continue;
            }
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
                        diff.del(fish.getId());
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
                        if(nextCell[ind].get(i).getContent() instanceof Trash) {
                            Trash trash = (Trash) nextCell[ind].get(i).getContent();
                            /**
                             * turn
                             */
                            if(!fish.isSick()) {
                                fish.setSick(true);
                                fish.setDeadTime(turn + gc.getSickLifeTime());
                                fishAlters.add(fish.getId());
                            }

                            diff.del(trash.getId());
                            tempObjects.remove(trash);
                        }
                        else if(nextCell[ind].get(i).getContent() instanceof Food ) {
                            Food food = (Food) nextCell[ind].get(i).getContent();

                            if(fish.isQueen()) {
                                int tempA[] = new int[2];
                                tempA[ind] = map.getScore()[ind]+gc.getQueenFoodScore();
                                tempA[1-ind] = map.getScore()[1-ind];
                                map.setScore(tempA);
                                //score[ind] += gc.getQueenFoodScore();
                            }
                            else {
                                int tempA[] = new int[2];
                                tempA[ind] = map.getScore()[ind]+gc.getFishFoodScore();
                                tempA[1-ind] = map.getScore()[1-ind];
                                map.setScore(tempA);
                                //score[ind] += gc.getFishFoodScore();
                            }
                            fish.setPregnant(true);

                            diff.del(food.getId());
                            tempObjects.remove(food);
                            // DIF FOOD DELETE
                        }
                        nextCell[ind].get(i).setContent(fish);
                        // DIFF.move
                        /**
                         * mv r dir
                         */
                        diff.mov(fish.getId(),1);
                    }
                }
            }
        }

        // Alters
        for(int ind=0;ind<2;ind++){
            for(int i=0;i<fishes[ind].size();i++){
                if(fishAlters.contains(fishes[ind].get(i))){
                    Fish fish = fishes[ind].get(i);
                    diff.alter(fish.getId(), fish.getColorNumber(),(fish.isSick())?1:0);

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
                        diff.del(fish.getId());
                        cells[i][j].setContent(null);
                    }

                }
            }
        }

        // SICK DEATH

        for(int ind=0; ind<gc.getTeamNum(); ind++){
            if(fishes[ind].size() == 0) {
                continue;
            }
            for(int i=fishes[ind].size()-1;i>=0;i--){
                if(fishes[ind].get(i).getDeadTime() == turn) {
                    Fish fish = fishes[ind].get(i);
                    diff.del(fish.getId());
                    cells[fish.getPosition().getRow()][fish.getPosition().getColumn()].setContent(null);
                    fishes[fish.getTeamNumber()].remove(fish);
                }
            }
        }

        // FOOD & TRASH & NET

        if(tempObjects.size() > 0) {
            for(int i=tempObjects.size()-1; i>=0; i--) {
                if(tempObjects.get(i).getDeadTime() == turn) {
                    diff.del(tempObjects.get(i).getId());
                    cells[tempObjects.get(i).getPosition().getRow()][tempObjects.get(i).getPosition().getColumn()].setContent(null);
                    tempObjects.remove(i);
                }
            }
        }

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
        Fish baby =new Fish(idCounter++, cell, fish.getTeamNumber(), fish.getDirection(), fish.getColorNumber(), fish.isQueen());
        cell.setContent(baby);
        fishes[fish.getTeamNumber()].add(baby);
        diff.addFish(baby.getId(), 0, baby.getPosition().getRow(), baby.getPosition().getColumn(),
                baby.getDirection(), baby.getColorNumber(), (baby.isQueen())?1:0, baby.getTeamNumber());
        //int id, int type, int x, int y, int dir, int color, int queen, int team
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
        tempA[ind] = map.getScore()[ind] - val;
        tempA[1-ind] = map.getScore()[1-ind] + val;
        map.setScore(tempA);
        //score[ind]-=val;
        //score[1-ind]+=val;
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


    /*
    it is remained :generateOutputs
     */
    @Override
    public void generateOutputs() {
        if (PARAM_SHOW_DEBUG_UI.getValue() == Boolean.TRUE) {
            debugUI.setMap(this.map);
        }
        /*
        if (debugUI != null) {
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), lastClientEvents, getStatusMessage(), movesDest2, movesSize2);
        }
        this.context.flush();
        this.context.turnUP();
        */
    }

    @Override
    public Message getUIMessage() {
       Message messages;
        String name0 = Message.NAME_TURN;
        Object[] args0 = {this.turn, this.score, this.diff.getChanges()};
        messages = new Message(name0, args0);
        return messages;
    }

    /*
    It is remained:getStatusMessage
     */
    @Override
    public Message getStatusMessage() {

        return new Message(Message.NAME_STATUS, new Object[]{this.turn, this.score[0], this.score[1]});
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        String name0 = Message.NAME_TURN;
        Object[] args0 = {this.turn, this.score, this.diff.getChanges()};
        messages[0] = new Message(name0, args0);

        String name1 = Message.NAME_TURN;
        Object[] args1 = {this.turn, this.score, this.diff.getChanges()};
        messages[1] = new Message(name1, args1);

        return messages;
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    /*
    isGameFinished is remained
     */
    @Override
    public boolean isGameFinished() {
        int[] finalScore = new int[2];
        finalScore[0] = map.getScore()[0];
        finalScore[1] = map.getScore()[1];
        if(numberOfQueens[0]==0 && numberOfQueens[1] >0){
            finalScore[0]=0;
            finalScore[1]=map.getScore()[1];

        }
        else if(numberOfQueens[1]==0 && numberOfQueens[0] >0){
            finalScore[1]=0;
            finalScore[0]=map.getScore()[1];
        }
        map.setScore(finalScore);
        int population = fishes[0].size() + fishes[1].size();
        if(numberOfQueens[0]==0 || numberOfQueens[1]==0 /*|| population > gc.getEndRatio()*(W*H)*/)
            return true;
        return false;
    }

    @Override
    public void terminate() {
        /*
        if (debugUI != null) {
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), null, getStatusMessage(), null, null);
        }
        */
    }
}
