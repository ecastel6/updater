package app;

import static org.junit.jupiter.api.Assertions.*;

class SysControllerTest
{
    SysController sysController = new SysController();
    String command;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {

        switch (sysController.osType) {
            case "Linux":
                command = "uname";
                break;
            case "Windows":
                command = "ver";
                break;
        }

    }

    @org.junit.jupiter.api.Test
    void runCommand() {
        assertTrue((sysController.runCommand(command)).u.toString().contains(sysController.osType));
    }

    @org.junit.jupiter.api.Test
    void serviceAction() {
        assertEquals(-1,sysController.serviceAction("invalidService","invalidAction").t);
        assertEquals(0,(sysController.serviceAction("cron","status").t));
        assertTrue(sysController.serviceAction("cron","status").u.toString().contains("Loaded"));
        assertEquals(0,sysController.serviceAction("cron","restart").t);
    }
}