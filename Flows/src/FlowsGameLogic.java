import model.Event;
import models.Diff;
import network.data.Message;
import server.config.FileParam;
import server.config.IntegerParam;
import server.core.GameLogic;
import server.core.GameServer;
import util.Log;

import java.util.ArrayList;


/**
 * Created by pezzati on 1/28/16.
 */
public class FlowsGameLogic implements GameLogic {
    private static final String TAG = "Flows";

    private Context context;

    //Constants
    final static int escapeNum = 1;
    final static int increaseWithOwnership = 1;
    final static int increaseWithEdge = 1;
    final static double highCasualties = 1.0;
    final static double mediumCasualties = 2.0 / 3.0;
    final static double lowCasualties = 1.0 / 3.0;

    //Temps
    private int vertexNum;
    private int[] armyCount;
    private int[] ownership;
    private boolean[][] graph;
    private int[][] adjacencyList;

    private int[] movesDest;
    private int[] movesSize;
    private int[][] armyInV;

    private Message uiMessage;

    public static final IntegerParam PARAM_NUM_TURNS = new IntegerParam("Turns", 100);
    public static final IntegerParam PARAM_CLIENT_TIMEOUT = new IntegerParam("ClientTimeout", 500);
    public static final IntegerParam PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", 1000);
    public static final FileParam PARAM_MAP = new FileParam("Map", null);

    public static void main(String[] args) throws InterruptedException {
        GameServer gameServer = new GameServer(new FlowsGameLogic(), args);
        gameServer.waitForClients();
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
        this.context = new Context(PARAM_MAP.getValue());

        this.context.flush();

        vertexNum = this.context.getMap().getVertexNum();
        graph = this.context.getMap().getGraph();
        adjacencyList = this.context.getMap().getAdjacencyList();
    }

    @Override
    public Message getUIInitialMessage() {
        // todo: send army levels (low, medium, high)
        Object[] args = {this.context.getMap().getVertexNum(), Diff.max_low, Diff.max_normal,
                this.context.getMap().getAdjacencyList(), this.context.getUIDiffList()};
        String name = Message.NAME_INIT;
        return new Message(name, args);
    }

    @Override
    public Message[] getClientInitialMessages() {
        Message[] msg = new Message[2];
        String name0 = Message.NAME_INIT;
        Object[] args0 = {0, this.context.getMap().getAdjacencyList(), this.context.getDiffList(0)};
        msg[0] = new Message(name0, args0);

        String name1 = Message.NAME_INIT;
        Object[] args1 = {1, this.context.getMap().getAdjacencyList(), this.context.getDiffList(1)};
        msg[1] = new Message(name1, args1);
        return msg;
    }

