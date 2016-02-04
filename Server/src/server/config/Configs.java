package server.config;

import java.io.File;
import java.util.ArrayList;

/**
 * Copyright (C) 2016 Hadi
 */
public class Configs {
    public static String DEFAULT_CONFIG_PATH = "game.conf";

    // Client Configs
    public static final IntegerParam PARAM_CLIENTS_PORT = new IntegerParam("ClientsPort", 7099);
    public static final IntegerParam PARAM_CLIENTS_CONNECTIONS_TIMEOUT = new IntegerParam("ClientsConnectionTimeout", Integer.MAX_VALUE);
    public static final ArrayList<ClientConfig> CLIENT_CONFIGS = new ArrayList<>();

    // UI Configs
    public static final BooleanParam PARAM_UI_ENABLE = new BooleanParam("UIEnable", true);
    public static final StringParam PARAM_UI_TOKEN = new StringParam("UIToken", "00000000000000000000000000000000");
    public static final IntegerParam PARAM_UI_PORT = new IntegerParam("UIPort", 7000);
    public static final IntegerParam PARAM_UI_CONNECTIONS_TIMEOUT = new IntegerParam("UIConnectionTimeout", Integer.MAX_VALUE);

    public static void handleCMDArgs(String[] args) {
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
}
