package Swarm;

import Swarm.map.Cell;
import Swarm.models.Diff;
import Swarm.models.GameConstants;
import Swarm.models.Map;
import Swarm.objects.*;
import debugUI.paintIt.MapFrame;
import model.Event;
import network.data.Message;
import server.config.BooleanParam;
import server.config.FileParam;
import server.config.IntegerParam;
import server.core.GameLogic;
import server.core.GameServer;
import util.Log;

import java.util.*;


/**
 * Created by pezzati on 1/28/16.
 */
public class SwarmGameLogic implements GameLogic {
    private static final String TAG = "Swarm";

    private MapFrame debugUI;
    private Event[][] lastClientEvents;

    /**
     * //swarm Constatnts :::
     * private int mapSize;
     * private int teamNum = 2;
     * private int teleportNum;
     * private int initFishNum;
     * private int initQueenNum;
     * <p>
     * private double foodProb;
     * private double trashProb;
     * private double netProb;
     * <p>
     * private int netActiveTime;
     * private int netValidTime;
     * <p>
     * //
     * <p>
     * private int changeColorLimit;
     * private int kStep;
     * // or :
     * private int colorCost;
     * //
     * private int sickCost;
     * private int UpdateCost;
     * private int detMoveCost;
     * <p>
     * private int killQueenScore;
     * private int killFishScore;
     * private int queenCollisionScore;
     * private int fishFoodScore;
     * private int queenFoodScore;
     * private int sickLifeTime;
     * <p>
     * private int powerRatio;
     * private double endRatio;
     * <p>
     * private int disobeyPointerTeam0;
     * private int disobeyPointerTeam1;
     * private int disobeyNum;
     * private double disobeyRatio;
     * <p>
     * private int foodValidTime;
     * private int trashValidTime;
     */
//////////
    Diff diff = new Diff();
    private int idCounter;
    private int H, W;
    Map map;
    GameConstants gc;
    int[][][][][] update;

    int[] rowHeadDir = new int[]{0, -1, 0, 1};
    int[] colHeadDir = new int[]{1, 0, -1, 0};

    int[] rowLeftDir = new int[]{-1, -1, 1, 1};
    int[] colLeftDir = new int[]{1, -1, -1, 1};

    int[] rowRightDir = new int[]{1, -1, -1, 1};
    int[] colRightDir = new int[]{1, 1, -1, -1};

    private ArrayList<Fish>[] fishes;
    private ArrayList<GameObject> tempObjects;
    private ArrayList<Teleport> teleports;
    ArrayList<Cell>[] nextCell;
    HashMap<Integer, Integer> nextMoveMap;
    //ArrayList<Integer>[] nextMove;
    //HashMap<Integer,Integer> detMoves = new HashMap<>();
    ArrayList<Event>[] detMoves;
    private Cell[][] cells;
    private ArrayList<Fish>[][][] attacks;
    private boolean[][][] newBorn;
    private Fish[][][] motherFish;

    private int[][][] mark;
    private Cell[][][] moves;
    private int[][][] moves_valid;
    private int[][][] dfs_reverse_data;

    private boolean firstTurn = false;

    private int score[];
    private int numberOfQueens[];
    //    HashMap<Integer, String> fishChanges;
    ArrayList<Pair<Fish, String>> fishChanges;
    HashSet <Integer>deletedFishes;
    Set<Integer> fishesChanged;
    HashSet<Integer> fishAlters = new HashSet<>();
    HashSet<Integer> teleportChecked = new HashSet<>();
    //private int turn;
    private Message uiMessage;

    public static final IntegerParam PARAM_CLIENT_TIMEOUT = new IntegerParam("ClientTimeout", 500);
    public static final IntegerParam PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", 1000);
    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static final BooleanParam PARAM_SHOW_DEBUG_UI = new BooleanParam("ShowDebugUI", true);

    public static void main(String[] args) throws InterruptedException {
        GameServer gameServer = new GameServer(new SwarmGameLogic(), args);
//        System.err.println("start");
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
        Object[] args0 = {0, this.map.getW(), this.map.getH(), this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(), this.map.getInitialTeleports(), constants};
        msg = new Message(name0, args0);
        return msg;
    }

    @Override
    public Message[] getClientInitialMessages() {
        GameConstants constants = this.map.getConstants();
        constants.setTurnTimeout(PARAM_TURN_TIMEOUT.getValue());

        Message[] msg = new Message[2];
        String name0 = Message.NAME_INIT;
        int[] size = {this.map.getW(), this.map.getH()};
        Object[] args0 = {0, size, this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(), this.map.getInitialTeleports(), this.map.getInitialConstants()};
        msg[0] = new Message(name0, args0);

        String name1 = Message.NAME_INIT;
        Object[] args1 = {1, size, this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(), this.map.getInitialTeleports(), this.map.getInitialConstants()};
        msg[1] = new Message(name1, args1);
        return msg;
    }

