package app.controllers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonsIoFileutilsTest
{
    File testSourceDir;
    File testTargetDir;
    File tempDirectory;
    File renamedDir;

    @BeforeEach
    void setUp() throws IOException {

        tempDirectory = FileUtils.getTempDirectory();
        testSourceDir = FileUtils.getFile(tempDirectory, "junitSource");
        FileUtils.forceMkdir(testSourceDir);
        for (Integer i = 1; i < 5; i++) {
            FileUtils.touch(FileUtils.getFile(testSourceDir, i.toString()));
        }
        testTargetDir = FileUtils.getFile(tempDirectory, "junitTarget");
        FileUtils.forceMkdir(testTargetDir);
        renamedDir = FileUtils.getFile(tempDirectory, "junitRenamed");
        System.out.printf("Created source dir: %s\nTarget dir: %s\n", testSourceDir, testTargetDir);
    }

    @Test
    void copyDirectory() throws IOException {
        System.out.printf("%s:\nSource: %s\nTarget: %s\n",
                "copyDirectory",
                testSourceDir.toString(),
                testTargetDir.toString());

        FileUtils.copyDirectory(
                testSourceDir,
                testTargetDir,
                true);
        assertTrue(FileUtils.directoryContains(testTargetDir, FileUtils.getFile(testTargetDir, "3")));

    }

    @Test
    void copyFileToDirectory() throws IOException {
        System.out.printf("%s:\nSource file: %s\nTarget: %s\n",
                "copyFileToDirectory",
                FileUtils.getFile(testSourceDir, "1").toString(),
                testTargetDir);

        FileUtils.copyFile(
                FileUtils.getFile(testSourceDir, "1"),
                FileUtils.getFile(testTargetDir, "1"),
                true);
        assertTrue(FileUtils.directoryContains(testTargetDir, FileUtils.getFile(testTargetDir, "1")));
    }

    @Test
    void renameDirectory() throws IOException {

        System.out.printf("%s: Source: %s\nTarget: %s\n",
                "renameDirectory",
                testSourceDir,
                renamedDir);

        FileUtils.moveDirectory(testSourceDir, renamedDir);
        assertTrue(FileUtils.directoryContains(renamedDir, FileUtils.getFile(renamedDir, "1")));
    }

    @Test
    void testListFilesAndDirs() {
        System.out.println("Searching for ArcadiaResources dir.");
//        Collection<File> filesAndDirs=new ArrayList<>();
//        IOFileFilter onlyArcadiaResources=new NameFileFilter( "ArcadiaResources");
//        String dir="d:/opt";
//
//        filesAndDirs=FileUtils.listFilesAndDirs(new File(dir), new NotFileFilter(TrueFileFilter.INSTANCE), onlyArcadiaResources);
//        System.out.printf("Found %s results\n",filesAndDirs.size());
//        for (File directory:filesAndDirs) {
//            System.out.println(directory);
//        }
        File directory = new File("d:/opt");
        File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File dir : subdirs) {
            System.out.println("Directory: " + dir.getName());
        }
    }

    @AfterEach
    void Cleanup() {
        try {
            System.out.printf("Deleting testSource dir: %s\n", testSourceDir);
            FileUtils.deleteDirectory(testSourceDir);
            System.out.printf("Deleting testTarget dir: %s\n", testTargetDir);
            FileUtils.deleteDirectory(testTargetDir);
            if (renamedDir.exists()) {
                System.out.printf("Deleting renamed dir: %s\n", renamedDir);
                FileUtils.deleteDirectory(renamedDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}