package app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceControllerTest {
    ServiceController serviceController = ServiceController.getInstance();
    private String[] command;
    private String testSVC, testSVCString;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        switch (serviceController.os) {
            case LINUX:
                command = new String[]{"echo", "1"};//new String[]{"bin","echo","1"};
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
        assertEquals("[1]", serviceController.runCommand(command).u.toString().trim());
    }

    @Test
    void serviceAlive() {
        assertTrue(serviceController.serviceAlive(testSVC));
    }

    @Test
    void testStopService() {
        boolean previousStatus = serviceController.serviceAlive(testSVC);
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        //testing alternative check
        assertEquals(0, serviceController.serviceAction(testSVC, "stop").t);
        assertFalse(serviceController.serviceAlive(testSVC));
        if (previousStatus) {
            serviceController.serviceAction(testSVC, "start");
        }
        boolean finalStatus = serviceController.serviceAlive(testSVC);
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }

    @Test
    void startService() {
        boolean previousStatus = serviceController.serviceAlive(testSVC);
        System.out.printf("Service %s previous status is %s\n", testSVC, previousStatus);
        if (previousStatus) serviceController.serviceAction(testSVC, "stop");
        System.out.printf("Service %s status is %s\n", testSVC, serviceController.serviceAlive(testSVC));
        System.out.printf("Starting Service %s \n", testSVC);
        serviceController.serviceAction(testSVC, "start");
        System.out.printf("Service %s status is %s\n", testSVC, serviceController.serviceAlive(testSVC));
        //testing alternative check
        assertTrue(serviceController.serviceAlive(testSVC));
        if (!previousStatus) {
            serviceController.serviceAction(testSVC, "stop");
        }
        boolean finalStatus = serviceController.serviceAlive(testSVC);
        System.out.printf("Service %s final status is %s\n", testSVC, finalStatus);
    }


}