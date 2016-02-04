package server.config;

import javax.swing.*;
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
        File file = null;
        while (file == null) {
            String path = null;
            JFileChooser fileChooser = new JFileChooser(path);
            fileChooser.showOpenDialog(null);
            String result = JOptionPane.showInputDialog("Parameter '" + getParamName() + "' is not specified. Please enter a value to continue.");
            file = new File(result);
            if (!file.exists())
                file = null;
        }
        return file;
    }
}
