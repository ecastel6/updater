package app.controllers;

import app.models.ArcadiaApp;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateControllerTest
{

    @Test
    void getLatestUpdate() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        for (ArcadiaApp app : ArcadiaApp.values()) {
            Path eachApp = Paths.get(arcadiaController.getArcadiaUpdatesRepository().toString(), app.getShortName());
            File[] subdirs = eachApp.toFile().listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            if (subdirs.length > 0)
                System.out.printf("%s latest update: %s\n",
                        app.getShortName(),
                        new FileSystemCommons().sortDirectoriesByName(subdirs)[0]);
        }
    }


    @Test
    void relativePercentageTest() {
        BackupsController backupsController = BackupsController.getInstance();
        assertEquals(
                backupsController.differencePercentage(1050L, 1000L),
                backupsController.differencePercentage(1000L, 1050L));
    }
}