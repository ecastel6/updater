package app.controllers;

import app.models.OS;
import app.models.SearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileFinderControllerTest {
    private String startPath, testPattern, testFile;

    @BeforeEach
    void setUp() {
        ServiceController serviceController = ServiceController.getInstance();
        if (serviceController.os.equals(OS.LINUX)) {
            startPath = "/";
            testPattern = "root";
            testFile = "hosts";
        } else {
            if (serviceController.os.equals(OS.WINDOWS)) {
                startPath = "/windows/system32";
                testPattern = "drivers";
                testFile = "hosts";
            }
        }
    }

    @Test
    void checkFindFile() {
        FileFinderController filefinder = FileFinderController.doit(startPath, testFile, SearchType.Files);
        assertTrue(filefinder.getNumMatches() > 0);
    }

    @Test
    void checkFindDirectory() {
        FileFinderController fileFinder = FileFinderController.doit(startPath, testPattern, SearchType.Directories);
        assertTrue(fileFinder.getNumMatches() > 0);
    }

    @Test
    void checkFindFileAndDirectory() {
        FileFinderController fileFinder = FileFinderController.doit(startPath, testPattern, SearchType.All);
        assertTrue(fileFinder.getNumMatches() > 0);
    }
}