    private void initialize() {
        idCounter++;
        //handle map.getScore()[0];
        nextMoveMap = new HashMap<>();
        fishChanges = new ArrayList<>();
        deletedFishes = new HashSet<>();
        fishesChanged = new HashSet<>();
        fishAlters = new HashSet<>();
        teleportChecked = new HashSet<>();

        attacks = new ArrayList[H][W][2];
        this.numberOfQueens = new int[gc.getTeamNum()];
        this.update = new int[gc.getTeamNum()][2][3][2][3];

        mark = new int[2][H][W];
        moves = new Cell[2][H][W];
//        moves_r = new ArrayList[2][H][W];
        moves_valid = new int[2][H][W];
        dfs_reverse_data = new int[2][H][W];

        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind] = new ArrayList<>();
                }
//                for (int t = 0; t < 1; t++) {
//                    moves_r[t][i][j] = new ArrayList<>();
//                }
            }
        }

        //////////////////
        update = new int[gc.getTeamNum()][2][3][2][3];
        //Arrays.fill(update, 1);
        for (int ind = 0; ind < 2; ind++) {
            for (int c = 0; c < 2; c++) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 2; j++) {
                        for (int k = 0; k < 3; k++) {
                            update[ind][c][i][j][k] = 1;
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

//        nextMove = new ArrayList[2];
//        nextMove[0] = new ArrayList<>();
//        nextMove[1] = new ArrayList<>();


        numberOfQueens = new int[2];
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                if (fishes[ind].get(i).isQueen())
                    numberOfQueens[ind]++;
            }
        }
        // numberOfQueens[0] ;

        firstTurn = true;
    }

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
        /*if(map.getTurn() == 0) {
            map.setTurn(map.getTurn()+1);
            return;
        }
        map.setTurn(map.getTurn()+1);
        */
        if (firstTurn) {
            firstTurn = false;
            //map.setTurn(map.getTurn()+1);
            return;
        }
        map.setTurn(map.getTurn() + 1);


//        System.out.println("TUUUUUUUUUUUUURN" + map.getTurn());
//        for(int i=0;i<H;i++){
//            for(int j=0;j<W;j++){
//                Cell cell = cells[i][j];
//                System.out.println(i + " " + j);
//                System.out.println(cell.getContent());
//                System.out.println(cell.getNet());
//            }
//        }

        /*for(int ind=0;ind<2;ind++){
            for(int i=0;i<fishes[ind].size();i++){
                Fish fish = fishes[ind].get(i);
                System.out.println("ind:" + ind + " i:" + i + " row:" + fish.getPosition().getRow() + " col:" + fish.getPosition().getColumn());
            }
        }
*/
        diff = new Diff();

        nextCell[0].clear();
        nextCell[1].clear();

//        nextMove[0].clear();
//        nextMove[1].clear();

        detMoves[0].clear();
        detMoves[1].clear();
        //detMoves = new ArrayList[2];
        //detMoves[0] = new ArrayList<>();
        //detMoves[1] = new ArrayList<>();
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind].clear();
                }
                for (int t = 0; t < gc.getTeamNum(); t++) {
                    mark[t][i][j] = 0;
                    moves[t][i][j] = null;
//                    moves_r[t][i][j].clear();
                    moves_valid[t][i][j] = 0;
                    dfs_reverse_data[t][i][j] = 0;
                }
            }
        }

        newBorn = new boolean[gc.getTeamNum()][H][W];
        motherFish = new Fish[gc.getTeamNum()][H][W];

        fishChanges.clear();
        deletedFishes.clear();
        fishesChanged.clear();
        fishAlters.clear();
        nextMoveMap.clear();
        teleportChecked.clear();
        // get argomans and make what we want
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < clientsEvent[ind].length; i++) {
                if (clientsEvent[ind][i].getType().equals("s")) {
                    if (clientsEvent[ind][i].getArgs().length != 5) continue;
                    int[] t = new int[5];
                    for (int j = 0; j < 5; j++) {
                        t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                    }
                    update[ind][t[0]][t[1]][t[2]][t[3]] = t[4];
                    //score[ind] -= gc.getUpdateCost();
                    changeScores(ind, gc.getUpdateCost());
                } else if (clientsEvent[ind][i].getType().equals("m")) {
                    /**
                     * handling that the type of args is correct or not
                     */
                    if (clientsEvent[ind][i].getArgs().length != 2) continue;
                    detMoves[ind].add(clientsEvent[ind][i]);
                } else if (clientsEvent[ind][i].getType().equals("c")) {
                    if (clientsEvent[ind][i].getArgs().length != 2) continue;
                    int[] t = new int[2];
                    for (int j = 0; j < 2; j++) {
                        t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                    }
                    for (int j = 0; j < fishes[ind].size(); j++) {
                        /**
                         *  we check containing in fishAlters
                         *  so do not need to get HASHSET
                         */
                        if (fishes[ind].get(j).getId() == t[0] && !fishAlters.contains(fishes[ind].get(j).getId())) {
                            fishes[ind].get(j).setColorNumber(t[1]);
                            //score[ind] -= gc.getColorCost();
                            changeScores(ind, gc.getColorCost());
                            fishAlters.add(fishes[ind].get(j).getId());
                        }
                    }
                }
            }
        }

        // NEXTCELL ACCORDING TO FISHES
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                nextCell[ind].add(getNextCellViaUpdate(fishes[ind].get(i)));
            }
        }
