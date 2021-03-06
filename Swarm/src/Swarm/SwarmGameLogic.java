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


public class SwarmGameLogic implements GameLogic {
    private static final String TAG = "Swarm";

    public static final IntegerParam PARAM_CLIENT_TIMEOUT = new IntegerParam("ClientTimeout", 500);
    public static IntegerParam PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", 1000);
    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static final BooleanParam PARAM_SHOW_DEBUG_UI = new BooleanParam("ShowDebugUI", true);

    private MapFrame debugUI;
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

    int[] detMoveLimit;

    int[] changeColorLimit;

    private boolean[][][] newBorn;
    private int[][][] mark;
    private Cell[][][] moves;
    private int[][][] moves_valid;
    private int[][][] dfs_loop_data;
    private int[][][] dfs_reverse_data;

    private boolean firstTurn;
    private int numberOfQueens[];

    ArrayList<Cell>[] nextCell;

    private ArrayList<Fish>[] fishes;

    private Fish[][][] motherFish;
    private ArrayList<GameObject> tempObjects;
    private ArrayList<Teleport> teleports;
    private Cell[][] cells;
    private ArrayList<Fish>[][][] attacks;

    HashMap<Integer, Integer> nextMoveMap;
    ArrayList<Event>[] detMoves;
    ArrayList<Pair<Fish, String>> fishChanges;
    ArrayList<Pair<Fish, String>> moveChanges;
    HashSet<Integer> deletedFishes;
    Set<Integer> fishesChanged;
    HashSet<Integer> fishAlters;
    HashSet<Integer> teleportChecked;

    private double[][] foodProbs;
    private double[][] trashProbs;
    private double[][] netProbs;

    private Message uiMessage;

    public static void main(String[] args) throws InterruptedException {
        GameServer gameServer = new GameServer(new SwarmGameLogic(), args);
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
        this.gc = this.map.getConstants();

        this.foodProbs = new double[H][W];
        this.trashProbs = new double[H][W];
        this.netProbs = new double[H][W];

        probabilityMaker();

        PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", (int) this.gc.getTurnTimeout());

        this.fishes = this.map.getFishes();
        this.tempObjects = this.map.getTempObjects();
        this.teleports = this.map.getTeleports();

        this.cells = this.map.getCells();
        this.idCounter = this.map.getIdCounter();
        this.moveChanges = new ArrayList<>();


        initialize();

        if (PARAM_SHOW_DEBUG_UI.getValue() == Boolean.TRUE) {
            debugUI = new MapFrame(this.map);
        }
    }

