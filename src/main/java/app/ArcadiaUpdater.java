package app;

import app.controllers.ArcadiaController;
import app.controllers.BackupsController;
import app.controllers.ServiceController;
import app.core.UpdateException;
import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.ReturnValues;
import org.apache.commons.io.FileUtils;

public class ArcadiaUpdater {

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
            throw new UpdateException("Tomcat not stoped!!");
        }
        else System.out.println("OK: Tomcat is stoped");
        // Backup database
        BackupsController backupsController = BackupsController.getInstance();
        backupsController.databaseBackup(
                app.getDatabaseName(),
                FileUtils.getFile(
                        backupsController.getRootBackupsDir(),
                        "daily",
                        app.getDatabaseName()));


        // Check database backup size
        // Check last database backup size
        // Backup ArcadiaResources

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
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        //Map<String, ArcadiaAppData> arcadiaAppData = null;
        arcadiaController.getInstalledApps();
        System.out.printf("%s valid app targets\n", arcadiaController.installedApps.size());
        for (ArcadiaAppData arcadiaAppData : arcadiaController.installedApps.values()) {
            System.out.println(arcadiaAppData.toString());
        }
//        for (ArcadiaAppData arcadiaAppData:
//                arcadiaController.getInstalledApps()) {
//            System.out.println(arcadiaAppData.toString());
//        }
        System.exit(0);

        /*boolean result;
        try {
            result = updateApp(app);
        } catch (UpdateException e) {
            System.out.printf("ERROR: %s", e.getMessage());
            //e.printStackTrace();
        }*/


    }
}
