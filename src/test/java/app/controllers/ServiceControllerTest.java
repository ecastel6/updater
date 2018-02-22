package app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceControllerTest {
    ServiceController serviceController = new ServiceController();
    String[] command;
    String testSVC, testSVCString;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        switch (serviceController.os) {
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
        assertEquals("1", serviceController.runCommand(command).u.toString().trim());
    }

    @Test
    void serviceAlive() {
        assertTrue(serviceController.serviceAlive(testSVC));
    }

    @Test
    void stopService() {
        String previousStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        serviceController.serviceAction(testSVC, "stop");
        //testing alternative check
        assertFalse(serviceController.serviceAlive(testSVC));
        assertEquals("0", serviceController.serviceAction(testSVC, "status").u);
        if (previousStatus == "1") {
            serviceController.serviceAction(testSVC, "start");
        }
        String finalStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }

    @Test
    void startService() {
        String previousStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        if (previousStatus == "1") serviceController.serviceAction(testSVC, "stop");
        System.out.printf("Service %s status is %s\n", testSVC, serviceController.serviceAlive(testSVC));
        System.out.printf("Starting Service %s \n", testSVC);
        serviceController.serviceAction(testSVC, "start");
        System.out.printf("Service %s status is %s\n", testSVC, serviceController.serviceAlive(testSVC));
        //testing alternative check
        assertTrue(serviceController.serviceAlive(testSVC));
        if (previousStatus == "0") {
            serviceController.serviceAction(testSVC, "stop");
        }
        String finalStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }

    @Test
    void restartService() {
        String previousStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        System.out.printf("Restarting Service %s\n", testSVC);
        serviceController.serviceAction(testSVC, "restart");
        System.out.printf("Restarted Service %s\n", testSVC);
        //testing alternative check
        assertTrue(serviceController.serviceAlive(testSVC));
        assertEquals("1", serviceController.serviceAction(testSVC, "status").u);
        if (previousStatus == "0") {
            serviceController.serviceAction(testSVC, "stop");
        }
        String finalStatus = serviceController.serviceAction(testSVC, "status").u.toString();
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }
}