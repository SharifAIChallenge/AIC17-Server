package core;

import model.Event;
import network.data.Message;
import server.Context;
import server.core.GameLogic;
import server.core.model.ClientInfo;


/**
 * Created by pezzati on 1/28/16.
 */
public class FlowsGameLogic implements GameLogic {
    private Context context;
    private String mapName;
	
	//Constants
	final static int escapeNum = 1;
    final static int increaseWithOwnership = 1;
    final static int increaseWithEdge = 1;
	final static double highCasualties = 1.0;
	final static double mediumCasualties = 2.0 / 3.0;
	final static double lowCasualties = 1.0 / 3.0;
	
	//Temps
	int vertexNum;
	int[] armyCount;
	int[] ownership;
	boolean[][] graph;
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
		
		vertexNum = this.context.getMap().getVertexNum();
		graph = this.context.getMap().getGraph();
		adjacencyList = this.context.getMap().getAdjacencyList();
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
		
		armyCount = this.context.getMap().getArmyCount();
		ownership = this.context.getMap().getOwnership();
		
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
					src = Integer.valueOf(clientsEvent[j][i].getArgs()[0]);
					dst = Integer.valueOf(clientsEvent[j][i].getArgs()[1]);
					armySize = Integer.valueOf(clientsEvent[j][i].getArgs()[2]);
				} catch (Exception e) {
                    System.out.println("Event is BBAADDDD :D");
                }
				if (movesDest[src] < 0 && isMoveValid(src, dst, armySize, j)) {
					movesDest[src] = dst;
					movesSize[src] = armySize;
					armyInV[j][src] -= armySize;
					if (movesDest[dst] == src  && ownership[dst] != ownership[src]) {
                        int[] battleInfo = doBattle('e', dst, src, movesSize[dst], armySize);
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

		for (int i = 0; i < vertexNum; i++)
			armyInV[ownership[i]][movesDest[i]] += movesSize[i];
		
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
				if (battleInfo[0] > -1) {
                    if (ownership[i] != battleInfo[0])
                        armyInV[battleInfo[0]][i] = battleInfo[1] + increaseWithOwnership;
                    else
                        armyInV[battleInfo[0]][i] = battleInfo[1];
                    ownership[i] = battleInfo[0];
                    armyInV[(ownership[i] - 1) * -1][i] = 0;
                } else {
                    armyInV[0][i] = 0;
                    armyInV[1][i] = 0;
                }
			}
            armyCount[i] = Math.max(armyInV[0][i], armyInV[1][i]);
		}

        //increase army
        for (int i = 0; i < vertexNum; i++) {
            if (ownership[i] == -1)
                continue;
            for (int j = 0; j < adjacencyList[i].length; j++) {
                if (ownership[adjacencyList[i][j]] == ownership[i])
                    armyCount[i] += increaseWithEdge;
            }
        }
    }
	
	boolean isMoveValid(int src, int dst, int armySize, int clientNum) {
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
				output[1] = more - (int)Math.ceil(less * highCasualties);
			} else if (qualAmount(more) - 1 == qualAmount(less)) {
				output[1] = more - (int)Math.ceil(less * mediumCasualties);
			} else if (qualAmount(more) - 2 == qualAmount(less)) {
				output[1] = more - (int)Math.ceil(less * lowCasualties);
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
