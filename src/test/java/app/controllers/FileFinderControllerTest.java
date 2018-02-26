package app.controllers;

import app.models.OS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileFinderControllerTest {
    String startPath, testPattern;

    @BeforeEach
    void setUp() {
        ServiceController serviceController = ServiceController.getInstance();
        if (serviceController.os.equals(OS.LINUX)) {
            startPath = "/";
            testPattern = "root";
        } else {
            if (serviceController.os.equals(OS.WINDOWS)) {
                startPath = "/windows/system32";
                testPattern = "drivers";
            }
        }
    }

    @Test
    void checkFileFinder() {
        FileFinderController fileFinder = FileFinderController.doit("/windows/system32", testPattern, 2);
        System.out.println(fileFinder.results.toString());
        assertTrue(fileFinder.getNumMatches() > 0);
    }
}