package server.config;

import com.google.gson.JsonObject;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Copyright (C) 2016 Hadi
 */
public abstract class Param<T> {
    private static final ArrayList<Param> allParameters = new ArrayList<>();

    public static Param[] getAllParameters() {
        return allParameters.toArray(new Param[allParameters.size()]);
    }

    private final String paramName;
    private final T defaultValue;
    private T value;
    private boolean cached = false;

    public Param(String paramName, T defaultValue) {
        allParameters.add(this);
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    public String getParamName() {
        return paramName;
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
        if (Configs.PARAM_AIC_DEPLOY.getValue() == Boolean.TRUE)
            throw new RuntimeException("Config '" + paramName + "' not found.");
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
        while (value == null) {
            try {
                String result = JOptionPane.showInputDialog(null, "Parameter '" + paramName + "' is not specified. Please enter a value to continue.", "Game Parameters", JOptionPane.INFORMATION_MESSAGE);
                value = getValueFromString(result);
            } catch (Exception ignore) {
            }
        }
        return value;
    }

    public abstract T getValueFromString(String value);

    @Override
    public String toString() {
        return String.format("%s[%s]", paramName, getClass().getSimpleName());
    }
}
