package server.config;

import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Copyright (C) 2016 Hadi
 */
public abstract class ConfigParam<T> {
    private static ArrayList<ConfigParam> allParameters = new ArrayList<>();

    public static ConfigParam[] getAllParameters() {
        return allParameters.toArray(new ConfigParam[allParameters.size()]);
    }

    private String paramName;
    private T defaultValue;
    private T value;
    private boolean cached = false;

    public ConfigParam(String paramName, T defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    public T getValue() {
        if (value != null || cached)
            return value;
        if ((value = getValueFromEnv()) != null)
            return value;
        if ((value = getValueFromJsonObject(Configs.getConfigFile())) != null)
            return value;
        if ((value = getDefaultValue()) != null)
            return value;
        while (value == null)
            value = getValueFromUser();
        cached = true;
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
