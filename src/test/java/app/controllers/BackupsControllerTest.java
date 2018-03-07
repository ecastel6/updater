package app.controllers;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupsControllerTest
{

    @Test
    void getLastBackupSize() {
    }

    @Test
    void getLastBackupDate() {
    }

    @Test
    void getRootBackupsDir() {
        BackupsController backupsController = BackupsController.getInstance();
        File backupsDir = backupsController.getRootBackupsDir();
        System.out.printf("Root backups dir: %s\n", backupsDir.toString());
        assertTrue(backupsDir.exists());
    }
}