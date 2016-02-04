package server.config;

/**
 * Copyright (C) 2016 Hadi
 */
public class ClientConfig {
    private static int clientCount = 0;

    public final IntegerParam id;
    public final StringParam name;
    public final StringParam token;

    public ClientConfig() {
        int num = clientCount++;
        id = new IntegerParam("Client" + num + "ID", num);
        name = new StringParam("Client" + num + "Name", "");
        token = new StringParam("Client" + num + "Token", "00000000000000000000000000000000");
    }
}