    private void initialize() {
        idCounter++;
        nextMoveMap = new HashMap<>();
        fishChanges = new ArrayList<>();
        deletedFishes = new HashSet<>();
        fishesChanged = new HashSet<>();
        fishAlters = new HashSet<>();
        teleportChecked = new HashSet<>();
        this.numberOfQueens = new int[gc.getTeamNum()];
        this.update = new int[gc.getTeamNum()][2][3][2][3];
        mark = new int[2][H][W];
        moves = new Cell[2][H][W];
        moves_valid = new int[2][H][W];
        dfs_reverse_data = new int[2][H][W];
        dfs_loop_data = new int[2][H][W];

        attacks = new ArrayList[H][W][2];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind] = new ArrayList<>();
                }

            }
        }

        update = new int[gc.getTeamNum()][2][3][2][3];
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


        numberOfQueens = new int[2];
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                if (fishes[ind].get(i).isQueen())
                    numberOfQueens[ind]++;
            }
        }

        firstTurn = true;

        detMoveLimit = new int[2];
        detMoveLimit[0] = this.gc.getDetMovLimit();
        detMoveLimit[1] = this.gc.getDetMovLimit();

        changeColorLimit = new int[2];
        changeColorLimit[0] = this.gc.getChangeColorLimit();
        changeColorLimit[1] = this.gc.getChangeColorLimit();
    }


    @Override
    public Message getUIInitialMessage() {

        Message msg;
        String name0 = Message.NAME_INIT;
        Object[] args0 = {0, this.map.getH(), this.map.getW(), this.map.getInitialFishes(), this.map.getInitialFoods(),
                this.map.getInitialTrashes(), this.map.getInitialNets(), this.map.getInitialTeleports(), this.map.getInitialConstants()};
        msg = new Message(name0, args0);
        return msg;
    }

    @Override
    public Message[] getClientInitialMessages() {

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


    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {

        if (firstTurn) {
            firstTurn = false;
            return;
        }
        map.setTurn(map.getTurn() + 1);

        diff = new Diff();

        nextCell[0].clear();
        nextCell[1].clear();

        detMoves[0].clear();
        detMoves[1].clear();

        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    attacks[i][j][ind].clear();
                }
                for (int t = 0; t < gc.getTeamNum(); t++) {
                    mark[t][i][j] = 0;
                    moves[t][i][j] = null;
                    moves_valid[t][i][j] = 0;
                    dfs_reverse_data[t][i][j] = 0;
                }
            }
        }

        newBorn = new boolean[gc.getTeamNum()][H][W];
        motherFish = new Fish[gc.getTeamNum()][H][W];

        fishChanges.clear();
        moveChanges.clear();
        deletedFishes.clear();
        fishesChanged.clear();
        fishAlters.clear();
        nextMoveMap.clear();
        teleportChecked.clear();


        // get arguments from client
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {

            loop:
            for (int i = 0; i < clientsEvent[ind].length; i++) {

                if (clientsEvent[ind][i].getType().equals("s")) {

                    if (clientsEvent[ind][i].getArgs().length != 5) continue;


                    int[] t = new int[5];
                    for (int j = 0; j < 5; j++) {
                        try {
                            t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                        } catch (NumberFormatException e) {
                            continue loop;
                        }
                    }
                    if (!(t[0] == 0 || t[0] == 1) || !(t[1] == 0 || t[1] == 1 || t[1] == 2) || !(t[2] == 0 || t[2] == 1) ||
                            !(t[3] == 0 || t[3] == 1 || t[3] == 2) || !(t[4] == 0 || t[4] == 1 || t[4] == 2)) {
                        continue;
                    }

                    if (update[ind][t[0]][t[1]][t[2]][t[3]] != t[4]) {

                        update[ind][t[0]][t[1]][t[2]][t[3]] = t[4];
                        changeScores(ind, gc.getUpdateCost());

                    }

                } else if (clientsEvent[ind][i].getType().equals("m") && detMoveLimit[ind] > 0) {

                    if (clientsEvent[ind][i].getArgs().length != 2) continue;

                    int[] t = new int[2];
                    for (int j = 0; j < 2; j++) {
                        try {
                            t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                        } catch (NumberFormatException e) {
                            continue loop;
                        }
                    }

                    int flag = 0;
                    for (int j = 0; j < fishes[ind].size(); j++) {
                        if (fishes[ind].get(j).getId() == t[0]) {
                            flag = 1;
                        }
                    }

                    if (flag == 0) {
                        continue;
                    }

                    if (!(t[1] == 0 || t[1] == 1 || t[1] == 2)) {
                        continue;
                    }

                    detMoves[ind].add(clientsEvent[ind][i]);
                    detMoveLimit[ind]--;
                    changeScores(ind, gc.getDetMoveCost());

                } else if (clientsEvent[ind][i].getType().equals("c") && changeColorLimit[ind] > 0) {
                    if (clientsEvent[ind][i].getArgs().length != 2) continue;

                    int[] t = new int[2];
                    for (int j = 0; j < 2; j++) {
                        try {
                            t[j] = Integer.parseInt(clientsEvent[ind][i].getArgs()[j]);
                        } catch (NumberFormatException e) {
                            continue loop;
                        }
                    }

                    if (!(t[1] == 0 || t[1] == 1)) {
                        continue;
                    }

                    for (int j = 0; j < fishes[ind].size(); j++) {

                        if (fishes[ind].get(j).getTeamNumber() == ind && fishes[ind].get(j).getId() == t[0] &&
                                !fishAlters.contains(fishes[ind].get(j).getId())) {
                            fishes[ind].get(j).setColorNumber(t[1]);
//                            changeScores(ind, gc.getColorCost());
                            fishAlters.add(fishes[ind].get(j).getId());
                            changeColorLimit[ind]--;
                        }
                    }
                }
            }
        }

        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                nextCell[ind].add(getNextCellViaUpdate(fishes[ind].get(i)));
            }
        }

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

