import model.Event;
import models.GameConstants;
import models.Map;
import network.data.Message;
import server.config.BooleanParam;
import server.config.FileParam;
import server.config.IntegerParam;
import server.core.GameLogic;
import server.core.GameServer;
import util.Log;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by pezzati on 1/28/16.
 */
public class FlowsGameLogic implements GameLogic {
    private static final String TAG = "Flows";

    private Context context;
    private DebugUI debugUI;
    private Event[][] lastClientEvents;

    //Constants
    private int escapeNum;
    private int increaseWithOwnership;
    private int increaseWithEdge;
    private double highCasualties;
    private double mediumCasualties;
    private double lowCasualties;
    private int maxLow, maxNormal;

    //Temps
    private int vertexNum;
    private int[] armyCount;
    private int[] ownership;
    private boolean[][] graph;
    private int[][] adjacencyList;

    private int[] movesDest;
    private int[] movesDest2;
    private int[] movesSize;
    private int[] movesSize2;
    private int[][] armyInV;

    private Message uiMessage;

    public static final IntegerParam PARAM_CLIENT_TIMEOUT = new IntegerParam("ClientTimeout", 500);
    public static final IntegerParam PARAM_TURN_TIMEOUT = new IntegerParam("TurnTimeout", 1000);
    public static final FileParam PARAM_MAP = new FileParam("Map", null, ".*\\.map");
    public static final BooleanParam PARAM_SHOW_DEBUG_UI = new BooleanParam("ShowDebugUI", true);

    public static void main(String[] args) throws InterruptedException {
        GameServer gameServer = new GameServer(new FlowsGameLogic(), args);
        gameServer.waitForClients();
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
        this.context = new Context(PARAM_MAP.getValue());
        this.context.flush();

        Map map = this.context.getMap();
        vertexNum = map.getVertexNum();
        graph = map.getGraph();
        adjacencyList = map.getAdjacencyList();

        GameConstants constants = map.getGameConstants();
        escapeNum = constants.getEscape();
        increaseWithOwnership = constants.getNodeBonus();
        increaseWithEdge = constants.getEdgeBonus();
        highCasualties = 1;
        mediumCasualties = constants.getLossRate1();
        lowCasualties = constants.getLossRate2();
        maxLow = constants.getFirstlvl();
        maxNormal = constants.getSecondlvl();

        if (PARAM_SHOW_DEBUG_UI.getValue() == Boolean.TRUE) {
            debugUI = new DebugUI(this);
            debugUI.update(context.getMap().getAdjacencyList(), context.getDiffer().getPrevOwnership(), context.getDiffer().getPrevArmyCount(), lastClientEvents, getStatusMessage(), movesDest2, movesSize2);
        }
    }

    @Override
    public Message getUIInitialMessage() {
        Object[] args = {this.context.getMap().getVertexNum(), maxLow, maxNormal,
                this.context.getMap().getAdjacencyList(), this.context.getUIDiffList()};
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

    @Override
    public void simulateEvents(Event[] environmentEvent, Event[][] clientsEvent) {
        lastClientEvents = clientsEvent;

        armyCount = this.context.getMap().getArmyCount();
        ownership = this.context.getMap().getOwnership();

        movesDest = new int[vertexNum];
        movesDest2 = new int[vertexNum];
        movesSize = new int[vertexNum];
        movesSize2 = new int[vertexNum];
        armyInV = new int[2][vertexNum];

        int[] conflictedMoves = new int[vertexNum];

        ArrayList<Message> uiMessages = new ArrayList<>();

        for (int i = 0; i < vertexNum; i++) {
            movesDest[i] = -1;
            movesDest2[i] = -1;
            if (ownership[i] > -1) {
                armyInV[ownership[i]][i] = armyCount[i];
            }
        }
        // args0: source, args1: destination, args2: army size
        if ((clientsEvent[0] == null || clientsEvent[0].length == 0) && (clientsEvent[1] == null || clientsEvent[1].length == 0)) {
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
            return;
        }
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
                if (isMoveValid(src, dst, armySize, j) && movesDest[src] < 0) {

                    movesDest[src] = dst;
                    movesSize[src] = armySize;
                    movesDest2[src] = dst;
                    movesSize2[src] = armySize;
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
                            movesSize[dst] = 0;
                        } else if (ownership[dst] == battleInfo[0]) {
                            movesSize[dst] = battleInfo[1];
                            movesSize[src] = 0;
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
                    if (ownership[i] == ownership[movesDest[i]])
                        uiMessages.add(new Message("4", new Object[]{movesDest[i], ownership[i], movesSize[i], 0, 0}));
                }

            }
        }

