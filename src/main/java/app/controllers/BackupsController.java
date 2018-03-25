package app.controllers;

import app.models.ArcadiaApp;
import app.models.ReturnValues;
import app.models.SearchType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupsController {
    // todo check backups
    // todo backup database

    private File rootBackupsDir = null;

    private String today = null;

    private static BackupsController ourInstance = new BackupsController();

    public static BackupsController getInstance() {
        return ourInstance;
    }

    private BackupsController() {
        if (this.rootBackupsDir == null) this.rootBackupsDir = getRootBackupsDir();
    }

    public BigInteger getDirSize(File directory) {
        return FileUtils.sizeOfDirectoryAsBigInteger(directory);
    }

    public String getToday() {
        if (today == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm.EEEE");
            Date now = new Date();
            today = simpleDateFormat.format(now);
        }
        return today;
    }




    public File getLastBackupDir(ArcadiaApp app) {
        File directory = FileUtils.getFile(this.rootBackupsDir, app.getDatabaseName());
        System.out.printf("Searching %s directory\n", directory);

        // app backups directory not found
        if (!directory.exists()) return null;
        File[] listDirectories = directory.listFiles();

        // app backups directory empty
        if (listDirectories.length == 0) return null;
        return new FileSystemCommons().sortDirectoriesByDate(listDirectories)[0];
    }

    public File getRootBackupsDir() {
        if (rootBackupsDir == null) {
            ArcadiaController arcadiaController = ArcadiaController.getInstance();
            // Simple shot, lowerDepthDirectory, guess system has daily
            FileFinderController fileFinderController = FileFinderController.doit("/", "daily", SearchType.Directories);
            rootBackupsDir = arcadiaController.getLowerDepthDirectory(fileFinderController.results);
        }
        return rootBackupsDir;
    }

    public void setRootBackupsDir(File rootBackupsDir) {
        this.rootBackupsDir = rootBackupsDir;

    }

    public int databaseBackup(String database, File targetFolder) {
        String[] command = new String[]{
                "pg_dump",
                "-U",
                "postgres",
                "-Fd",
                "-b",
                "-f",
                targetFolder.toString(),
                database
        };
        ServiceController serviceController = ServiceController.getInstance();
        ReturnValues returnValues = serviceController.runCommand(command);
        return (int) returnValues.t;
    }

}










