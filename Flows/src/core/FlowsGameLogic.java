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

    public FlowsGameLogic(String name) {
        this.mapName = name;
    }

    @Override
    public void init() {
        this.context = new Context(mapName);
        this.context.flush();
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
