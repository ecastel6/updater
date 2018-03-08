package app.controllers;

import app.models.ArcadiaApps;
import app.models.SearchType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
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
        File directory = FileUtils.getFile(this.getRootBackupsDir(), app.getDatabaseName());
        if (!directory.exists()) return null;
        System.out.printf("Looking for directories in: %s\n", directory);
        File[] listDirectories = directory.listFiles();

        Arrays.sort(listDirectories, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        System.out.println(listDirectories[0]);
        return null;
    }

    public File getRootBackupsDir() {
        ArcadiaController arcadiaController = new ArcadiaController();
        // Simple shot, lowerDepthDirectory, guess system has daily
        FileFinderController fileFinderController = FileFinderController.doit("/", "daily", SearchType.Directories);
        return arcadiaController.getLowerDepthDirectory(fileFinderController.results);
    }

    public void setRootBackupsDir(File rootBackupsDir) {
        this.rootBackupsDir = rootBackupsDir;
    }

    public void createZipBackup(File startDir) {

        //https://commons.apache.org/proper/commons-compress/examples.html

    }

}










