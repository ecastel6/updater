package app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileCopyControllerTest {
    Path testSourcePath, testTargetPath;
    Path sampleFile;

    @BeforeEach
    void setUp() {
        try {
            testSourcePath = Files.createTempDirectory("junitSource");
            testTargetPath = Paths.get(testSourcePath.getParent().toString(), "junitTarget");

            System.out.println(testSourcePath.toString());
            System.out.println(testTargetPath.toString());
            sampleFile = Files.createTempFile(testSourcePath, "sample", "file");
            System.out.println(sampleFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void copyDirectory() {
        FileCopyController.copyDirectory(testSourcePath, testTargetPath);
    }

    @Test
    void copyFile() {
        System.out.println("Copying " + sampleFile.toString() + " to c:\\tmp");
        FileCopyController.copyDirectory(sampleFile, Paths.get("c:\\tmp", ""));
    }
}