    @Override
    public void simulateEvents(Event[] terminalEvent, Event[] environmentEvent, Event[][] clientsEvent) {
        armyCount = this.context.getMap().getArmyCount();
        ownership = this.context.getMap().getOwnership();

        movesDest = new int[vertexNum];
        movesSize = new int[vertexNum];
        armyInV = new int[2][vertexNum];

        int[] conflictedMoves = new int[vertexNum];

        ArrayList<Message> uiMessages = new ArrayList<>();

        for (int i = 0; i < vertexNum; i++) {
            movesDest[i] = -1;
            if (ownership[i] > -1) {
                armyInV[ownership[i]][i] = armyCount[i];
            }
        }
        // args0: source, args1: destination, args2: army size
        if ((clientsEvent[0] == null || clientsEvent[0].length == 0) && (clientsEvent[1] == null || clientsEvent[1].length == 0))
            return;
        for (int j = 0; j < 2; j++) {
            if (clientsEvent[j] == null || clientsEvent[j].length == 0)
                continue;
            for (int i = clientsEvent[j].length - 1; i > -1; i--) {
                int src = -1;
                int dst = -1;
                int armySize = -1;
                try {
                    src = Integer.valueOf(clientsEvent[j][i].getArgs()[0]);
                    dst = Integer.valueOf(clientsEvent[j][i].getArgs()[1]);
                    armySize = Integer.valueOf(clientsEvent[j][i].getArgs()[2]);
                } catch (Exception e) {
                    Log.w(TAG, "Bad event received.", e);
                }
                if (movesDest[src] < 0 && isMoveValid(src, dst, armySize, j)) {

                    movesDest[src] = dst;
                    movesSize[src] = armySize;
                    armyInV[j][src] -= armySize;

                    // type: exitArmy
                    // args in order: node id, size after army exit, player of move
                    uiMessages.add(new Message("0", new Object[]{src, armyCount[src] - armySize, j}));

                    if (movesDest[dst] == src && ownership[dst] != ownership[src]) {
                        int[] battleInfo = doBattle('e', dst, src, movesSize[dst], armySize);

                        conflictedMoves[src] = 1;
                        conflictedMoves[dst] = 1;

                        // type: edgeBattle
                        // args in order: winner node, looser node, winner initial army, size of alive army of winner after battle, casualties of looser
                        if (ownership[src] == battleInfo[0])
                            uiMessages.add(new Message("1", new Object[]{src, dst, movesSize[src], battleInfo[1], movesSize[dst]}));
                        //else if (ownership[dst] == battleInfo[0])
                        else
                            uiMessages.add(new Message("1", new Object[]{dst, src, movesSize[dst], battleInfo[1], movesSize[src]}));

                        if (ownership[src] == battleInfo[0]) {
                            movesSize[src] = battleInfo[1];
                        } else if (ownership[dst] == battleInfo[0]) {
                            movesSize[dst] = battleInfo[1];
                        } else {
                            movesSize[src] = 0;
                            movesSize[dst] = 0;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < vertexNum; i++) {
            if (ownership[i] > -1 && movesDest[i] > -1) {
                armyInV[ownership[i]][movesDest[i]] += movesSize[i];

                if (conflictedMoves[i] == 0) {
                    // type: nonConflictedMove
                    // args in order: source node, destination node, owner, army size
                    uiMessages.add(new Message("2", new Object[]{i, movesDest[i], ownership[i], movesSize[i]}));
                }

            }
        }

        //escapes
        for (int i = 0; i < vertexNum; i++) {
            if (armyInV[0][i] != 0 && armyInV[1][i] != 0) {

                int escaper = -1;
                if (armyInV[0][i] < armyInV[1][i])
                    escaper = 0;
                else if (armyInV[0][i] > armyInV[1][i])
                    escaper = 1;
                if (escaper == -1)
                    continue;

                for (int j = 0; j < adjacencyList[i].length; j++) {
                    if (ownership[adjacencyList[i][j]] == escaper && armyInV[escaper][i] >= escapeNum) {
                        armyInV[escaper][i] -= escapeNum;
                        armyInV[escaper][adjacencyList[i][j]] += escapeNum;

                        // type: escape
                        // args in order: source node, destination node, escape army size
                        uiMessages.add(new Message("3", new Object[]{i, adjacencyList[i][j], escapeNum}));

                    }
                }
            }
        }

        //battles
        for (int i = 0; i < vertexNum; i++) {
            if (armyInV[0][i] != 0 && armyInV[1][i] != 0) {
                int[] battleInfo = doBattle('v', i, -1, armyInV[0][i], armyInV[1][i]);

                // need talk to pooya
                // type: nodeBattle
                // args in order: node battle, final owner, final size of winner army, size of winner casualties, size of looser casualties
                if (battleInfo[0] != -1)
                    uiMessages.add(new Message("4", new Object[]{i, battleInfo[0], battleInfo[1], armyInV[battleInfo[0]][i] - battleInfo[1], armyInV[(battleInfo[0] - 1) * -1][i]}));

                if (battleInfo[0] > -1) {
                    if (ownership[i] != battleInfo[0]) {
                        armyInV[battleInfo[0]][i] = battleInfo[1] + increaseWithOwnership;

                        // type: increaseWithOwnership
                        // args in order: node, amount
                        uiMessages.add(new Message("5", new Object[]{i, increaseWithOwnership}));

                    } else {
                        armyInV[battleInfo[0]][i] = battleInfo[1];
                    }
                    ownership[i] = battleInfo[0];
                    armyInV[(ownership[i] - 1) * -1][i] = 0;
                } else {
                    armyInV[0][i] = 0;
                    armyInV[1][i] = 0;
                }
            } else if (armyInV[0][i] != 0) {
                if (ownership[i] != 0)
                    armyInV[0][i] += increaseWithOwnership;
                ownership[i] = 0;
            } else if (armyInV[1][i] != 0) {
                if (ownership[i] != 1)
                    armyInV[1][i] += increaseWithOwnership;
                ownership[i] = 1;
            }
            armyCount[i] = Math.max(armyInV[0][i], armyInV[1][i]);
            if (ownership[i] == -1) {
                if (armyInV[0][i] != 0) {
                    ownership[i] = 0;
                    armyCount[i] += increaseWithOwnership;
                    uiMessages.add(new Message("5", new Object[]{i, increaseWithOwnership}));
                } else if (armyInV[1][i] != 0) {
                    ownership[i] = 1;
                    armyCount[i] += increaseWithOwnership;
                    uiMessages.add(new Message("5", new Object[]{i, increaseWithOwnership}));
                }
            }
        }

        //increase army
        for (int i = 0; i < vertexNum; i++) {
            if (ownership[i] == -1)
                continue;
            for (int j = 0; j < adjacencyList[i].length; j++) {
                if (ownership[adjacencyList[i][j]] == ownership[i]) {
                    armyCount[i] += increaseWithEdge;
                    uiMessages.add(new Message("6", new Object[]{i, increaseWithEdge}));
                }
            }
        }

        uiMessage = new Message(Message.NAME_TURN, uiMessages.toArray());
    }

    private boolean isMoveValid(int src, int dst, int armySize, int clientNum) {
        if (src < 0 || src > vertexNum - 1)
            return false;
        if (dst < 0 || dst > vertexNum - 1)
            return false;
        if (clientNum != ownership[src])
            return false;
        if (!graph[src][dst])
            return false;
        if (armySize > armyCount[src])
            return false;
        return true;
    }

    // an array with size three, first member is winner, second one is number of alive soldiers of winner
    private int[] doBattle(char type, int endp0, int endp1, int armySize0, int armySize1) {
        if (armySize0 == armySize1) {
            return new int[]{-1, 0};
        } else {
            int[] output = new int[2];
            int more, less;
            if (armySize0 > armySize1) {
                output[0] = 0;
                more = armySize0;
                less = armySize1;
            } else {
                output[0] = 1;
                more = armySize1;
                less = armySize0;
            }
            if (qualAmount(more) == qualAmount(less)) {
                output[1] = more - (int) Math.ceil(less * highCasualties);
            } else if (qualAmount(more) - 1 == qualAmount(less)) {
                output[1] = more - (int) Math.ceil(less * mediumCasualties);
            } else if (qualAmount(more) - 2 == qualAmount(less)) {
                output[1] = more - (int) Math.ceil(less * lowCasualties);
            }
            return output;
        }
    }

    private int qualAmount(int amount) {
        if (amount < 10) {
            return 0;
        } else if (amount < 20) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public void generateOutputs() {
        this.context.flush();
        this.context.turnUP();
    }

    @Override
    public Message getUIMessage() {
        return uiMessage;
    }

    @Override
    public Message getStatusMessage() {
        return null;
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        String name0 = Message.NAME_TURN;
        Object[] args0 = {this.context.getTurn(), this.context.getDiffList(0)};
        messages[0] = new Message(name0, args0);

        String name1 = Message.NAME_TURN;
        Object[] args1 = {this.context.getTurn(), this.context.getDiffList(1)};
        messages[1] = new Message(name1, args1);

        return messages;
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished() {
        return (this.context.getMap().isFinished() || this.context.getTurn() >= PARAM_NUM_TURNS.getValue());
    }

    @Override
    public void terminate() {
    }
}