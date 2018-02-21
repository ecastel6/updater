package app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessControllerTest
{
    ProcessController processController = new ProcessController();
    String command;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        switch (processController.osType) {
            case "Linux":
                command = "uname";
                break;
            case "Windows":
                command = "ver";
                break;
        }

    }

    @Test
    void runCommand() {
        assertTrue((processController.runCommand(command)).u.toString().contains(processController.osType));
    }

    @Test
    void serviceAction() {
        assertEquals(-1, processController.serviceAction("invalidService", "invalidAction").t);
        assertEquals(0, (processController.serviceAction("cron", "status").t));
        assertTrue(processController.serviceAction("cron", "status").u.toString().contains("Loaded"));
        assertEquals(0, processController.serviceAction("cron", "restart").t);
    }

    @Test
    void serviceAlive() {
        String testService = "";
        switch (processController.osType) {
            case "Linux":
                testService = "network";
                break;
            case "Windows":
                testService = "net";
                break;
        }
        System.out.println("Testing service " + testService);
        assertTrue(processController.serviceAlive(testService));
    }
}