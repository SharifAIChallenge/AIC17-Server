package core;

import model.Event;
import network.data.Message;
import server.Context;
import server.core.GameLogic;
import server.core.model.ClientInfo;

import java.util.Objects;

/**
 * Created by pezzati on 1/28/16.
 */
public class FlowsGameLogic implements GameLogic {
    private Context context;
    private String mapName;
	
	//Constants
	final static int escapeNum = 1;
	final static double highCasualties = 1.0;
	final static double mediumCasualties = 2.0 / 3.0;
	final static double lowCasualties = 1.0 / 3.0;
	
	//Temps
	int vertexNum;
	int[] armyCount;
	int[] ownership;
	int[][] graph;
	int[][] adjacencyList;

	int[] movesDest;
	int[] movesSize;
	int[][] armyInV;

    public FlowsGameLogic(String name) {
        this.mapName = name;
    }

    @Override
    public void init() {
        this.context = new Context(mapName);
        this.context.flush();
		
		vertexNum = Map.getVertexNum();
		graph = map.getGraph();
		adjacencyList = map.getAdjacencyList();
    }

    @Override
    public Message getUIInitialMessage() {
        return null;
    }

    @Override
    public Message[] getClientInitialMessages() {
        Message[] msg = new Message[2];
        msg[0] = new Message();
        msg[0].setName(Message.NAME_INIT);
        Object[] args0 = {this.context.getMap().getGraph(), this.context.getDiffer().getDiff(0)};
        msg[0].setArgs(args0);

        msg[1] = new Message();
        msg[1].setName(Message.NAME_INIT);
        Object[] args1 = {this.context.getMap().getGraph(), this.context.getDiffer().getDiff(1)};
        msg[1].setArgs(args1);
        return msg;
    }

    @Override
    public ClientInfo[] getClientInfo() {
        return new ClientInfo[0];
    }

    @Override
    public void simulateEvents(Event[] terminalEvent, Event[] environmentEvent, Event[][] clientsEvent) {
		
		armyCount = map.getArmyCount();
		ownership = map.getOwnership();
		
		movesDest = new int[vertexNum];
		movesSize = new int[vertexNum];
		armyInV = new int[2][vertexNum];
		
		for (int i = 0; i < vertexNum; i++) {
			movesDest[i] = -1;
			if (ownership[i] > -1) {
				armyInV[ownership[i]][i] = armyCount[i];
			}
		}
		
		// args0: source, args1: destination, args2: army size		
		for (int j = 0; j < 2; j++) {
			for (int i = clientsEvent[j].length - 1; i > -1; i--) {
				int src = -1;
				int dst = -1;
				int armySize = -1;
				try{
					src = (int)clientsEvent[j][i].args[0];
					dst = (int)clientsEvent[j][i].args[1];
					armySize = (int)clientsEvent[j][i].args[2];
				} catch (Exception e) {
				
				}
				if (movesDest[src] < 0 && isMoveValid(src, dst, armySize, j)) {
					movesDest[src] = dst;
					movesSize[src] = armySize;
					armyInV[j][src] -= armySize;
					if (movesDest[dst] == src) {
						addBattle('e', dst, src, movesSize[dst], armySize);
						doBattle('e', dst, src, movesSize[dst], armySize);
					}
				}
			}
		}

		for (int i = 0; i < vertexNum; i++) {
			armyInV[ownership[i]][movesDest[i]] += movesSize[i];
		}
		
		//escapes
		for (int i = 0; i < vertexNum; i++) {
			if (armyInV[0][i] != 0 && armyInV[1][i] != 0) {
				if (armyInV[0][i] < armyInV[1][i]) {
					for (int j = 0; j < adjacencyList[i].length; j++) {
						if (ownership[adjacencyList[i][j]] == 0 && armyInV[0][i] >= escapeNum) {
							armyInV[0][i] -= escapeNum;
							armyInV[0][adjacencyList[i][j]] += escapeNum;
						}
					}
				} else if (armyInV[0][i] > armyInV[1][i]) {
					for (int j = 0; j < adjacencyList[i].length; j++) {
						if (ownership[adjacencyList[i][j]] == 1 && armyInV[1][i] >= escapeNum) {
							armyInV[1][i] -= escapeNum;
							armyInV[1][adjacencyList[i][j]] += escapeNum;
						}
					}
				}
			}
		}

		//battles
		for (int i = 0; i < vertexNum; i++) {
			if (armyInV[0][i] != 0 && armyInV[1][i] != 0) {
				int[] battleInfo = doBattle('v', i, -1, armyInV[0][i], armyInV[1][i]);
				ownership[i] = battleInfo[0];
				armyCount[i] = battleInfo[1];
			}
		}
    }
	
	bool isMoveValid(int src, int dst, int armySize, int clientNum) {
		if (src < 0 || src > vertexNum - 1)
			return false;
		if (dst < 0 || dst > vertexNum - 1)
			return false;
		if (clientNum != ownership[src])
			return false;
		if (graph[src][dst] != 1)
			return false;
		if (armySize > armyCount[src])
			return false;
		return true;
	}
	
	// an array with size three, first member is winner, second one is number of alive soldiers of winner
	int[] doBattle(char type, int endp0, int endp1, int armySize0, int armySize1) {
		if (armySize0 == armySize1) {
			return new int[]{-1, 0};
		} else {
			int[] output = new int[2];
			int more, less;
			if (armySize0 > armySize1) {
				output[0] = 0;
				more = armySize0;
				less = armySize1 - output[2];
			} else {
				output[0] = 1;
				more = armySize1;
				less = armySize0 - output[2];
			}
			if (qualAmount(more) == qualAmount(less)) {
				output[1] = more - Math.ceil(less * highCasulties);
			} else if (qualAmount(more) - 1 == qualAmount(less)) {
				output[1] = more - Math.ceil(less * mediumCasualties);
			} else if (qualAmount(more) - 2 == qualAmount(less)) {
				output[1] = more - Math.ceil(less * lowCasualties);
			}
			return output;
		}
	}

	char qualAmount(int amount) {
		if (amount < 10) {
			return 'l';
		} else if (amount < 20) {
			return 'm';
		} else {
			return 'h';
		}
	}

    @Override
    public void generateOutputs() {

    }

    @Override
    public Message getUIMessage() {
        return null;
    }

    @Override
    public Message getStatusMessage() {
        return null;
    }

    @Override
    public Message[] getClientMessages() {
        Message[] messages = new Message[2];
        messages[0] = new Message();
//        messages[0].setName(); TODO
        Object[] args0 = {this.context.getDiffer().getDiff(0)};
        messages[0].setArgs(args0);

        messages[1] = new Message();
//        messages[1].setName(); TODO
        Object[] args1 = {this.context.getDiffer().getDiff(1)};
        messages[1].setArgs(args1);

        return messages;
    }

    @Override
    public Event[] makeEnvironmentEvents() {
        return new Event[0];
    }

    @Override
    public boolean isGameFinished() {
        return this.context.getDiffer().isFinished();
    }

    @Override
    public void terminate() {

    }
}
