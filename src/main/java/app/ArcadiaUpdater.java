package app;

import app.controllers.*;
import app.core.UpdateException;
import app.core.ZipHandler;
import app.models.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ArcadiaUpdater {

    private ArcadiaController arcadiaController = ArcadiaController.getInstance();
    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private BackupsController backupsController = BackupsController.getInstance();

    public Boolean updateApp(ArcadiaApp app) throws UpdateException {

        // Initialize general Directories variables
        FileFinderController fileFinder =
                FileFinderController.doit("/", "arcadiaVersions", SearchType.Directories);

        // updates base directory
        // /opt/arcadiaVersions
        File appUpdatesDirectory = FileUtils.getFile(
                arcadiaController.getLowerDepthDirectory(fileFinder.getResults()),
                app.getShortName());
        System.out.printf("ArcadiaUpdater.updateApp updatesdir:%s\n", appUpdatesDirectory.toString());

        // latest update available

        File latestAppUpdatesDirectory = getLatestUpdate(appUpdatesDirectory.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));
        System.out.printf("ArcadiaUpdater.updateApp latestupdDir:%s\n", latestAppUpdatesDirectory.toString());

        // updating app base dir
        // /opt/tomcat_cbos e.g
        File installedAppDir = testInstalledApps.get(app.name()).getInstalledDir();

        // Check database service
        ServiceController serviceController = ServiceController.getInstance();
        if (!serviceController.serviceAlive("postgres")) {
            throw new UpdateException("Database not started");
        }
        else System.out.println("OK: Database Server available");

        // Stop tomcat service
        //serviceController.serviceAction("tomcat_cbos","stop");

        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "stop");

        // Check tomcat stopped
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            throw new UpdateException("Tomcat not stopped!!");
        } else System.out.println("OK: Tomcat is stopped");

        // Backup database

        File targetBackupDir = FileUtils.getFile(
                backupsController.getRootBackupsDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + backupsController.getToday());
        if (backupsController.databaseBackup(app.getDatabaseName(), targetBackupDir) > 0) {
            throw new UpdateException("Error while backup'in database");
        }

        // Check last database backup size
        BigInteger lastBackupSize = backupsController.getDirSize(backupsController.getLastBackupDir(app));

        // Check database backup size
        BigInteger targetBackupDirSize = backupsController.getDirSize(targetBackupDir);
        System.out.printf("LastBackupSize: %s\ntargetBackupDirSize: %s\n", lastBackupSize, targetBackupDirSize);
        // Backup ArcadiaResources
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
        /*String sourceFolder =
                arcadiaController.installedApps.get(
                        app.name()).getInstalledDir().toString() +
                        separator +
                        "webapps" +
                        separator +
                        "Arcadiaresources";*/
        String sourceFolder =
                installedAppDir.toString() +
                        separator +
                        "webapps" +
                        separator +
                        "Arcadiaresources";

        String outputZip = backupsController.getRootBackupsDir().toString() +
                separator +
                "ArcadiaResources." + app.getShortName() + "." +
                backupsController.getToday() + ".zip";
        System.out.printf("Compressing folder %s to output zip: %s\n", sourceFolder, outputZip);
        zipHandler.zip(sourceFolder, outputZip, CompresionLevel.UNCOMPRESSED.getLevel());

        // Move sharedlib to backout
        System.out.printf("Moving %s to %s",
                Paths.get(installedAppDir.toString(), "sharedlib"),
                Paths.get(latestAppUpdatesDirectory.toString(), "backout"));

        FileCopyController.move(
                Paths.get(installedAppDir.toString(), "sharedlib"),
                Paths.get(latestAppUpdatesDirectory.toString(), "backout", "sharedlib"),
                StandardCopyOption.REPLACE_EXISTING);

        // Move logback-common.xml to backout
        // Copy custom to backout
        // Move webapps to backout
        // Move backout/webapps/ArcadiaResources to webapps
        // Move commons WEB-INF to backout
        // Move logs to backout
        // Create logs
        // Clean tomcat cache

        // Update resources
        // Copy new logback
        // Copy new sharedlib
        // Copy jars
        // Copy wars but ArcadiaResources
        // Update custom

        // Check rabbitmq
        // Check zookeeper
        // Start service
        // Check schema_version all ok


        return true;
    }

    // Input Array of directories
    // returns first sorted
    public File getLatestUpdate(File[] updatesDir) {
        backupsController.sortDirectoriesByDate(updatesDir);
        return updatesDir[0];
    }

    public static void main(String[] args) {
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/
        if (args.length == 0) System.out.println("No parameters, auto update in progress...");

        //public ArcadiaAppData(ArcadiaApp app, File installedDir, String portNumber, String version) {
        ArcadiaAppData testArcadiaAppData = new ArcadiaAppData(
                ArcadiaApp.CBOS,
                //new File("/home/ecastel/opt/tomcat_cbos"),
                new File("d:/opt/tomcat_cbos"),
                "81",
                "12R1");
        testInstalledApps.put("CBOS", testArcadiaAppData);

        ArcadiaUpdater arcadiaUpdater = new ArcadiaUpdater();
        try {
            arcadiaUpdater.updateApp(ArcadiaApp.CBOS);
        } catch (UpdateException e) {
            e.printStackTrace();
        }
        /*ArcadiaController arcadiaController=ArcadiaController.getInstance();
        arcadiaController.getInstalledApps();
        System.out.printf("%s valid app targets\n", arcadiaController.installedApps.size());
        for (ArcadiaAppData arcadiaAppData : arcadiaController.installedApps.values()) {
            System.out.printf("Updating %s ...\n",arcadiaAppData.toString());
            boolean result;
            try {
                result = updateApp(arcadiaAppData.getApp());
            } catch (UpdateException e) {
                System.out.printf("ERROR: %s", e.getMessage());
                //e.printStackTrace();
            }
        }*/
//        for (ArcadiaAppData arcadiaAppData:
//                arcadiaController.getInstalledApps()) {
//            System.out.println(arcadiaAppData.toString());
//        }
        System.exit(0);



    }
}
