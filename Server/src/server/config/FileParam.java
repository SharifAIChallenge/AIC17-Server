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
        JOptionPane.showMessageDialog(null, "Parameter '" + getParamName() + "' is not specified or invalid.\nPlease select a file to continue.", "Game Parameters", JOptionPane.INFORMATION_MESSAGE);
        JFileChooser fileChooser = new JFileChooser((String) null);
        int result = fileChooser.showOpenDialog(null);
        if (result != JOptionPane.YES_OPTION)
            return null;
        return fileChooser.getSelectedFile();
    }
}
