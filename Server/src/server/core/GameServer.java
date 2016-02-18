package server.core;

import model.Event;
import network.data.Message;
import server.config.ClientConfig;
import server.config.Configs;
import server.network.ClientNetwork;
import server.network.UINetwork;
import util.Log;

import java.util.Arrays;

/**
 * Core controller of the framework, controls the {@link server.core.GameLogic GameLogic}, main loop of the game and
 * does the output controlling operations.
 * <p>
 * This class runs the main running thread of the framework. Class interacts with the clients, UI, and the
 * GameLogic itself.
 * Threads in this class, will gather the clients' events
 * (See also {@link server.network.ClientNetwork ClientNetwork}), send them to the main Game
 * (See also {@link server.core.GameLogic GameLogic})
 * The output will be manipulated and sent to the appropriate controller within a inner module of the class
 * (OutputController).
 * The sequence of the creation and running the operations of this class will be through the call of the following
 * methods.
 * {@link GameServer#start() start()} and then at the
 * moment the external terminal user wants to shut down the games loop (except than waiting for the
 * {@link server.core.GameLogic GameLogic} to flag the end of the game), the
 * {@link GameServer#shutdown() shutdown()} method would be called.
 * Note that shutting down the {@link GameServer GameServer} will not immediately stop the threads,
 * actually it will set a shut down request flag in the class, which will closes the thread in the aspect of
 * accepting more inputs, and the terminate the threads as soon as the operation queue got empty.
 * </p>
 */
public class GameServer {
    private ClientNetwork mClientNetwork;
    private UINetwork mUINetwork;
    private GameLogic mGameLogic;
    private OutputController mOutputController;
    private final int mClientsNum;
    private ClientConfig[] mClientConfigs;

    private Loop mLoop;

    /**
     * Constructor of the {@link GameServer GameServer}, connects the handler to the Clients through
     * {@link server.network.ClientNetwork ClientNetwork} and to the UI through
     * {@link server.network.UINetwork UINetwork}.
     * <p>
     * The constructor accepts the instances of {@link GameServer GameServer} and
     * {@link server.network.ClientNetwork ClientNetwork} classes. Then sets some configurations of the loops
     * within the "turn_timeout.conf" file ({@see https://github.com/JavaChallenge/JGFramework/wiki wiki}).
     * </p>
     */
    public GameServer(GameLogic gameLogic, String[] cmdArgs) {
        Configs.handleCMDArgs(cmdArgs);
        mGameLogic = gameLogic;
        mGameLogic.init();
        mClientsNum = mGameLogic.getClientsNum();
        setClientConfigs();
        mClientNetwork = new ClientNetwork();
        mUINetwork = new UINetwork();
        mOutputController = new OutputController(mUINetwork);
        initGame();
    }

    private void setClientConfigs() {
        mClientConfigs = new ClientConfig[mClientsNum];
        for (int i = 0; i < mClientsNum; i++) {
            mClientConfigs[i] = new ClientConfig();
            Configs.CLIENT_CONFIGS.add(mClientConfigs[i]);
        }
    }

