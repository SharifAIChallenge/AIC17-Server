package Swarm;

import Swarm.models.Diff;
import Swarm.objects.*;
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

import java.util.*;


/**
 * Created by pezzati on 1/28/16.
 */
public class SwarmGameLogic implements GameLogic {
    private static final String TAG = "Flows";

    private Context context;
    private DebugUI debugUI;
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
    GameConstants gc = map.getGameConstants();
    int[][][][][] update = new int[gc.getTeamNum()][2][3][2][3];

    int[] rowHeadDir = {0, -1,  0, 1};
    int[] colHeadDir = {1,  0, -1, 0};

    int[] rowLeftDir = {-1, -1,  1, 1};
    int[] colLeftDir = { 1, -1, -1, 1};

    int[] rowRightDir = {1, -1,  -1,  1};
    int[] colRightDir = {1,  1,  -1, -1};

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

    private int score[] = new int[gc.getTeamNum()];
    private int numberOfQueens[] = new int[gc.getTeamNum()];
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

    public Context getContext() {
        return context;
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
        this.IDCounter = this.map.getIdCounter();
        this.teleports = this.map.getTeleports();
    }

    @Override
    public Message getUIInitialMessage() {
        Object[] args = {this.context.getMap().getVertexNum(), maxLow, maxNormal,
                this.context.getMap().getAdjacencyList(), this.context.getUIDiffList(),
                Configs.CLIENT_CONFIGS.get(0).getName(), Configs.CLIENT_CONFIGS.get(1).getName()};
        String name = Message.NAME_INIT;
        return new Message(name, args);
    }

    @Override
    public Message[] getClientInitialMessages() {
        GameConstants constants = context.getMap().getGameConstants();
        constants.setTurnTimeout(PARAM_TURN_TIMEOUT.getValue());

        Message[] msg = new Message[2];
        String name0 = Message.NAME_INIT;
        Object[] args0 = {constants, 0, this.context.getMap().getAdjacencyList(), this.context.getDiffList(0, maxLow, maxNormal)};
        msg[0] = new Message(name0, args0);

        String name1 = Message.NAME_INIT;
        Object[] args1 = {constants, 1, this.context.getMap().getAdjacencyList(), this.context.getDiffList(1, maxLow, maxNormal)};
        msg[1] = new Message(name1, args1);
        return msg;
    }