//
//        // DESTINATIONS
//        for(int ind=0;ind<gc.getTeamNum();ind++){
//            for(int i=0; i<detMoves[ind].size(); i++){
//                /**
//                 * handle correct type
//                 */
//                int id = Integer.parseInt(detMoves[ind].get(i).getArgs()[0]),
//                        mv = Integer.parseInt(detMoves[ind].get(i).getArgs()[1]);
//                for(int j=0;j<fishes[ind].size();j++){
//                    if(fishes[ind].get(j).getId() == id){
//                        nextCell[ind].set(j, getNextCellViaMove(fishes[ind].get(j), mv));
//                        //score[ind] -= gc.getDetMoveCost();
//                        changeScores(ind, gc.getDetMoveCost());
//                    }
//                }
//            }
//        }

        stageMakeAttacks();

        stageDeleteAndMove();

        stageSickZombie();

        stageNetDeath();

        stageSickDeath();


        stageFoodTrashNet();

        stageAddRandomTempObject();

        stageNewBorn();
        stageDiff();

        stageSwitchTeleports();

        stageAlters();


        //uiMessage = new Message(Message.NAME_TURN, uiMessages.toArray());
    }


    private void stageMakeAttacks() {
        // ATTACKS !!!
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                // move fishes
                /**
                 * for each cell compare summation of each team power
                 * if it was more than twice of the other team remove all of them
                 * and fix scores/
                 */
                int[] powerSum = new int[2];
                Fish cellFish = null;
                if (cells[i][j].getContent() instanceof Fish) {
                    cellFish = (Fish) cells[i][j].getContent();
                }
                if (cellFish != null) {
                    powerSum[cellFish.getTeamNumber()] = cellFish.getPower();
                }
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                        Fish fish = attacks[i][j][ind].get(k);
                        powerSum[ind] += fish.getPower();
                    }
                }
                ////
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    int attacks_num[] = {attacks[i][j][0].size(), attacks[i][j][1].size()};
                    if (cellFish != null)
                        attacks_num[cellFish.getTeamNumber()]++;
                    if ((powerSum[1 - ind] == 0 && powerSum[ind] == 0 && attacks_num[1 - ind] > attacks_num[ind])
                            || powerSum[1 - ind] > 2 * powerSum[ind]) {
//                        if (cellFish == null || cellFish.getTeamNumber() == ind) {
                        for (Fish f : attacks[i][j][1 - ind]) {
                            // mark as candidate move
//                            System.out.println("move " + f.getId() + " " + (1 - ind) + " " + f.getPosition().getRow() + " " + f.getPosition().getColumn() + " -> " + i + ", " + j);
//                                //System.out.print("attacks ");
//                                //System.out.println(attacks[i][j][1 - ind]);
                            moves[1 - ind][f.getPosition().getRow()][f.getPosition().getColumn()] = cells[i][j];
//                                moves_r[1-ind][i][j].add(f.getPosition());
                        }
//                        }

                        if (cellFish != null) {
                            if (cellFish.getTeamNumber() == ind) {
                                if (cellFish.isQueen() && !fishesChanged.contains(cellFish.getId())) {
                                    changeScores(ind, gc.getKillQueenScore());
                                }
                                if (!cellFish.isQueen() && !fishesChanged.contains(cellFish.getId())) {
                                    changeScores(ind, gc.getKillFishScore());
                                }
                                fishesChanged.add(cellFish.getId());
                                fishChanges.add(new Pair<>(cellFish, "delete"));
                                deletedFishes.add(cellFish.getId());

//                                System.out.println("delete center " + cellFish + ", " + cellFish.getPosition());
                            }
                        }
                        for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                            Fish fish = attacks[i][j][ind].get(k);
                            //score[ind] -= gc.getKillFishScore();
                            if (fish.isQueen() && !fishesChanged.contains(fish.getId())) {
                                changeScores(ind, gc.getKillQueenScore());
                            }
                            if (!fish.isQueen() && !fishesChanged.contains(fish.getId())) {
                                changeScores(ind, gc.getKillFishScore());
                            }
                            fishesChanged.add(fish.getId());
                            fishChanges.add(new Pair<>(fish, "delete"));
                            deletedFishes.add(fish.getId());

//                            System.out.println("delete neighbour " + fish.getId() + ", " + fish.getPosition());
                        }
//                        Fish takFish = attacks[i][j][1-ind].get(0);
//                        int indexOf = fishes[takFish.getTeamNumber()].indexOf(takFish);
//                        Cell nxtTakFishCell = nextCell[takFish.getTeamNumber()].get(indexOf);
                        /**
                         * todo: tofmali takFish
                         */
//                        if(attacks[i][j][1-ind].size()==1 && !fishChanges.containsKey(takFish.getId())
//                                && !takFish.getPosition().equals(nxtTakFishCell)){
//                            fishChanges.put(takFish.getId(), "move");
//                        }
                        // todo: next choice?!?!?!
                    } else {
                        for (Fish f : attacks[i][j][1 - ind]) {
//                            System.out.println("move cellfish " + f + ", " + f.getPosition() + " " + i + "," + j);
                            int r = f.getPosition().getRow(), c = f.getPosition().getColumn();
                            moves[1 - ind][r][c] = cells[r][c];
                        }
                    }
                    // todo: @hadi: check this if clause
