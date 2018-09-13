package app.controllers;

import app.core.FileLogFormatter;

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

            // Define Console handler
            // Minimum level shown either file or console logger
            log.setLevel(Level.FINE);
            Handler consoleHandler = new java.util.logging.ConsoleHandler();
            consoleHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return new String(record.getMessage() + "\n");
                }
            });
            log.addHandler(consoleHandler);
            // File handler
            Handler fileHandler = new FileHandler();
            fileHandler.setFormatter(new FileLogFormatter());
            log.addHandler(fileHandler);
            log.setUseParentHandlers(false);
            fis.close();
        } catch (IOException e) {
            log.severe("logging.properties file not found. Running with default config.  Console output only ");
            //e.printStackTrace();
        }
    }

    public static LogController getInstance() {
        return ourInstance;
    }


}