    private void initGame() {
        for (int i = 0; i < mClientsNum; ++i) {
            int id = mClientNetwork.defineClient(mClientConfigs[i].getToken());
            if (id != i) {
                throw new RuntimeException("Client ID and client order does not match");
            }
            mClientConfigs[i].setID(id);
        }

        if (Configs.PARAM_UI_ENABLE.getValue()) {
            mUINetwork.listen(Configs.PARAM_UI_PORT.getValue());
            mClientNetwork.listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                mUINetwork.waitForClient(Configs.PARAM_UI_CONNECTIONS_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for ui clients interrupted");
            }

            try {
                mClientNetwork.waitForAllClients(Configs.PARAM_CLIENTS_CONNECTIONS_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            Message initialMessage = mGameLogic.getUIInitialMessage();
            mUINetwork.sendBlocking(initialMessage);

            Message[] initialMessages = mGameLogic.getClientInitialMessages();
            for (int i = 0; i < initialMessages.length; ++i) {
                mClientNetwork.queue(i, initialMessages[i]);
            }
            mClientNetwork.sendAllBlocking();
        } else {
            mClientNetwork.listen(Configs.PARAM_CLIENTS_PORT.getValue());

            try {
                mClientNetwork.waitForAllClients(Configs.PARAM_CLIENTS_CONNECTIONS_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for clients interrupted");
            }

            Message[] initialMessages = mGameLogic.getClientInitialMessages();
            for (int i = 0; i < initialMessages.length; ++i) {
                mClientNetwork.queue(i, initialMessages[i]);
            }
            mClientNetwork.sendAllBlocking();
        }
    }

    public void waitForClients() throws InterruptedException {
        mClientNetwork.waitForAllClients();
    }

    /**
     * Starts the main game ({@link server.core.GameLogic GameLogic}) loop and the
     * {@link server.core.OutputController OutputController} operations in two new {@link java.lang.Thread Thread}.
     */
    public void start() {
        mLoop = new Loop();
        new Thread(mLoop).start();
        new Thread(mOutputController).start();
    }

    /**
     * Registers a shutdown request into the main loop and {@link server.core.OutputController OutputController} class
     * <p>
     * Note that the shutdown requests, will be responded as soon as the current queue of operations got freed.
     * </p>
     */
    public void shutdown() {
        if (mLoop != null)
            mLoop.shutdown();
        if (mOutputController != null)
            mOutputController.shutdown();
    }

    /**
     * In order to give the loop a thread to be ran beside of the main loop.
     * <p>
     * This inner class has a {@link java.util.concurrent.Callable Callable} part, which is wrote down as a
     * runnable code template. This template is composed by the multiple steps in every turn of the game.
     * </p>
     */
    private class Loop implements Runnable {

        private boolean shutdownRequest = false;

        private Event[] environmentEvents;
        private Event[][] clientEvents;

        /**
         * The run method of the {@link java.lang.Runnable Runnable} interface which will create a
         * {@link java.util.concurrent.Callable Callable} instance and call it in a while until the finish flag if the
         * game had been raised or the shutdown request sent to the class (through
         * {@link GameServer.Loop#shutdown() shutdown()} method)
         */
        @Override
        public void run() {
            clientEvents = new Event[mClientsNum][];
            for (int i = 0; i < clientEvents.length; i++) {
                clientEvents[i] = new Event[0];
            }

            Runnable simulate = () -> {
                try {
                    mGameLogic.simulateEvents(environmentEvents, clientEvents);
                } catch (Exception e) {
                    err("Simulation", e);
                }
                try {
                    mGameLogic.generateOutputs();
                } catch (Exception e) {
                    err("Generating outputs", e);
                }

                mOutputController.putMessage(mGameLogic.getUIMessage());
                mOutputController.putMessage(mGameLogic.getStatusMessage());

                if (mGameLogic.isGameFinished()) {
                    try {
                        mGameLogic.terminate();
                        Message shutdown = new Message(Message.NAME_SHUTDOWN, new Object[]{});
                        for (int i = 0; i < mClientsNum; i++) {
                            mClientNetwork.queue(i, shutdown);
                        }
                        mClientNetwork.sendAllBlocking();
//                        mClientNetwork.shutdownAll();
//                        mClientNetwork.terminate();
                        Message uiShutdown = new Message(Message.NAME_SHUTDOWN, new Object[]{});
                        mOutputController.putMessage(uiShutdown);
                        mOutputController.waitToSend();
                        mLoop.shutdown();
                        mOutputController.shutdown();
//                        mUINetwork.terminate();
                    } catch (Exception e) {
                        err("Finishing game", e);
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                    return;
                }

                Message[] output = mGameLogic.getClientMessages();
                for (int i = 0; i < output.length; ++i) {
                    mClientNetwork.queue(i, output[i]);
                }

//                if (mGameLogic.isGameFinished()) {
//                    mClientNetwork.sendAllBlocking();
//                    mGameLogic.terminate();
//                    mClientNetwork.shutdownAll();
//                    mLoop.shutdown();
//                    mOutputController.shutdown();
//                    return;
//                }

                mClientNetwork.startReceivingAll();
                mClientNetwork.sendAllBlocking();
                long elapsedTime = System.currentTimeMillis();
                environmentEvents = mGameLogic.makeEnvironmentEvents();
                elapsedTime = System.currentTimeMillis() - elapsedTime;
                long timeout = mGameLogic.getClientResponseTimeout();
                if (timeout - elapsedTime > 0) {
                    try {
                        Thread.sleep(timeout - elapsedTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Waiting for clients interrupted");
                    }
                }
                mClientNetwork.stopReceivingAll();

                clientEvents = new Event[mClientsNum][];
                for (int i = 0; i < mClientsNum; ++i) {
                    Event[] events = mClientNetwork.getReceivedEvents(i);
                    clientEvents[i] = events;
                }
            };

            while (!shutdownRequest) {
                long start = System.currentTimeMillis();
                try {
                    simulate.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long end = System.currentTimeMillis();
                long remaining = mGameLogic.getTurnTimeout() - (end - start);
                if (remaining <= 0) {
                    Log.i("GameServer", "Simulation timeout passed!");
                } else {
                    try {
                        Thread.sleep(remaining);
                    } catch (InterruptedException e) {
                        Log.i("GameServer", "Loop interrupted!");
                        break;
                    }
                }
            }

            synchronized (this) {
                notifyAll();
            }
        }

        /**
         * Will set the shutdown request flag in order to finish the main {@link GameServer.Loop Loop} at
         * the first possible turn
         */
        public void shutdown() {
            this.shutdownRequest = true;
        }
    }

    public void waitForFinish() throws InterruptedException {
        final Loop loop = mLoop;
        if (loop != null)
            synchronized (loop) {
                loop.wait();
            }
    }

    private void err(String title, Throwable exception) {
        System.err.println(title + " failed with message " + exception.getMessage() + ", stack: " + Arrays.toString(exception.getStackTrace()));
    }

}
