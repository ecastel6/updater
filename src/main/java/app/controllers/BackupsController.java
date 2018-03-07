package app.controllers;

import app.models.ArcadiaApps;
import app.models.SearchType;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;

public class BackupsController
{
    // todo check backups
    // todo get backup size
    // todo backup ArcadiaResources
    // todo backup database

    private File rootBackupsDir;

    private static BackupsController ourInstance = new BackupsController();

    public static BackupsController getInstance() {
        return ourInstance;
    }

    private BackupsController() {
    }

    public BigInteger getLastBackupSize(ArcadiaApps app) {
        //public static BigInteger sizeOfDirectoryAsBigInteger(File directory)
        return null;
    }

    public Date getLastBackupDate(ArcadiaApps app) {

        return null;
    }

    public File getRootBackupsDir() {
        ArcadiaController arcadiaController = new ArcadiaController();
        // Simple shot, lowerDepthDirectory, guess system has daily and parent is the searched dir
        FileFinderController fileFinderController = FileFinderController.doit("/", "daily", SearchType.Directories);
        return arcadiaController.getLowerDepthDirectory(fileFinderController.results).getParentFile();
    }

    public void setRootBackupsDir(File rootBackupsDir) {
        this.rootBackupsDir = rootBackupsDir;
    }

    public void createZipBackup(File startDir) {

        //https://commons.apache.org/proper/commons-compress/examples.html

    }

}










