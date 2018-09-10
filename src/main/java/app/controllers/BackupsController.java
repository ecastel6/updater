package app.controllers;

import app.models.ArcadiaApp;
import app.models.ReturnValues;
import app.models.SearchType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BackupsController {
    private static LogController logController = LogController.getInstance();

    private static BackupsController ourInstance = new BackupsController();
    public boolean noRootBackupDirectory = false;
    private File rootBackupsDir;
    private String today = null;

    private BackupsController() {
        this.noRootBackupDirectory = false;
    }

    public static BackupsController getInstance() {
        return ourInstance;
    }


    public Long getLatestBackupSize(ArcadiaApp app) {
        return FileUtils.sizeOfDirectory(getLastBackupDir(app));
    }

    public File getLastBackupDir(ArcadiaApp app) {
        File directory = FileUtils.getFile(this.getRootBackupsDir(), app.getDatabaseName());
        logController.log.info(String.format("Checking %s directory. Searching latest backup.", directory));

        // app backups directory not found
        if (!directory.exists()) return null;
        File[] listDirectories = directory.listFiles();

        // app backups directory empty
        if (listDirectories.length == 0) return null;
        File latestDir = new SystemCommons().sortDirectoriesByDate(listDirectories)[1];
        return latestDir;
    }

    public File getRootBackupsDir() {
        if (rootBackupsDir == null) {
            ArcadiaController arcadiaController = ArcadiaController.getInstance();
            // Simple shot, lowerDepthDirectory, guess system has daily
            logController.log.config("Searching daily backups folder...");
            FileFinderControllerStr fileFinderController = FileFinderControllerStr.doit("/", "/daily", SearchType.Directories);
            if (fileFinderController.getNumMatches() > 0) {
                logController.log.config(String.format("Found %s backups folder. Selecting lowestdepth one", fileFinderController.getNumMatches()));
                rootBackupsDir = arcadiaController.getLowerDepthDirectory(fileFinderController.results);
            } else noRootBackupDirectory = true;
        }
        return rootBackupsDir;
    }

    public void setRootBackupsDir(File rootBackupsDir) {
        this.rootBackupsDir = rootBackupsDir;

    }

    public int databaseBackup(String database, File targetFolder, String dbhost, String dbport, String dbuser, String dbpass) {
        DbController dbController = DbController.getInstance();
        File pgpass;
        pgpass = dbController.setCredentials(dbuser, dbpass);
        if (!targetFolder.getParentFile().exists()) {
            try {
                FileUtils.forceMkdirParent(targetFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] command = new String[]{
                dbController.getPg_dump().toString(),
                "-U",
                dbuser,
                "-Fd",
                "-b",
                "-h",
                dbhost,
                "-p",
                dbport,
                "-w",
                String.format("-j%s", new SystemCommons().getAvailableCores() / 2),
                "-f",
                targetFolder.toString(),
                database
        };
        logController.log.config(String.format("Running command: %s", Arrays.toString(command)));
        ServiceController serviceController = ServiceController.getInstance();
        ReturnValues returnValues = serviceController.runCommand(command);
        dbController.unsetCredentials(pgpass);

        return (int) returnValues.t;
    }

    /*
    Relative difference percentaje
     */
    public double differencePercentage(Long v1, Long v2) {
        Long z = Math.abs(v1 - v2);
        double p = (v1 + v2) / 2;
        p = Math.abs((z / p) * 100);
        logController.log.config(String.format("Difference percentage: %s", p));
        return p;
    }
}