        //escapes
        int[][] armyInVTemp = new int[2][vertexNum];
        //for (int i = 0; i < 2; i++)
        //    for (int j = 0; j < vertexNum; j++)
        //        armyInVTemp[i][j] = armyInV[i][j];
        for (int i = 0; i < vertexNum; i++) {
            if (armyInV[0][i] > 0 && armyInV[1][i] > 0) {
                int escaper = -1;
                if (armyInV[0][i] < armyInV[1][i])
                    escaper = 0;
                else if (armyInV[0][i] > armyInV[1][i])
                    escaper = 1;
                if (escaper == -1)
                    continue;

                ArrayList<Integer> adjacencyListTemp = new ArrayList<>(adjacencyList[i].length);
                for (int j = 0; j < adjacencyList[i].length; j++)
                    adjacencyListTemp.add(adjacencyList[i][j]);
                Collections.shuffle(adjacencyListTemp);
                for (int j = 0; j < adjacencyListTemp.size(); j++) {
                    if (ownership[adjacencyListTemp.get(j)] == escaper && armyInV[escaper][i] > 0) {
                        if (armyInV[escaper][i] >= escapeNum) {
                            armyInV[escaper][i] -= escapeNum;
                            armyInVTemp[escaper][adjacencyListTemp.get(j)] += escapeNum;
                        } else {
                            armyInVTemp[escaper][adjacencyListTemp.get(j)] += armyInV[escaper][i];
                            armyInV[escaper][i] = 0;
                        }

                        // type: escape
                        // args in order: source node, destination node, escape army size
                        uiMessages.add(new Message("3", new Object[]{i, adjacencyListTemp.get(j), escapeNum}));
                    }
                }
            }
        }
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < vertexNum; j++)
                armyInV[i][j] += armyInVTemp[i][j];

        //battles
        for (int i = 0; i < vertexNum; i++) {
            if (armyInV[0][i] > 0 && armyInV[1][i] > 0) {
                int[] battleInfo = doBattle('v', i, -1, armyInV[0][i], armyInV[1][i]);

                // need talk to pooya
                // type: nodeBattle
                // args in order: node battle, final owner, final size of winner army, size of winner casualties, size of looser casualties
                if (battleInfo[0] != -1)
                    uiMessages.add(new Message("4", new Object[]{i, battleInfo[0], battleInfo[1], armyInV[battleInfo[0]][i] - battleInfo[1], armyInV[(battleInfo[0] - 1) * -1][i]}));
                else
                    uiMessages.add(new Message("4", new Object[]{i, battleInfo[0], 0, 0, 0}));

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
            } else if (armyInV[0][i] > 0) {
                if (ownership[i] != 0) {
                    armyInV[0][i] += increaseWithOwnership;
                    uiMessages.add(new Message("4", new Object[]{i, 0, armyInV[0][i], 0, 0}));
                    uiMessages.add(new Message("5", new Object[]{i, increaseWithOwnership}));
                }
                ownership[i] = 0;
            } else if (armyInV[1][i] > 0) {
                if (ownership[i] != 1) {
                    armyInV[1][i] += increaseWithOwnership;
                    uiMessages.add(new Message("4", new Object[]{i, 1, armyInV[1][i], 0, 0}));
                    uiMessages.add(new Message("5", new Object[]{i, increaseWithOwnership}));
                }
                ownership[i] = 1;
            }
            armyCount[i] = Math.max(armyInV[0][i], armyInV[1][i]);
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
        if (armySize > armyCount[src] || armySize <= 0)
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
        if (amount <= maxLow) {
            return 0;
        } else if (amount <= maxNormal) {
            return 1;
        } else {
            return 2;
        }
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