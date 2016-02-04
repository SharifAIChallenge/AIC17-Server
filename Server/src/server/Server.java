package server;

import network.data.Message;
import server.config.ClientConfig;
import server.config.Configs;
import server.config.Param;
import server.core.GameHandler;
import server.core.GameLogic;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Class is the main Server of the game with networks and game logic within.
 * <p>
 * {@link Server Server} class is the main runner of the framework activities.
 * Creation of the server class will assign the proper objects with the preferred configurations based on
 * files within the config files.
 * The class creates a terminal with connection through a terminal network.
 * Also sets the {@link server.core.GameLogic GameLogic} subclass instance to the
 * {@link server.core.GameHandler GameHandler} of the framework.
 * Any mistake in the config files causes rising a runtime exception.
 * </p>
 */
public class Server {
    public static final String DEFAULT_CONFIG_PATH = "game.conf";

    private Supplier<GameLogic> mGameLogicConstructor;
    private GameHandler mGameHandler;

    /**
     * Constructor of main server of the framework, which creates and connects server components to the object.
     * <p>
     * This class accepts a supplier in order to set the
     * user created subclass of {@link server.core.GameLogic GameLogic} class to the
     * {@link server.core.GameHandler GameHandler} of the server.
     * The configuration of {@link server.network.TerminalNetwork TerminalNetwork} and
     * {@link server.network.UINetwork UINetwork} are inside a file in resources folder.
     * ({@see https://github.com/JavaChallenge/JGFramework/wiki wiki})
     * Occurring any error during parsing config json files, causes a runtime exception to be thrown.
     * </p>
     *
     * @param gameLogicConstructor constructor of the implemented game logic
     * @param cmdArgs              command line arguments
     */
    public Server(Supplier<GameLogic> gameLogicConstructor, String[] cmdArgs) {
        handleCMDArgs(cmdArgs);
        mGameLogicConstructor = gameLogicConstructor;
        setClientConfigs();
        mGameHandler = new GameHandler();
        mGameHandler.init();
    }

    private void handleCMDArgs(String[] args) {
        if (args.length != 1)
            return;
        String[] split = args[0].split("=");
        if (split.length != 2 || !split[0].equals("--config"))
            return;
        File configFile = new File(split[1]);
        if (!configFile.exists())
            configFile = new File(DEFAULT_CONFIG_PATH);
        Param.setConfigFile(configFile);
    }

    private void setClientConfigs() {
        int clientsNum = mGameLogicConstructor.get().getClientsNum();
        for (int i = 0; i < clientsNum; i++) {
            Configs.CLIENT_CONFIGS.add(new ClientConfig());
        }
    }

    public GameHandler getGameHandler() {
        return mGameHandler;
    }

    public void newGame() throws IOException {
        GameLogic gameLogic = mGameLogicConstructor.get();
        gameLogic.init();
        mGameHandler.setGameLogic(gameLogic);
        mClientsInfo = gameLogic.getClientInfo();
        mGameHandler.setClientsInfo(mClientsInfo);
        for (int i = 0; i < mClientsInfo.length; ++i) {
            int id = mGameHandler.getClientNetwork().defineClient(mClientsInfo[i].getToken());
            if (id != i) {
                throw new RuntimeException("Client ID and client order does not match");
            }
            mClientsInfo[i].setID(id);
        }

        if (Configs.PARAM_UI_ENABLE.getValue()) {
            mGameHandler.getUINetwork().listen(Configs.PARAM_UI_PORT.getValue());
            mGameHandler.getClientNetwork().listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                mGameHandler.getUINetwork().waitForClient(uiTimeout);
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for ui clients interrupted");
            }

            try {
                mGameHandler.getClientNetwork().waitForAllClients(clientTimeout);
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            Message initialMessage = gameLogic.getUIInitialMessage();
            mGameHandler.getUINetwork().sendBlocking(initialMessage);

            Message[] initialMessages = gameLogic.getClientInitialMessages();
            for (int i = 0; i < initialMessages.length; ++i) {
                mGameHandler.getClientNetwork().queue(i, initialMessages[i]);
            }
            mGameHandler.getClientNetwork().sendAllBlocking();
        } else {
            mGameHandler.getClientNetwork().listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                mGameHandler.getClientNetwork().waitForAllClients(clientTimeout);
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            Message[] initialMessages = gameLogic.getClientInitialMessages();
            for (int i = 0; i < initialMessages.length; ++i) {
                mGameHandler.getClientNetwork().queue(i, initialMessages[i]);
            }
            mGameHandler.getClientNetwork().sendAllBlocking();
        }
    }

    /**
     * Terminates operations of the server.
     */
    public void shutdown() {
        mGameHandler.shutdown();
    }
}
