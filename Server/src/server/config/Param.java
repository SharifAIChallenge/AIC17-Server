package server.config;

import com.google.gson.JsonObject;
import network.Json;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Copyright (C) 2016 Hadi
 */
public abstract class Param<T> {
    private static JsonObject configFile = null;
    private static ArrayList<Param> allParameters = new ArrayList<>();

    public static void setConfigFile(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String content = new String(bytes, Charset.forName("UTF-8"));
            configFile = Json.GSON.fromJson(content, JsonObject.class);
        } catch (Exception ignore) {
            configFile = null;
        }
    }

    public static Param[] getAllParameters() {
        return allParameters.toArray(new Param[allParameters.size()]);
    }

    private String paramName;
    private T defaultValue;

    public Param(String paramName, T defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    public T getValue() {
        T value;
        if ((value = getValueFromEnv()) != null)
            return value;
        if ((value = getValueFromJsonObject(configFile)) != null)
            return value;
        if ((value = getDefaultValue()) != null)
            return value;
        while (value == null)
            value = getValueFromUser();
        return value;
    }

    public T getValueFromEnv() {
        try {
            String value = System.getenv("AIC" + paramName);
            return getValueFromString(value);
        } catch (Exception ignore) {
            return null;
        }
    }

    public T getValueFromJsonObject(JsonObject object) {
        try {
            String value = object.getAsJsonPrimitive(paramName).getAsString();
            return getValueFromString(value);
        } catch (Exception ignore) {
            return null;
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValueFromUser() {
        // todo
        return null;
    }

    public abstract T getValueFromString(String value);
}
