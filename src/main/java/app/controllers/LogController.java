package app.controllers;

import app.core.LogFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;

public class LogController {
    private static LogController ourInstance = new LogController();
    public Logger log = Logger.getLogger(this.getClass().getName());

    private LogController() {
        try {
            FileInputStream fis = new FileInputStream("logging.properties");
            LogManager.getLogManager().readConfiguration(fis);

            // Minimum level shown either file or console logger
            log.setLevel(Level.FINE);
            log.addHandler(new java.util.logging.ConsoleHandler());
            Handler fileHandler = new FileHandler();
            fileHandler.setFormatter(new LogFormatter());
            log.addHandler(fileHandler);
            log.setUseParentHandlers(false);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LogController getInstance() {
        return ourInstance;
    }


}
