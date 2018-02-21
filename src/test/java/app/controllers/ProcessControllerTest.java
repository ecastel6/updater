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

        switch (processController.os) {
            case LINUX:
                command = "echo 1";
                break;
            case WINDOWS:
                //System.getenv("windir") + "\\system32\\" + "ver.exe /SVC");
                command = "cmd.exe /C echo 1";
                break;
        }

    }

    @Test
    void runCommand() {
        assertEquals("1",processController.runCommand(command).u.toString().trim());
    }

    @Test
    void serviceAction() {
        System.out.println(processController.serviceAction("invalidService", "invalidAction").t);
        assertEquals(-1, processController.serviceAction("invalidService", "invalidAction").t);
        System.out.println(processController.serviceAction("cron", "status").t);
        assertEquals(0, (processController.serviceAction("cron", "status").t));
        System.out.println (processController.serviceAction("cron", "status").u.toString().contains("Loaded"));
        assertTrue(processController.serviceAction("cron", "status").u.toString().contains("Loaded"));
        System.out.println(processController.serviceAction("cron", "restart").t);
        assertEquals(0, processController.serviceAction("cron", "restart").t);
    }

    @Test
    void serviceAlive() {
        String testService = "";
        switch (processController.os) {
            case LINUX:
                testService = "network";
                break;
            case WINDOWS:
                testService = "net";
                break;
            case OTHER:
                break;
        }
        System.out.println("Testing service " + testService);
        assertTrue(processController.serviceAlive(testService));
    }
}