    public void initialize(){
        // init these for the first time from map;
        //private ArrayList <Fish>[] fishes;
        //private ArrayList <GameObject> tempObjects;
        //private ArrayList <Teleport> teleports;
        //private Cell[][] cells;
        //private int score[] = new int[gc.getTeamNum()];

        //HashMap<Integer, String> fishChanges;
        //HashSet<Integer> fishAlters = new HashSet<>();



        attacks = new ArrayList[H][W][2];

        for(int i=0;i<H;i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind] = new ArrayList<>();
                }
            }
        }



    }

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {

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

        for(int i=0;i<H;i++){
            for(int j=0;j<W;j++) {
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
                int[] queens = new int[2];
                Fish[] lastNormalFish = new Fish[2];

                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                        Fish fish = attacks[i][j][ind].get(k);
                        /**
                         * CALC cost of queen in both cells ??
                         */
                        if (fish.isQueen() && attacks[i][j][1-ind].size() > 0 /* && !fishChanges.containsKey(fish.getId())*/) {
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
                    if (powerSum[1-ind] > 2 * powerSum[ind]) {
                        for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                            Fish fish =attacks[i][j][ind].get(k);
                            if(!fish.isQueen() && !fishChanges.containsKey(fish.getId())){
                                fishChanges.put(fish.getId(), "delete");
                                //score[ind] -= gc.getKillFishScore();
                                changeScores(ind, gc.getKillFishScore());
                            }
                        }
                        if((attacks[i][j][1-ind].size()-queens[1-ind])==1){
                            if( !fishChanges.containsKey(lastNormalFish[1-ind]) );
                                fishChanges.put(lastNormalFish[1-ind].getId(), "move");
                        }
                    }
                }
                if(queens[0]>0 && queens[1]>0){
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
                if(fishChanges.containsKey(fishes[ind].get(i))) {
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
                        boolean moved = true;
                        if(fish.getPosition().equals(nextCell[ind].get(i))) {
                            moved = false;
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

                            diff.del(trash.getId());
                            tempObjects.remove(trash);
                        }
                        else if(nextCell[ind].get(i).getContent() instanceof Food ) {
                            Food food = (Food) nextCell[ind].get(i).getContent();

                            if(fish.isQueen()) {
                                score[ind] += gc.getQueenFoodScore();
                            }
                            else {
                                score[ind] += gc.getFishFoodScore();
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
                        diff.mov(fish.getId(),fish.getDirection());
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
                        diff.del(fish.getId());
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
                    diff.del(fish.getId());
                    cells[fish.getPosition().getRow()][fish.getPosition().getColumn()].setContent(null);
                }
            }
        }

        // FOOD & TRASH & NET

        for(int i=0; i<tempObjects.size(); i++) {
            if(tempObjects.get(i).getDeadTime() == turn) {
                tempObjects.remove(i);
                diff.del(tempObjects.get(i).getId());
                cells[tempObjects.get(i).getPosition().getRow()][tempObjects.get(i).getPosition().getColumn()].setContent(null);
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
        Fish baby =new Fish(idCounter, cell, fish.getTeamNumber(), fish.getDirection(), fish.isQueen(), fish.getColorNumber());
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
            row = (fish.getPosition().getRow() + rowHeadDir[fish.getDirection()] + H)%H;
            col = (fish.getPosition().getColumn() + colHeadDir[fish.getDirection()] + W)%W;
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


    @Override
    public void generateOutputs() {
        if (debugUI != null) {
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), lastClientEvents, getStatusMessage(), movesDest2, movesSize2);
        }
        this.context.flush();
        this.context.turnUP();
    }

    @Override
    public Message getUIMessage() {
        return uiMessage;
    }

    @Override
    public Message getStatusMessage() {
        int[] armyCount = this.context.getDiffer().getPrevArmyCount();
        int[] ownerships = this.context.getDiffer().getPrevOwnership();
        int unitsCount[] = new int[2];
        for (int i = 0; i < ownerships.length; i++) {
            if (ownerships[i] != -1) {
                unitsCount[ownerships[i]] += armyCount[i];
            }
        }
        int totalUnits = unitsCount[0] + unitsCount[1];
        int totalTurns = this.context.getMap().getGameConstants().getTurns();
        int remainingTurns = totalTurns - this.context.getTurn();
        double points[] = new double[2];
        for (int i = 0; i < 2; i++) {
            int diffSign = unitsCount[i] - unitsCount[1 - i];
            if (diffSign < 0)
                diffSign = -1;
            if (diffSign > 0)
                diffSign = 1;
            if (diffSign == 0)
                diffSign = 0;
            points[i] = 1 + (totalUnits == 0 ? 0.5 : (double) unitsCount[i] / totalUnits) + (double) remainingTurns / totalTurns * diffSign;
        }
        return new Message(Message.NAME_STATUS, new Object[]{context.getTurn(), points[0], points[1]});
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        String name0 = Message.NAME_TURN;
        Object[] args0 = {this.context.getTurn(), this.context.getDiffList(0, maxLow, maxNormal)};
        messages[0] = new Message(name0, args0);

        String name1 = Message.NAME_TURN;
        Object[] args1 = {this.context.getTurn(), this.context.getDiffList(1, maxLow, maxNormal)};
        messages[1] = new Message(name1, args1);

        return messages;
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished() {
        return (this.context.getMap().isFinished() || this.context.getTurn() >= this.context.getMap().getGameConstants().getTurns());
    }

    @Override
    public void terminate() {
        if (debugUI != null) {
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), null, getStatusMessage(), null, null);
        }
    }
}
