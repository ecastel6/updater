package app.controllers;

import app.models.ArcadiaApp;
import app.models.ReturnValues;
import app.models.SearchType;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class BackupsController {
    private static BackupsController ourInstance = new BackupsController();
    private File rootBackupsDir = null;
    private String today = null;

    private BackupsController() {
    }

    public static BackupsController getInstance() {
        return ourInstance;
    }



    public Long getLatestBackupSize(ArcadiaApp app) {
        return FileUtils.sizeOfDirectory(getLastBackupDir(app));
    }

    public File getLastBackupDir(ArcadiaApp app) {
        File directory = FileUtils.getFile(this.getRootBackupsDir(), app.getDatabaseName());
        System.out.printf("Checking %s directory. Searching latest backup.\n", directory);

        // app backups directory not found
        if (!directory.exists()) return null;
        File[] listDirectories = directory.listFiles();

        // app backups directory empty
        if (listDirectories.length == 0) return null;
        return new SystemCommons().sortDirectoriesByDate(listDirectories)[0];
    }

    public File getRootBackupsDir() {
        if (rootBackupsDir == null) {
            ArcadiaController arcadiaController = ArcadiaController.getInstance();
            // Simple shot, lowerDepthDirectory, guess system has daily
            FileFinderControllerStr fileFinderController = FileFinderControllerStr.doit("/", "/daily", SearchType.Directories);
            if (fileFinderController.getNumMatches() > 0)
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

    /*
    Relative difference percentaje
     */
    public double differencePercentage(Long v1, Long v2) {
        Long z = Math.abs(v1 - v2);
        double p = (v1 + v2) / 2;
        p = Math.abs((z / p) * 100);
        System.out.printf("Difference percentage: %s\n", p);
        return p;
    }
}