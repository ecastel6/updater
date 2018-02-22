package app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessControllerTest {
    ProcessController processController = new ProcessController();
    String[] command;
    String testSVC, testSVCString;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        switch (processController.os) {
            case LINUX:
                command = new String[]{"echo 1"};
                testSVC = "cron";
                testSVCString = "Loaded";
                break;
            case WINDOWS:
                command = new String[]{"cmd.exe", "/C", "echo 1"};
                testSVC = "DiagTrack";
                testSVCString = "RUNNING";
                break;
        }

    }

    @Test
    void runCommand() {
        assertEquals("1", processController.runCommand(command).u.toString().trim());
    }

    @Test
    void serviceAlive() {
        assertTrue(processController.serviceAlive(testSVC));
    }

    @Test
    void stopService() {
        String previousStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        processController.serviceAction(testSVC, "stop");
        //testing alternative check
        assertFalse(processController.serviceAlive(testSVC));
        assertEquals("0", processController.serviceAction(testSVC, "status").u);
        if (previousStatus == "1") {
            processController.serviceAction(testSVC, "start");
        }
        String finalStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }

    @Test
    void startService() {
        String previousStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        if (previousStatus == "1") processController.serviceAction(testSVC, "stop");
        System.out.printf("Service %s status is %s\n", testSVC, processController.serviceAlive(testSVC));
        System.out.printf("Starting Service %s \n", testSVC);
        processController.serviceAction(testSVC, "start");
        System.out.printf("Service %s status is %s\n", testSVC, processController.serviceAlive(testSVC));
        //testing alternative check
        assertTrue(processController.serviceAlive(testSVC));
        if (previousStatus == "0") {
            processController.serviceAction(testSVC, "stop");
        }
        String finalStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }

    @Test
    void restartService() {
        String previousStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        System.out.printf("Restarting Service %s\n", testSVC);
        processController.serviceAction(testSVC, "restart");
        System.out.printf("Restarted Service %s\n", testSVC);
        //testing alternative check
        assertTrue(processController.serviceAlive(testSVC));
        assertEquals("1", processController.serviceAction(testSVC, "status").u);
        if (previousStatus == "0") {
            processController.serviceAction(testSVC, "stop");
        }
        String finalStatus = processController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }
}