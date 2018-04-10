package app.controllers;

import java.io.IOException;
import java.util.logging.*;

public class LoggingController {
    private static final Logger LOGGER = Logger.getLogger(LoggingController.class.getName());

    public LoggingController() throws IOException {
        ConsoleHandler consoleHandler = new ConsoleHandler();

        FileHandler fileHandler = new FileHandler("./app%g.log", 10485760, 3, true);
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(Level.FINEST);
        consoleHandler.setLevel(Level.SEVERE);
        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);
    }

    public void registerTestLoggerMessages() {

        //LOGGER.setLevel(Level.FINEST);
        LOGGER.setUseParentHandlers(false);
        LOGGER.log(Level.INFO, "Mensaje INFO");
        LOGGER.log(Level.SEVERE, "Mensaje Severe");

    }
}
