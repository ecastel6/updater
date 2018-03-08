package app.controllers;

import app.models.ArcadiaApps;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupsControllerTest
{


    @Test
    void testGetLastBackupDir() {
        BackupsController backupsController = BackupsController.getInstance();
        File appBackupDir;
        for (ArcadiaApps app : ArcadiaApps.values()) {
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
}