//        int[] finalScore = new int[2];
//        finalScore[0] = map.getScore()[0];
//        finalScore[1] = map.getScore()[1];
//        if (numberOfQueens[0] == 0 && numberOfQueens[1] > 0) {
//            finalScore[0] = 0;
//            } else if (numberOfQueens[1] == 0 && numberOfQueens[0] > 0) {
//            finalScore[1] = 0;
//
//        }
//        map.setScore(finalScore);

    }


    private void stageMakeAttacks() {

        for (int i = 0; i < this.H; i++) {
            for (int j = 0; j < this.W; j++) {
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

                for (int ind = 0; ind < gc.getTeamNum(); ind++) {
                    int attacks_num[] = {attacks[i][j][0].size(), attacks[i][j][1].size()};
                    if (cellFish != null)
                        attacks_num[cellFish.getTeamNumber()]++;
                    if ((powerSum[1 - ind] == 0 && powerSum[ind] == 0 && attacks_num[1 - ind] > attacks_num[ind])
                            || powerSum[1 - ind] > this.gc.getPowerRatio() * powerSum[ind]) {
                        for (Fish f : attacks[i][j][1 - ind]) {
                            moves[1 - ind][f.getPosition().getRow()][f.getPosition().getColumn()] = cells[i][j];
                        }
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

                            }
                        }

                        for (int k = 0; k < attacks[i][j][ind].size(); k++) {
                            Fish fish = attacks[i][j][ind].get(k);
                            if (fish.isQueen() && !fishesChanged.contains(fish.getId())) {
                                changeScores(ind, gc.getKillQueenScore());
                            }
                            if (!fish.isQueen() && !fishesChanged.contains(fish.getId())) {
                                changeScores(ind, gc.getKillFishScore());
                            }
                            fishesChanged.add(fish.getId());
                            fishChanges.add(new Pair<>(fish, "delete"));
                            deletedFishes.add(fish.getId());

                        }
                    } else {
                        for (Fish f : attacks[i][j][1 - ind]) {

                            int r = f.getPosition().getRow(), c = f.getPosition().getColumn();
                            moves[1 - ind][r][c] = cells[r][c];
                        }
                    }
                }
            }
        }
        /**
         * Do not change these codes
         */
        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                Cell cell = fishes[ind].get(i).getPosition();
                Cell ncell = nextCell[ind].get(i);
                if (ncell == fishes[ind].get(i).getPosition()) {
                    moves[ind][ncell.getRow()][ncell.getColumn()] = ncell;
                }
                if (cell == moves[ind][cell.getRow()][cell.getColumn()]) {
                    mark[ind][cell.getRow()][cell.getColumn()] = 1;
                }
            }
        }
    }


    private void stageDeleteAndMove() {
        Log.i(TAG, "changes: " + fishChanges.toString());


        for (Pair<Fish, String> fishChange : fishChanges) {
            Fish fish = fishChange.first;
            String change = fishChange.second;
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

            int total_chain = 0;
            for (int r = 0; r < this.H; r++) {
                for (int c = 0; c < this.W; c++) {
                    if (moves[t][r][c] == null && mark[t][r][c] == 0) {
                        total_chain += dfs_reverse(t, r, c, 1, 0);
                        mark[t][r][c] = 1;
                    }
                }
            }

            for (int r = 0; r < this.H; r++) {
                for (int c = 0; c < this.W; c++) {
                    if (moves[t][r][c] != null && mark[t][r][c] == 0) {
                        total_chain += dfs_loop(t, r, c, 0);
                    }
                }
            }

            ArrayList[] valid_moves = new ArrayList[W * H];
            for (int r = 0; r < this.H; r++) {
                for (int c = 0; c < this.W; c++) {
                    int i = moves_valid[t][r][c];
                    if (i != 0) {
                        if (valid_moves[i] == null)
                            valid_moves[i] = new ArrayList();
                        if (cells[r][c].getContent() != null && cells[r][c].getContent() instanceof Fish) {
                            valid_moves[i].add(new int[]{t, r, c});
                            fishesChanged.add(cells[r][c].getContent().getId());
                        }
                    }
                }
            }

            for (ArrayList valid_move : valid_moves) {
                if (valid_move != null) {
                    for (Object move : valid_move) {
                        int[] move_arr = (int[]) move;
                        moveChanges.add(new Pair<>((Fish) cells[move_arr[1]][move_arr[2]].getContent(), "move"));
                    }
                }
            }
        }

        for (Pair<Fish, String> moveChange : moveChanges) {
            Fish fish = moveChange.first;
            String change = moveChange.second;
            int team = fish.getTeamNumber();
            if (change.equals("move")) {
                int r = fish.getPosition().getRow(), c = fish.getPosition().getColumn();
                moveFish(fish, moves[team][r][c]);
            }
        }

    }


    private int dfs_reverse(int t, int r, int c, int valid_index, int valid_mark) {
//        System.out.println("dfs rev on " + r + ", " + c + ", " + valid_index);
        if (valid_index > 0 && moves[t][r][c] != null)
            moves_valid[t][r][c] = valid_index;
        if (mark[t][r][c] != valid_mark)
            return dfs_reverse_data[t][r][c];
        mark[t][r][c] = valid_mark + 1;
        boolean has_max = false;
        int max = 0, maxr = 0, maxc = 0;
        int num_in = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0)
                    continue;
                int row = makeValidIndex(r + i, H);
                int col = makeValidIndex(c + j, W);
                if (moves[t][row][col] == cells[r][c]) {
                    num_in++;
                    maxr = row;
                    maxc = col;
                }
            }
        }
        if (num_in == 1) {
            max = dfs_reverse(t, maxr, maxc, 0, valid_mark);
            has_max = true;
        }
        if (num_in > 1) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0)
                        continue;
                    int row = makeValidIndex(r + i, H);
                    int col = makeValidIndex(c + j, W);
                    if (moves[t][row][col] == cells[r][c]) {
                        int val = dfs_reverse(t, row, col, 0, valid_mark);
                        if (val > max) {
                            has_max = true;
                            max = val;
                            maxr = row;
                            maxc = col;
                        }
                    }
                }
            }
        }
        if (has_max && valid_index != 0) {
            dfs_reverse(t, maxr, maxc, valid_index + 1, valid_mark + 1);
        }
        dfs_reverse_data[t][r][c] = max + 1;
        return max + 1;
    }

    private int dfs_loop(int t, int r, int c, int d) {

        if (mark[t][r][c] == 1) {
            return 0;
        }
        mark[t][r][c] = 2;
        dfs_loop_data[t][r][c] = d;
        if (moves[t][r][c] == null) {
            mark[t][r][c] = 1;
            return 0;
        }
        int r2 = moves[t][r][c].getRow();
        int c2 = moves[t][r][c].getColumn();
        if (mark[t][r2][c2] == 2 && dfs_loop_data[t][r2][c2] != d + 1) {
            moves_valid[t][r][c] = d + 1;
            mark[t][r][c] = 1;
            return d +/* 1*/ -dfs_loop_data[t][r2][c2];
        }

        int dl = dfs_loop(t, r2, c2, d + 1);
        if (dl > 0) {
            moves_valid[t][r][c] = d + 1;
            mark[t][r][c] = 1;
            return dl - 1;
        }

        mark[t][r][c] = 1;
        return 0;
    }

    private void stageSickZombie() {
        for (int ind = 0; ind < 2; ind++) {
            for (int i = 0; i < fishes[ind].size(); i++) {
                Fish fish = fishes[ind].get(i);
                if (fish.isSick()) {
                    sickZombie(fish);
                }
            }
        }
    }

    private void stageNetDeath() {
        for (int i = 0; i < this.H; i++) {
            for (int j = 0; j < this.W; j++) {

                if (cells[i][j].getContent() instanceof Fish) {
                    if (isNetDeadTime(cells[i][j])) {
                        Fish fish = (Fish) cells[i][j].getContent();
                        fishChanges.add(new Pair<>(fish, "delete"));
                        deletedFishes.add(fish.getId());
                        fishesChanged.add(fish.getId());
                        deleteFish(fish, fishes[fish.getTeamNumber()].indexOf(fish));
                        if (fish.isQueen()) {
                            changeScores(fish.getTeamNumber(), (int) (0.5 * gc.getKillQueenScore()));
                        } else {
                            changeScores(fish.getTeamNumber(), (int) (0.5 * gc.getKillFishScore()));
                        }
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
            } else if (str.equals("move") && !deletedFishes.contains(fish.getId())) {
                diff.mov(fish.getId(), nextMoveMap.get(fish.getId()));
            }
        }
    }

    private void stageNewBorn() {

        for (int ind = 0; ind < gc.getTeamNum(); ind++) {
            for (int i = 0; i < this.H; i++) {
                for (int j = 0; j < this.W; j++) {

                    if (newBorn[ind][i][j] && cells[i][j].getContent() == null) {
                        makeBabyFish(cells[i][j], motherFish[ind][i][j]);
                    } else if (newBorn[ind][i][j] && cells[i][j].getContent() != null) {
                        loop:
                        for (int k = -1; k <= 1; k++) {
                            for (int l = -1; l <= 1; l++) {
                                if (k == 0 && l == 0)
                                    continue;
                                if (cells[(k + i + this.H) % this.H][(l + j + this.W) % this.W].getContent() == null) {
                                    makeBabyFish(cells[(k + i + this.H) % H][(l + j + this.W) % W], motherFish[ind][i][j]);
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

        if (tempObjects.size() > 0) {

            for (int i = tempObjects.size() - 1; i >= 0; i--) {
                GameObject tempObject = tempObjects.get(i);

                if (tempObject.getDeadTime() == map.getTurn()) {
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
        for (int i = 0; i < this.H; i++) {
            for (int j = 0; j < this.W; j++) {

                if (cells[i][j].getContent() == null && cells[i][j].getTeleport() == null) {
                    double r0 = Math.random();
                    if (r0 < this.foodProbs[i][j]) {
                        Food food;
                        food = new Food(idCounter++, cells[i][j]);
                        food.setDeadTime(foodValidTime + turn);
                        this.tempObjects.add(food);
                        cells[i][j].setContent(food);
                        diff.add(food.getId(), 1, i, j);
                    } else if (cells[i][j].getContent() == null) {
                        double r1 = Math.random();
                        if (r1 < this.trashProbs[i][j]) {
                            Trash trash;
                            trash = new Trash(idCounter++, cells[i][j]);
                            trash.setDeadTime(trashValidTime + turn);
                            this.tempObjects.add(trash);
                            cells[i][j].setContent(trash);
                            diff.add(trash.getId(), 2, i, j);
                        }
                    }
                }
                if (cells[i][j].getNet() == null) {
                    double r2 = Math.random();
                    if (r2 < this.netProbs[i][j]) {
                        Net net;
                        net = new Net(idCounter++, cells[i][j]);
                        net.setDeadTime(netValidTime + turn);
                        this.tempObjects.add(net);
                        cells[i][j].setNet(net);
                        diff.add(net.getId(), 3, i, j);
                    }
                }

            }
        }
    }


    private void stageSwitchTeleports() {

        teleportChecked.clear();

        for (int i = 0; i < teleports.size(); i++) {
            Teleport teleport;
            teleport = teleports.get(i);
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
                int bsick = 0;
                if (fish.isSick()) {
                    bsick = 1;
                }
                if (fishAlters.contains(fish.getId())) {
                    diff.alterFish(fish.getId(), fish.getPosition().getRow(), fish.getPosition().getColumn(), fish.getColorNumber(), bsick);
                }
            }
        }

        for (int i = 0; i < tempObjects.size(); i++) {
            if (fishAlters.contains(tempObjects.get(i).getId())) {
                diff.alterItem(tempObjects.get(i).getId(), tempObjects.get(i).getPosition().getRow(), tempObjects.get(i).getPosition().getColumn());
            }
        }
    }

    private void sickZombie(Fish fish) {

        int row = fish.getPosition().getRow(), neighbourRow;
        int col = fish.getPosition().getColumn(), neighbourCol;
        int[] tempA = new int[2];
        int ind;
        tempA[0] = map.getScore()[0];
        tempA[1] = map.getScore()[1];
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                neighbourRow = makeValidIndex(row + i, H);
                neighbourCol = makeValidIndex(col + j, W);
                if (cells[neighbourRow][neighbourCol].getContent() != null &&
                        cells[neighbourRow][neighbourCol].getContent() instanceof Fish &&
                        !((Fish) cells[neighbourRow][neighbourCol].getContent()).isSick()) {
                    ind = ((Fish) cells[neighbourRow][neighbourCol].getContent()).getTeamNumber();
                    tempA[1 - ind] += gc.getSickCost();
                }
            }
        }
        map.setScore(tempA);
    }

    private void deleteFish(Fish fish, int i) {
        if (i < 0) {
            return;
        }
        fishes[fish.getTeamNumber()].remove(i);
        nextCell[fish.getTeamNumber()].remove(i);

        if (fish.isQueen())
            numberOfQueens[fish.getTeamNumber()]--;

        fish.getPosition().setContent(null);
    }

    private void moveFish(Fish fish, Cell nxtCell) {
        Cell cell = fish.getPosition();

        if (deletedFishes.contains(fish.getId())) {
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
        if (nxtCell.getContent() != null) {
            if (nxtCell.getContent() instanceof Trash) {
                Trash trash;
                trash = (Trash) nxtCell.getContent();
                if (!fish.isSick()) {
                    fish.setSick(true);
                    fish.setDeadTime(map.getTurn() + gc.getSickLifeTime());
                    fishAlters.add(fish.getId());
                }

                diff.del(trash.getId());
                tempObjects.remove(trash);
            } else if (nxtCell.getContent() instanceof Food) {
                Food food;
                food = (Food) nxtCell.getContent();

                if (fish.isQueen()) {
                    int tempA[] = new int[2];
                    tempA[fish.getTeamNumber()] = map.getScore()[fish.getTeamNumber()] + gc.getQueenFoodScore();
                    tempA[1 - fish.getTeamNumber()] = map.getScore()[1 - fish.getTeamNumber()];
                    map.setScore(tempA);
                } else if (!fish.isQueen()) {
                    int tempA[] = new int[2];
                    tempA[fish.getTeamNumber()] = map.getScore()[fish.getTeamNumber()] + gc.getFishFoodScore();
                    tempA[1 - fish.getTeamNumber()] = map.getScore()[1 - fish.getTeamNumber()];
                    map.setScore(tempA);
                }
                fish.setPregnant(true);

                diff.del(food.getId());
                tempObjects.remove(food);
            }
        }
        if (!nxtCell.equals(cell)) {
            fishChanges.add(new Pair<>(fish, "move"));
            nxtCell.setContent(fish);
        }
        if (nxtCell.equals(cell) && nextMoveMap.get(fish.getId()) != 1) {

            fishChanges.add(new Pair<>(fish, "move"));
            nxtCell.setContent(fish);

        }

    }

    private boolean isNetDeadTime(Cell cell) {
        int row = cell.getRow(), neighbourRow;
        int col = cell.getColumn(), neighbourCol;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                neighbourRow = makeValidIndex(row + i, this.H);
                neighbourCol = makeValidIndex(col + j, this.W);
                if (cells[neighbourRow][neighbourCol].getNet() != null &&
                        cells[neighbourRow][neighbourCol].getNet().getDeadTime() == map.getTurn()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void makeBabyFish(Cell cell, Fish fish) {

        Fish baby = new Fish(idCounter++, cell, fish.getTeamNumber(), fish.getDirection(), fish.getColorNumber(), fish.isQueen());
        cell.setContent(baby);
        fishes[fish.getTeamNumber()].add(baby);
        if (fish.isQueen()) {
            numberOfQueens[fish.getTeamNumber()]++;
        }
        diff.addFish(baby.getId(), 0, baby.getPosition().getRow(), baby.getPosition().getColumn(),
                baby.getDirection(), baby.getColorNumber(), (baby.isQueen()) ? 1 : 0, baby.getTeamNumber());
    }

    private void Wrongid() {
        for (int t = 0; t < 2; t++) {
            for (int i = 0; i < fishes[t].size(); i++) {
                Fish fish = fishes[t].get(i);
                Cell cell = fish.getPosition();
                if (cell.getContent() == null) {
                    System.out.println("Wrong id null id:" + fish.getId());
                } else if (cell.getContent() != null && fish.getId() != cell.getContent().getId()) {
                    if (cell.getContent() instanceof Fish) {
                        System.out.println("Wrong id1: " + fish.getId() + " id2 " + cell.getContent().getId());
                        System.out.println("Cell cl: " + cell.getColumn() + "row:" + cell.getRow());
                    } else {
                        System.out.println("Wrong entity");
                    }

                }
            }
        }
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

        // set direction & return next

        mv = update[fish.getTeamNumber()][fish.getColorNumber()][right][head][left];

        /**
         * Deterministic Move
         */
        int TeamNum = fish.getTeamNumber();
        int size = detMoves[TeamNum].size();
        for (int i = 0; i < size; i++) {
            String[] args = detMoves[TeamNum].get(i).getArgs();
            try {
                int id = Integer.parseInt(args[0]);
                int mov = Integer.parseInt(args[1]);
                if (id == fish.getId()) {
                    mv = mov;

                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        /**
         * Disobey
         */
        // TODO: 2/26/2017 Disobey is not handled yet
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
            if ((this.map.getTurn() - j) % dTurn == 0) {
                Random rand = new Random();
//                mv = rand.nextInt(3);
            }
        }


        return getNextCellViaMove(fish, mv);

    }

    private Cell getNextCellViaMove(Fish fish, int mv) {
        // TODO: 2/14/2017 Tofe khales

        nextMoveMap.put(fish.getId(), mv);
        if (mv == 0 || mv == 2) {
            moveFish(fish, fish.getPosition());
        }

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

        fish.setDirection(dir);


        return cells[row][col];
    }

    private void changeScores(int ind, int val) {
        int tempA[] = new int[2];
        tempA[ind] = map.getScore()[ind];
        tempA[1 - ind] = map.getScore()[1 - ind] + val;
        map.setScore(tempA);

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
        } else {
            return 2;
        }
    }

    @Override
    public void generateOutputs() {
        if (PARAM_SHOW_DEBUG_UI.getValue() == Boolean.TRUE) {
            debugUI.setMap(this.map);
        }
    }

    @Override
    public Message getUIMessage() {
        Message messages;
        String name0 = Message.NAME_TURN;
        int[] scores = map.getScore();
        int turn = map.getTurn();
        Object[] args0 = {turn, scores, diff.getChanges()};
        messages = new Message(name0, args0);
        return messages;
    }

    @Override
    public Message getStatusMessage() {

        return new Message(Message.NAME_STATUS, new Object[]{this.map.getTurn(), this.map.getScore()[0], this.map.getScore()[1]});
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        String name0 = Message.NAME_TURN;
        int turn = map.getTurn();
        Object[] args0 = {turn, map.getScore(), diff.getChanges()};
        messages[0] = new Message(name0, args0);

        String name1 = Message.NAME_TURN;
        Object[] args1 = {turn, map.getScore(), diff.getChanges()};
        messages[1] = new Message(name1, args1);

        return messages;
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }


    @Override
    public boolean isGameFinished() {

        int population = fishes[0].size() + fishes[1].size();
        if (((float) population) / (this.map.getH() * this.map.getW()) >= this.map.getConstants().getEndRatio()) {
            return true;
        }
        if (this.map.getTurn() >= gc.getTotalTurn()) {
            return true;
        }
        if (numberOfQueens[0] == 0 || numberOfQueens[1] == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void terminate() {

        if (PARAM_SHOW_DEBUG_UI.getValue()) {
            this.debugUI.setMap(this.map);
            this.debugUI.gameOver();
        }
    }

    private void probabilityMaker() {
        for (int i = 0; i < this.H; i++) {
            for (int j = 0; j < this.W; j++) {
                double xi = -2 + (i * 4) / H;
                double yi = -2 + (j * 4) / W;
                this.foodProbs[i][j] = (1 / (2 * Math.PI)) * Math.exp(-(xi * xi + yi * yi) / (2)) * gc.getFoodProb() * 8;
                this.trashProbs[i][j] = (1 / (2 * Math.PI)) * Math.exp(-(xi * xi + yi * yi) / (2)) * gc.getTrashProb() * 8;
                this.netProbs[i][j] = (1 / (2 * Math.PI)) * Math.exp(-(xi * xi + yi * yi) / (2)) * gc.getNetProb() * 8;
            }

        }
//        for (int i = 0; i < H; i++) {
//            for (int j = 0; j < W; j++) {
//                System.out.printf(" %.12f ",(double)(foodProbs[i][j]));
//            }
//            System.out.println("");
//        }
    }
}


class Pair<K, V> {
    K first;
    V second;

    Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }
}