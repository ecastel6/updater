package app;

import app.controllers.ArcadiaController;
import app.controllers.BackupsController;
import app.controllers.ServiceController;
import app.core.UpdateException;
import app.core.ZipHandler;
import app.models.ArcadiaApp;
import app.models.CompresionLevel;
import app.models.ReturnValues;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigInteger;

public class ArcadiaUpdater {

    private static ArcadiaController arcadiaController;

    public static Boolean updateApp(ArcadiaApp app) throws UpdateException {

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
        BackupsController backupsController = BackupsController.getInstance();

        File targetBackupDir = FileUtils.getFile(
                backupsController.getRootBackupsDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + backupsController.getToday());
        if (backupsController.databaseBackup(app.getDatabaseName(), targetBackupDir) > 0) {
            throw new UpdateException("Error while backup database");
        }

        // Check last database backup size
        BigInteger lastBackupSize = backupsController.getDirSize(backupsController.getLastBackupDir(app));

        // Check database backup size
        BigInteger targetBackupDirSize = backupsController.getDirSize(targetBackupDir);

        // Backup ArcadiaResources
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
        String sourceFolder =
                arcadiaController.installedApps.get(app.name()).getInstalledDir().toString() +
                        separator +
                        "webapps" +
                        separator +
                        "Arcadiaresources";
        String outputZip = backupsController.getRootBackupsDir().toString() +
                separator +
                "ArcadiaResources." + app.getShortName() +
                backupsController.getToday() + ".zip";
        zipHandler.zip(sourceFolder, outputZip, CompresionLevel.UNCOMPRESSED.getLevel());

        // Move sharedlib to backout
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

    public static void main(String[] args) {
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/
        if (args.length == 0) System.out.println("No parameters, auto update in progress...");

        try {
            updateApp(ArcadiaApp.CBOS);
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
