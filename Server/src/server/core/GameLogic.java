package server.core;

import model.Event;
import network.data.Message;
import server.config.Param;
import server.core.model.ClientInfo;

import java.util.function.Function;

/**
 * The abstract class representing the main game logic of the user's game.
 * <p>
 * This class will be the simulator engine of the game.
 * </p>
 */
public interface GameLogic {

    /**
     * Returns essential dynamic game parameters like map, number of players (if dynamic), etc.
     * Framework will assign appropriate values in the runtime and you
     * can get values by calling .getValue() method on each of these
     * parameters.
     *
     * @return list of game parameters
     */
    public Param[] getGameParameters();

    /**
     * Returns number of players.
     *
     * @return number of players
     */
    public int getClientsNum();

    /**
     * This method must send initial and necessary values to UI and clients.
     *
     * @return A hashmap that has <code>Token</code> as <strong>key</strong> and a <code>Message</code> as <strong>value</strong>.
     * <code>Token</code> is used for specifying if the <code>Message</code> is for UI or Client.
     */
    public void init();

    /**
     * @return
     */
    public Message getUIInitialMessage();

    /**
     * @return
     */
    public Message[] getClientInitialMessages();

    /**
     * Simulate events based on the current turn event and calculate the changes in game.
     *
     * @param terminalEvent    Events that user enters in terminal.
     * @param environmentEvent Events that is related to environment. Suppose we want to develop a strategic game.
     *                         Increasing/Decreasing a specific resource in map is an environment event.
     * @param clientsEvent     Events that is related to client e.g. moving the player.
     */
    public void simulateEvents(Event[] terminalEvent, Event[] environmentEvent, Event[][] clientsEvent);

    /**
     * This method generates the output based on the changes that were calculated in
     * {@link #simulateEvents}.
     *
     * @return A hashmap that has <code>Token</code> as <strong>key</strong> and a <code>Message</code> as <strong>value</strong>.
     * <code>Token</code> is used for specifying if the <code>Message</code> is for UI or Client.
     */
    public void generateOutputs();

    public Message getUIMessage();

    public Message getStatusMessage();

    public Message[] getClientMessages();

    /**
     * This method is used for making the environment events.
     *
     * @return An array that is environment events.
     */
    public Event[] makeEnvironmentEvents();

    public boolean isGameFinished();

    public void terminate();
}
