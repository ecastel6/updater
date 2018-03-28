package app;

import app.controllers.ArcadiaController;
import app.controllers.UpdateController;
import app.core.UpdateException;
import app.models.ArcadiaAppData;

import java.util.HashMap;
import java.util.Map;

public class ArcadiaUpdater
{

    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();

    public static void main(String[] args) {
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

        if (args.length == 0) System.out.println("No parameters, auto update in progress...");


        //public ArcadiaAppData(ArcadiaApp app, File installedDir, String portNumber, String version) {

        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        arcadiaController.getInstalledApps();
        System.out.printf("%s valid app targets\n", arcadiaController.installedApps.size());
        for (ArcadiaAppData arcadiaAppData : arcadiaController.installedApps.values()) {
            System.out.printf("Updating %s ...\n",arcadiaAppData.toString());
            boolean result;
            UpdateController updateController = new UpdateController(arcadiaAppData);
            try {
                result = updateController.updateApp(arcadiaAppData);
            } catch (UpdateException e) {
                System.out.println(e.getMessage());
            }
        }

        System.exit(0);
    }
}
