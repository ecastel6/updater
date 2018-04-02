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
            startPath = "/home/ecastel";
            testPattern = "opt/arcadiaVersions";
            testFile = "opt/pgsql/share/psqlrc.sample";
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
        /// Check GlobSearch
        long startTime = System.currentTimeMillis();
        FileFinderController fileFinder = FileFinderController.doit(
                startPath, testFile, SearchType.Files);
        System.out.println(fileFinder.getResults().toString());
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.printf("Elapsed time.GlobSearch: %s\n", elapsedTime);

        /// Check StringSearch
        startTime = System.currentTimeMillis();
        FileFinderControllerStr fileFinderStr = FileFinderControllerStr.doit(
                startPath, testFile, SearchType.Files);
        System.out.println(fileFinderStr.getResults().toString());
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.printf("Elapsed time.StringSearch: %s\n", elapsedTime);

        /// Check StringUtilsSearch
        startTime = System.currentTimeMillis();
        FileFinderControllerStrUtl fileFinderStrUtl = FileFinderControllerStrUtl.doit(
                startPath, testFile, SearchType.Files);
        System.out.println(fileFinderStrUtl.getResults().toString());
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.printf("Elapsed time.StringUtilsSearch: %s\n", elapsedTime);

        assertTrue(fileFinderStr.getNumMatches() > 0);
    }

    @Test
    void checkFindDirectory() {
        testPattern = "/daily";
        /* long startTime = System.currentTimeMillis();
        FileFinderController fileFinder = FileFinderController.doit(startPath, testPattern, SearchType.Directories);
        System.out.println(fileFinder.getResults().toString());
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.printf("Elapsed time.GlobSearch: %s\n", elapsedTime);*/

        long startTime = System.currentTimeMillis();
        FileFinderControllerStr fileFinderStr = FileFinderControllerStr.doit(startPath, testPattern, SearchType.Directories);
        System.out.println(fileFinderStr.getResults().toString());
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.printf("Elapsed time.StringSearch: %s\n", elapsedTime);

        assertTrue(fileFinderStr.getNumMatches() > 0);
    }

    @Test
    void checkFindFileAndDirectory() {
        FileFinderController fileFinder = FileFinderController.doit(startPath, testPattern, SearchType.All);
        assertTrue(fileFinder.getNumMatches() > 0);
    }
}