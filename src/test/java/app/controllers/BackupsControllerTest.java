package app.controllers;

import app.models.ArcadiaApps;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupsControllerTest
{

    @Test
    void getLastBackupSize() {
    }

    @Test
    void getLastBackupDate() {
        BackupsController backupsController = BackupsController.getInstance();
        Date appBackupDate;
        for (ArcadiaApps app : ArcadiaApps.values()) {
            appBackupDate = backupsController.getLastBackupDate(app);
            if (appBackupDate != null) {
                System.out.printf("%s Date: %s\n", app.getLongName(), appBackupDate.toString());
            } else {
                System.out.printf("Backup directory of %s not found\n", app.getLongName());
            }
        }
    }

    @Test
    void getRootBackupsDir() {
        BackupsController backupsController = BackupsController.getInstance();
        File backupsDir = backupsController.getRootBackupsDir();
        System.out.printf("Root backups dir: %s\n", backupsDir.toString());
        assertTrue(backupsDir.exists());
    }
}