//                    if(attacks[i][j][ind].size()==0 && attacks[i][j][1-ind].size()==1  &&
//                            !fishChanges.containsKey(attacks[i][j][1-ind].get(0).getId())){
//                        Fish takFish = attacks[i][j][1-ind].get(0);
//                        int indexOf = fishes[takFish.getTeamNumber()].indexOf(takFish);
//                        Cell nxtTakFishCell = nextCell[takFish.getTeamNumber()].get(indexOf);
//                        if(!takFish.getPosition().equals(nxtTakFishCell)) {
//                            fishChanges.put(attacks[i][j][1 - ind].get(0).getId(), "move");
//                        }
//                    }
                }
            }
        }
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                Cell cell = nextCell[ind].get(i);
                if (cell == fishes[ind].get(i).getPosition()) {
                    moves[ind][cell.getRow()][cell.getColumn()] = cell;
                }
            }
        }
    }


    private void stageDeleteAndMove() {
        Log.i(TAG, "changes: " + fishChanges.toString());


        for (int i = 0; i < fishChanges.size(); i++) {
            Fish fish = fishChanges.get(i).first;
            String change = fishChanges.get(i).second;
            int team = fish.getTeamNumber();
            if (change.equals("delete")) {
                deleteFish(fish, fishes[team].indexOf(fish));
            }
        }
        // Handle Deletions & (Conflicting & Non Conflicting) Moves
        for (int t = 0; t < gc.getTeamNum(); t++) {
            if (fishes[t].size() == 0) {
                continue;
            }
//            for (int i = fishes[t].size() - 1; i >= 0; i--) {
//                Fish fish = fishes[t].get(i);
//                if (fishesChanged.contains(fish.getId())) {
//                    String str = fishChanges.get(fish.getId()).second;
//                    if (str.equals("delete")) {
//                        deleteFish(fish, i);
//                    }
//                }
//            }
//            //System.out.println("nexts " + t);
            for (int r = 0; r < H; r++) {
                for (int c = 0; c < W; c++) {
                    if (cells[r][c].getContent() instanceof Fish && ((Fish) cells[r][c].getContent()).getTeamNumber() == t) {
                        int i = fishes[t].indexOf(cells[r][c].getContent());
                        //System.out.print(nextCell[t].get(i));
                    } else {
//                        //System.out.print("...");
                    }
//                    //System.out.print(",");
                }
//                //System.out.println();
            }
//            System.out.println("moves " + t);
//            for (int r = 0; r < H; r++) {
//                for (int c = 0; c < W; c++) {
//                    System.out.print(moves[t][r][c]);
//                    System.out.print(",");
//                }
//                System.out.println();
//            }
            int total_chain = 0;
            for (int r = 0; r < H; r++) {
                for (int c = 0; c < W; c++) {
                    if (moves[t][r][c] == null && mark[t][r][c] == 0) {
                        total_chain += dfs_reverse(t, r, c, 0);
                        mark[t][r][c] = 1;
                    }
                }
            }
            for (int r = 0; r < H; r++) {
                for (int c = 0; c < W; c++) {
                    if (moves[t][r][c] != null && mark[t][r][c] == 0) {
                        total_chain += dfs_loop(t, r, c, 0);
                    }
                }
            }
//            //System.out.println("Total chain: " + total_chain);
//            for (int r = 0; r < H; r++) {
//                for (int c = 0; c < W; c++) {
//                    if (moves_valid[t][r][c] != 0) {
//                        //System.out.print("v");
//                    } else {
//                        //System.out.print(".");
//                    }
//                }
//                //System.out.println();
//            }
            ArrayList[] valid_moves = new ArrayList[W * H];
            for (int r = 0; r < H; r++) {
                for (int c = 0; c < W; c++) {
                    int i = moves_valid[t][r][c];
                    if (i != 0) {
                        //System.out.println(r + ", " + c + ", " + cells[r][c].getContent());
                        if (valid_moves[i] == null)
                            valid_moves[i] = new ArrayList();
                        if (cells[r][c].getContent() != null) {
                            valid_moves[i].add(new int[]{t, r, c});
                            fishesChanged.add(cells[r][c].getContent().getId());
                        }
//                        fishChanges.put(cells[r][c].getContent().getId(), "move");
                    }
                }
            }
            for (int i = 0; i < valid_moves.length; i++) {
                if (valid_moves[i] != null) {
                    for (Object move : valid_moves[i]) {
                        int[] move_arr = (int[]) move;
                        fishChanges.add(new Pair<>((Fish) cells[move_arr[1]][move_arr[2]].getContent(), "move"));
                    }
                }
            }
//            for (int i = fishes[t].size() - 1; i >= 0; i--) {
//                Fish fish = fishes[t].get(i);
//                if (fishChanges.containsKey(fish.getId())) {
//                    String str = fishChanges.get(fish.getId());
//                    if (str.equals("move")) {
//                        moveFish(fish, nextCell[t].get(i));
//                    }
//                }
//            }
        }

        for (int i = 0; i < fishChanges.size(); i++) {
            Fish fish = fishChanges.get(i).first;
            String change = fishChanges.get(i).second;
            int team = fish.getTeamNumber();
            if (change.equals("move")) {
                int r = fish.getPosition().getRow(), c = fish.getPosition().getColumn();
                moveFish(fish, moves[team][r][c]);
            }
        }

    }

    private int dfs_reverse(int t, int r, int c, int valid_index) {
        //System.out.println("dfs rev on " + r + ", " + c);
        if (valid_index > 0)
            moves_valid[t][r][c] = valid_index;
        if (mark[t][r][c] != 0)
            return dfs_reverse_data[t][r][c];
        mark[t][r][c] = 1;
        boolean has_max = false;
        int max = 0, maxr = 0, maxc = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int row = makeValidIndex(r + i, H);
                int col = makeValidIndex(c + j, W);
                if (moves[t][row][col] == cells[r][c]) {
                    int val = dfs_reverse(t, row, col, 0);
                    if (val > max) {
                        has_max = true;
                        max = val;
                        maxr = row;
                        maxc = col;
                    }
                }
            }
        }
        if (has_max) {
            dfs_reverse(t, maxr, maxc, valid_index + 1);
        }
        dfs_reverse_data[t][r][c] = max + 1;
        return max + 1;
    }

    private int dfs_loop(int t, int r, int c, int d) {
        if (mark[t][r][c] != 0)
            return 0;
        mark[t][r][c] = d;
        if (moves[t][r][c] == null)
            return 0;
        int r2 = moves[t][r][c].getRow();
        int c2 = moves[t][r][c].getColumn();
        int dl = dfs_loop(t, r2, c2, d + 1);
        if (dl > 0) {
            moves_valid[t][r][c] = d + 1;
            return dl - 1;
        }
        if (mark[t][r2][c2] != d + 1) {
            moves_valid[t][r][c] = d + 1;
            return d + 1 - mark[t][r2][c2];
        }
        return 0;
    }

    private void stageSickZombie() {
        for (int ind = 0; ind < 2; ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                Fish fish = fishes[ind].get(i);
                if (fish.isSick()) {
                    sickZombie(fish, i);
                }
            }
        }
    }

    private void stageNetDeath() {
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                // NET death just for fish ?
                if (cells[i][j].getContent() instanceof Fish) {
                    if (isNetDeadTime(cells[i][j])) {
                        Fish fish = (Fish) cells[i][j].getContent();
                        fishChanges.add(new Pair<>(fish, "delete"));
                        deletedFishes.add(fish.getId());
                        fishesChanged.add(fish.getId());
                        deleteFish(fish, fishes[fish.getTeamNumber()].indexOf(fish));
                        //fishes[fish.getTeamNumber()].remove(fish);
                        //diff.del(fish.getId());
                        //cells[i][j].setContent(null);
                    }

                }
            }
        }
    }

    private void stageSickDeath() {
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            if (fishes[ind].size() == 0) {
                continue;
            }
            for (int i = fishes[ind].size() - 1; i >= 0; i--) {
                if (fishes[ind].get(i).getDeadTime() == map.getTurn()) {
                    Fish fish = fishes[ind].get(i);
                    fishChanges.add(new Pair<>(fish, "delete"));
                    deletedFishes.add(fish.getId());
                    deleteFish(fish, i);
                    //diff.del(fish.getId());
                    //cells[fish.getPosition().getRow()][fish.getPosition().getColumn()].setContent(null);
                    //fishes[fish.getTeamNumber()].remove(fish);
                }
            }
        }
    }

    private void stageDiff() {
        // Iterate through HashMap entries(Key-Value pairs)
        for (Pair<Fish, String> p : fishChanges) {
            String str = p.second;
            Fish fish = p.first;
            if (str.equals("delete")) {
                diff.del(fish.getId());
            }
            else if (str.equals("move") && !deletedFishes.contains(fish.getId())) {
                diff.mov(fish.getId(), nextMoveMap.get(fish.getId()));
            }
        }
    }

    /**
     * todo: @hadi: check this later
     */
    private void stageNewBorn() {
        /// NEW BORN
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < H; i++) {
                for (int j = 0; j < W; j++) {
                    /**
                     * == or .equals
                     */
                    if (newBorn[ind][i][j] && cells[i][j].getContent() == null) {
//                        System.out.println("turn = " + map.getTurn() + " baby is born in " + "row:" + i + "column:" + j);
                        makeBabyFish(cells[i][j], motherFish[ind][i][j]);
                    } else if (newBorn[ind][i][j] && cells[i][j].getContent() != null) {
                        loop:
                        for (int k = -1; k <= 1; k++) {
                            for (int l = -1; l <= 1; l++) {
                                if (k == 0 && l == 0)
                                    continue;
                                if (cells[k + i][l + j].getContent() == null) {
//                                    System.out.println("turn = " + map.getTurn() + " baby is born in " + "row:" + (i + k) + "column:" + (j + l));
                                    makeBabyFish(cells[(k + i)%H][(l + j)%W], motherFish[ind][i][j]);
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void stageFoodTrashNet() {
        // FOOD & TRASH & NET

        if (tempObjects.size() > 0) {
            for (int i = tempObjects.size() - 1; i >= 0; i--) {
                //System.out.println("ooooooooooooo");
                GameObject tempObject = tempObjects.get(i);
                /*if(tempObject instanceof Net){
                    System.out.println("NEEEEEEEEEEEEEEEEEEEEEEEEEEEEET");
                    System.out.println(tempObject.getDeadTime() + " " + map.getTurn());
                }*/
                if (tempObject.getDeadTime() == map.getTurn()) {
                    // System.out.println("Dead Tiiiiiiiiiiiiiiiiiiime!");
                    diff.del(tempObject.getId());
                    Cell cell = cells[tempObject.getPosition().getRow()][tempObject.getPosition().getColumn()];
                    if (tempObject instanceof Net) {
                        cell.setNet(null);
                    } else {
                        cell.setContent(null);
                    }
                    tempObjects.remove(i);
                }
            }
        }
    }


    private void stageAddRandomTempObject() {
        int foodValidTime = this.map.getConstants().getFoodValidTime();
        int trashValidTime = this.map.getConstants().getTrashValidTime();
        int netValidTime = this.map.getConstants().getNetValidTime();
        int turn = this.map.getTurn();
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.H; j++) {

                if (cells[i][j].getContent() == null && cells[i][j].getTeleport() == null) {


                    double r0 = Math.random();
                    if (r0 < this.map.getConstants().getFoodProb()) {
                        Food food = new Food(idCounter++, cells[i][j]);
                        food.setDeadTime(foodValidTime + turn);
                        this.tempObjects.add(food);
                        cells[i][j].setContent(food);
                        diff.add(food.getId(), 1, i, j); // todo: check with clients
                    } else {
                        double r1 = Math.random();
                        if (r1 < this.map.getConstants().getTrashProb()) {
                            Trash trash = new Trash(idCounter++, cells[i][j]);
                            trash.setDeadTime(trashValidTime + turn);
                            this.tempObjects.add(trash);
                            cells[i][j].setContent(trash);
                            diff.add(trash.getId(), 2, i, j);
                        }
                    }
                }
                if (cells[i][j].getNet() == null) {
                    double r2 = Math.random();
                    if (r2 < this.map.getConstants().getNetProb()) {
                        Net net = new Net(idCounter++, cells[i][j]);
                        net.setDeadTime(netValidTime + turn);
                        this.tempObjects.add(net);
                        cells[i][j].setNet(net);
                        diff.add(net.getId(), 3, i, j);
                    }
                }

            }
        }
    }


    /**
     * Switch Teleports!
     */
    private void stageSwitchTeleports() {
        // TELEPORT SWITCH
        teleportChecked.clear();
        for (int i = 0; i < teleports.size(); i++) {
            Teleport teleport = teleports.get(i);
            Teleport pairTeleport = teleport.getPair().getTeleport(); // todo: fix it if you have extra time and energy!
            Cell cell = teleport.getPosition();
            Cell pairCell = pairTeleport.getPosition();
            GameObject content = cell.getContent();
            GameObject pairContent = pairCell.getContent();
            if (!teleportChecked.contains(teleport.getId()) &&
                    !teleportChecked.contains(pairTeleport.getId())) {
                teleportChecked.add(teleport.getId());
                // SWAP
                cell.setContent(pairContent);
                pairCell.setContent(content);
                if (content != null) {
                    content.setPosition(pairCell);
                    fishAlters.add(content.getId());
                }
                if (pairContent != null) {
                    pairContent.setPosition(cell);
                    fishAlters.add(pairContent.getId());
                }
            }
        }
    }

    private void stageAlters() {
        for (int ind = 0; ind < 2; ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                Fish fish = fishes[ind].get(i);
                if (fishAlters.contains(fish.getId())) {
                    diff.alterFish(fish.getId(), fish.getPosition().getRow(), fish.getPosition().getColumn(), fish.getColorNumber(), (fish.isSick()) ? 1 : 0);
                }
            }
        }
        for (int i = 0; i < tempObjects.size(); i++) {
            if (fishAlters.contains(tempObjects.get(i).getId())) {
                diff.alterItem(tempObjects.get(i).getId(), tempObjects.get(i).getPosition().getRow(), tempObjects.get(i).getPosition().getColumn());
            }
        }
    }

    private void sickZombie(Fish fish, int k) {
        int row = fish.getPosition().getRow(), neighbourRow;
        int col = fish.getPosition().getColumn(), neighbourCol;
        int[] tempA = new int[2];
        int ind;
        tempA[0] = map.getScore()[0];
        tempA[1] = map.getScore()[1];
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                /**
                 * getDeadTime == turn is true ?
                 */

                neighbourRow = makeValidIndex(row + i, H);
                neighbourCol = makeValidIndex(col + j, W);
                if (cells[neighbourRow][neighbourCol].getContent() instanceof Fish &&
                        !((Fish) cells[neighbourRow][neighbourCol].getContent()).isSick()) {
                    ind = ((Fish) cells[neighbourRow][neighbourCol].getContent()).getTeamNumber();
                    tempA[1 - ind] += gc.getSickCost();
                }
            }
        }
        map.setScore(tempA);
    }

    private void deleteFish(Fish fish, int i) {
        if(i < 0) {
            return;
        }
        fishes[fish.getTeamNumber()].remove(i);
        nextCell[fish.getTeamNumber()].remove(i);
//        nextMove[fish.getTeamNumber()].remove(i);
        // queens --
        if (fish.isQueen())
            numberOfQueens[fish.getTeamNumber()]--;
        //DIFF.delete();
        //diff.del(fish.getId());
        // cell
        /**
         * handle null
         */
        fish.getPosition().setContent(null);
    }

    private void moveFish(Fish fish, Cell nxtCell/*, int nxtMove*/) {
        if(deletedFishes.contains(fish.getId())) {
            return;
        }
        boolean moved = false;
        if (!fish.getPosition().equals(nxtCell)) {
            moved = true;
            fish.setPower(fish.getPower() + 1);
        }
        //ATTENTION TARTIB MOHEM DAR OBJECT CELL
        if (fish.equals(fish.getPosition().getContent()) && moved) {
            fish.getPosition().setContent(null); /// NULL
        }
        //////////////// if fish is pregnant add new baby on old position
        if (fish.isPregnant()) {
            newBorn[fish.getTeamNumber()][fish.getPosition().getRow()][fish.getPosition().getColumn()] = true;
            motherFish[fish.getTeamNumber()][fish.getPosition().getRow()][fish.getPosition().getColumn()] = fish;
            fish.setPregnant(false);
        }
        // NEW POSITION
        fish.setPosition(nxtCell);
        ////// define changes after fish moved
        // set sick & deadtime & fishAlters & trash remove & diff trash
        if (nxtCell.getContent() instanceof Trash) {
            Trash trash = (Trash) nxtCell.getContent();
            /**
             * turn
             */
            if (!fish.isSick()) {
                fish.setSick(true);
                fish.setDeadTime(map.getTurn() + gc.getSickLifeTime());
                fishAlters.add(fish.getId());
            }

            diff.del(trash.getId());
            tempObjects.remove(trash);
        } else if (nxtCell.getContent() instanceof Food) {
            Food food = (Food) nxtCell.getContent();

            if (fish.isQueen()) {
                int tempA[] = new int[2];
                tempA[fish.getTeamNumber()] = map.getScore()[fish.getTeamNumber()] + gc.getQueenFoodScore();
                tempA[1 - fish.getTeamNumber()] = map.getScore()[1 - fish.getTeamNumber()];
                map.setScore(tempA);
                //score[ind] += gc.getQueenFoodScore();
            } else {
                int tempA[] = new int[2];
                tempA[fish.getTeamNumber()] = map.getScore()[fish.getTeamNumber()] + gc.getFishFoodScore();
                tempA[1 - fish.getTeamNumber()] = map.getScore()[1 - fish.getTeamNumber()];
                map.setScore(tempA);
                //score[ind] += gc.getFishFoodScore();
            }
            fish.setPregnant(true);

            diff.del(food.getId());
            tempObjects.remove(food);
            // DIF FOOD DELETE
        }
        nxtCell.setContent(fish);
        // DIFF.move
        /**
         * mv r dir
         */
        //diff.mov(fish.getId(),nxtMove);

    }

    private boolean isNetDeadTime(Cell cell) {
        int row = cell.getRow(), neighbourRow;
        int col = cell.getColumn(), neighbourCol;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                /**
                 * getDeadTime == turn is true ?
                 */
                neighbourRow = makeValidIndex(row + i, H);
                neighbourCol = makeValidIndex(col + j, W);
                if (cells[neighbourRow][neighbourCol].getNet() != null &&
                        cells[neighbourRow][neighbourCol].getNet().getDeadTime() == map.getTurn()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void makeBabyFish(Cell cell, Fish fish) {
        /**
         * new FISH ?
         */
        Fish baby = new Fish(idCounter++, cell, fish.getTeamNumber(), fish.getDirection(), fish.getColorNumber(), fish.isQueen());
        cell.setContent(baby);
        fishes[fish.getTeamNumber()].add(baby);
        diff.addFish(baby.getId(), 0, baby.getPosition().getRow(), baby.getPosition().getColumn(),
                baby.getDirection(), baby.getColorNumber(), (baby.isQueen()) ? 1 : 0, baby.getTeamNumber());
        //int id, int type, int x, int y, int dir, int color, int queen, int team
    }

    private Cell getNextCellViaUpdate(Fish fish) {
        int left, right, head, mv;
        int row, col;
        // Right - neighbour
        // 0: ally 1:enemy 2:blank
        row = (fish.getPosition().getRow() + rowRightDir[fish.getDirection()] + H) % H;
        col = (fish.getPosition().getColumn() + colRightDir[fish.getDirection()] + W) % W;
        right = getUpdateINdex(fish, cells[row][col].getContent());

        // left - neighbour
        row = (fish.getPosition().getRow() + rowLeftDir[fish.getDirection()] + H) % H;
        col = (fish.getPosition().getColumn() + colLeftDir[fish.getDirection()] + W) % W;
        left = getUpdateINdex(fish, cells[row][col].getContent());

        // Head

        row = (fish.getPosition().getRow() + rowHeadDir[fish.getDirection()] + H) % H;
        col = (fish.getPosition().getColumn() + colHeadDir[fish.getDirection()] + W) % W;

        while (!(cells[row][col].getContent() instanceof Fish)) {
            row = (row + rowHeadDir[fish.getDirection()] + H) % H;
            col = (col + colHeadDir[fish.getDirection()] + W) % W;
        }

        head = getUpdateINdex(fish, cells[row][col].getContent());
        // delete
        //if(head > 1) head =0; // tofmali :)
        /**
         * is possible that values not be 0 to 2
         */
        // set direction & return next
        mv = update[fish.getTeamNumber()][fish.getColorNumber()][right][head][left];
        // TODO: 2/9/2017 DISOBEY
        /**
         * DisobeyNum is for example 50 in the real Game
         */
        int disObeyNum = this.map.getConstants().getDisobeyNum();
        int teamNum = fish.getTeamNumber();
        int j = 0;
        if (fishes[teamNum].size() > disObeyNum) {
            int dTurn = (int) Math.abs(1 / (1 - Math.sqrt(Math.min(disObeyNum / fishes[teamNum].size(), 1))));
            for (int i = 0; i < fishes[teamNum].size(); i++) {
                if (fishes[teamNum].get(i).getId() == fish.getId()) {
                    j = i;
                    break;
                }
            }

            if (this.map.getTurn() - j % dTurn == 0) {
                Random rand = new Random();
                mv = rand.nextInt(2);
            }
        }


//        nextMove[fish.getTeamNumber()].add(mv);

        return getNextCellViaMove(fish, mv);

    }

    // FISHCHANGES MOVE
    private Cell getNextCellViaMove(Fish fish, int mv) {
        nextMoveMap.put(fish.getId(), mv);
        int row, col, dir;
        row = fish.getPosition().getRow();
        col = fish.getPosition().getColumn();
        dir = fish.getDirection();
        switch (mv) {
            case 0:
                dir = makeValidIndex(dir - 1, 4);
                break;
            case 1:
                row = (fish.getPosition().getRow() + rowHeadDir[fish.getDirection()] + H) % H;
                col = (fish.getPosition().getColumn() + colHeadDir[fish.getDirection()] + W) % W;
                attacks[row][col][fish.getTeamNumber()].add(fish);
                break;
            case 2:
                dir = makeValidIndex(dir + 1, 4);
        }
//        if (dir == fish.getDirection()) {
//            fishChanges.put(fish.getId(), "move");
//        }
        fish.setDirection(dir);

        ///////////// TELEPORT
       /*
        Cell destination;
        if(cells[row][col].getTeleport() instanceof Teleport){
            destination = ((Teleport) cells[row][col].getTeleport()).getPair();
            row = destination.getRow();
            col = destination.getColumn();
        }
        */
//        attacks[row][col][fish.getTeamNumber()].add(fish);

        return cells[row][col];
    }

    private void changeScores(int ind, int val) {
        int tempA[] = new int[2];
        //tempA[ind] = map.getScore()[ind] - val;
        tempA[ind] = map.getScore()[ind];
        tempA[1 - ind] = map.getScore()[1 - ind] + val;
        map.setScore(tempA);
        //score[ind]-=val;
        //score[1-ind]+=val;
    }

    private int makeValidIndex(int val, int mod) {
        return (val + mod) % mod;
    }

    private int getUpdateINdex(Fish fish, GameObject neighbour) {

        if (neighbour instanceof Fish) {
            if (((Fish) neighbour).getTeamNumber() == fish.getTeamNumber())
                return 0;
            else
                return 1;
        } else
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
        Object[] args0 = {map.getTurn(), map.getScore(), diff.getChanges()};
        messages = new Message(name0, args0);
        return messages;
    }

    /*
    It is remained:getStatusMessage
     */
    @Override
    public Message getStatusMessage() {

        return new Message(Message.NAME_STATUS, new Object[]{this.map.getTurn(), this.map.getScore()[0], this.map.getScore()[1]});
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        String name0 = Message.NAME_TURN;
        Object[] args0 = {map.getTurn(), map.getScore(), diff.getChanges()};
        messages[0] = new Message(name0, args0);

        String name1 = Message.NAME_TURN;
        Object[] args1 = {map.getTurn(), map.getScore(), diff.getChanges()};
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
        if (numberOfQueens[0] == 0 && numberOfQueens[1] > 0) {
            finalScore[0] = 0;
        } else if (numberOfQueens[1] == 0 && numberOfQueens[0] > 0) {
            finalScore[1] = 0;
        }
        map.setScore(finalScore);
        int population = fishes[0].size() + fishes[1].size();
        if (((float) population) / (this.map.getH() * this.map.getW()) >= this.map.getConstants().getEndRatio()) {
            return true;
        }
        if (numberOfQueens[0] == 0 || numberOfQueens[1] == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void terminate() {
        this.debugUI.gameOver();
        /*
        if (debugUI != null) {
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), null, getStatusMessage(), null, null);
        }
        */
    }
}


class Pair<K, V> {
    K first;
    V second;

    Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }
}