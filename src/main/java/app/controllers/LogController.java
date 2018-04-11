package app.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
            log.addHandler(new java.util.logging.FileHandler());
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
