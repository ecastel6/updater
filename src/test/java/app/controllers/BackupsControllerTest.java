package app.controllers;

import app.models.ArcadiaApp;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupsControllerTest
{


    @Test
    void testGetLastBackupDir() {
        BackupsController backupsController = BackupsController.getInstance();
        File appBackupDir;
        for (ArcadiaApp app : ArcadiaApp.values()) {
            appBackupDir = backupsController.getLastBackupDir(app);
            if (appBackupDir != null) {
                System.out.printf("%s Date: %s Size: %d\n",
                        app.getLongName(),
                        appBackupDir.toString(),
                        backupsController.getDirSize(appBackupDir));
            } else {
                System.out.printf("Backup directory of %s not found\n", app.getLongName());
            }
        }
    }

    @Test
    void testGetRootBackupsDir() {
        BackupsController backupsController = BackupsController.getInstance();
        File backupsDir = backupsController.getRootBackupsDir();
        System.out.printf("Root backups dir: %s\n", backupsDir.toString());
        assertTrue(backupsDir.exists());
    }

    @Test
    void databaseBackup() {
        BackupsController backupsController = BackupsController.getInstance();
        try {
            Path targetFolder = Files.createTempDirectory(this.getClass().toString());
            System.out.println(targetFolder.toString());
            assertEquals(0, backupsController.databaseBackup("template1", targetFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}