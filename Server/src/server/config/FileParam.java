package server.config;

import java.io.File;

/**
 * Copyright (C) 2016 Hadi
 */
public class FileParam extends Param<File> {
    public FileParam(String paramName, File defaultValue) {
        super(paramName, defaultValue);
    }

    @Override
    public File getValueFromString(String value) {
        File file = new File(value);
        return file.exists() ? file : null;
    }

    @Override
    public File getValueFromUser() {
        // todo
        return super.getValueFromUser();
    }
}
