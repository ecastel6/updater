package app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class FileCopyControllerTest {
    Path testSourcePath, testTargetPath;
    Path sampleFile1, sampleFile2;

    @BeforeEach
    void setUp() {
        try {
            testSourcePath = Files.createTempDirectory("junitSource");
            testTargetPath = Paths.get(testSourcePath.getParent().toString(), "junitTarget");

            System.out.println(testSourcePath.toString());
            System.out.println(testTargetPath.toString());
            Path sampleFile1 = Files.createTempFile(testSourcePath, "sample", "file");
            Path sampleFile2 = Files.createTempFile(testSourcePath, "sample", "file");
            System.out.printf("Samplefile1 %s. Samplefile2 %s \n", sampleFile1.toString(), sampleFile2.toString());
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
        System.out.printf("Copying %s to %s\n", sampleFile1.toString(), testTargetPath.toString());
        FileCopyController.copyDirectory(sampleFile1, testTargetPath);
    }

    @Test
    void rename() {
        System.out.printf("Renaming directory %s to %s",
                testSourcePath.toString(),
                testTargetPath.toString());
        FileCopyController.rename(testSourcePath, testTargetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}