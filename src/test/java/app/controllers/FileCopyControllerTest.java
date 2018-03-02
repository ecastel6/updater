package app.controllers;
//
// DEPRECATED
// Decided to go with apache commons-io
//
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class FileCopyControllerTest {
    Path testSourcePath, testTargetPath, testEmptyTargetPath;
    Path testFile1, testFile2;

    @BeforeEach
    void setUp() {
        try {
            testSourcePath = Files.createTempDirectory("junitSource");
            testTargetPath = Files.createTempDirectory("junitTarget");
            testEmptyTargetPath = Paths.get(testSourcePath.getParent().toString(), "junitTarget");

            testFile1 = Files.createTempFile(testSourcePath, "sample", "file");
            testFile2 = Files.createTempFile(testSourcePath, "sample", "file");
            System.out.printf("testSourcePath: %s\ntestTargetPath:%s\n", testSourcePath, testTargetPath);
            System.out.printf("testFile1: %s\ntestFile2: %s\n\n", testFile1, testFile2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void copyDirectory() {
        File sourceDir = new File("/home/ecastel/Música/");
        File targetDir = new File("/tmp/target");
        try {
            FileUtils.copyDirectory(sourceDir, targetDir, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*System.out.printf("%s:\nSource: %s\nTarget: %s\n", this.getClass(), testSourcePath, testEmptyTargetPath);
        FileCopyController.copyDirectory(testSourcePath, testEmptyTargetPath);
        FileCopyController.copyDirectory(Paths.get("C:", "tmp"), testEmptyTargetPath);*/
    }

    @Test
    void copyFile() {
        System.out.printf("%s: Source: %s\nTarget: %s\n", this.getClass(), testFile1, testTargetPath);
        try {
            Files.copy(testFile1, testTargetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void rename() {
        System.out.printf("Renaming directory %s to %s",
                testSourcePath.toString(),
                testEmptyTargetPath.toString());
        FileCopyController.rename(testSourcePath, testEmptyTargetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /*@AfterEach
    void Cleanup() {
        try {
            System.out.printf("Deleting temp dir: %s\n",testSourcePath);
            FileCopyController.delete(testSourcePath);
            System.out.printf("Deleting temp dir: %s\n",testTargetPath);
            FileCopyController.delete(testTargetPath);

//            FileUtils.deleteDirectory(new File(testSourcePath.toString()));
//            FileUtils.deleteDirectory(new File(testTargetPath.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}