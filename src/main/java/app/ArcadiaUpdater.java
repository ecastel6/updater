package app;

import app.controllers.ArcadiaController;
import app.controllers.ServiceController;
import app.core.UpdateException;
import app.models.ArcadiaAppData;
import app.models.ReturnValues;

import java.util.Map;

public class ArcadiaUpdater
{

    public static Boolean updateCbos() throws UpdateException {


        // Check database service
        ServiceController serviceController = ServiceController.getInstance();
        if (!serviceController.serviceAlive("postgres"))
            throw new UpdateException("Database not started");
        else System.out.println("OK: Database Server available");

        // Stop tomcat service
        //serviceController.serviceAction("tomcat_cbos","stop");
        String serviceName = "tomcat_cbos";
        ReturnValues returnedValues = serviceController.runCommand(new String[]{"sudo", "/etc/init.d/" + serviceName, "stop", "-force"});

        // Check tomcat stopped
        if (serviceController.serviceAlive("tomcat_cbos"))
            throw new UpdateException("Tomcat not stoped!!");
        else System.out.println("OK: Tomcat is stoped");
        // Backup database

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

        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        Map<String, ArcadiaAppData> arcadiaAppData = arcadiaController.getInstalledApps();
        System.out.printf("%s apps installed\n", arcadiaAppData.size());
//        for (ArcadiaAppData arcadiaAppData:
//                arcadiaController.getInstalledApps()) {
//            System.out.println(arcadiaAppData.toString());
//        }
        System.exit(0);

        boolean resultCbos;
        try {
            resultCbos = updateCbos();
        } catch (UpdateException e) {
            System.out.printf("ERROR: %s", e.getMessage());
            //e.printStackTrace();
        }


    }
}
