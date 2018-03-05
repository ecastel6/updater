package app.controllers;

import app.models.ArcadiaAppData;
import app.models.ArcadiaApps;
import app.models.SearchType;

import java.io.File;
import java.util.ArrayList;

public class ArcadiaController
{

    ArrayList<ArcadiaAppData> installedApps;

    public int getArcadiaAppPort(ArcadiaApps app) {
        // grep tomcat conf search Port
        return 80;
    }

    public String getArcadiaVersion(ArcadiaApps app) {
        return "10.0";
    }

    public File getArcadiaAppDir(ArcadiaApps appName) {
        // find directory tomcat + short name
        FileFinderController arcadiaAppDir = FileFinderController.doit("/", "tomcat_" + appName.getShortName(), SearchType.Directories);
        if (arcadiaAppDir.getNumMatches() > 0) {
            return arcadiaAppDir.getResults().get(0).toFile();
        }
        return null;
    }

    // Search system, returns ArcadiaAppData Arraylist
    public ArrayList<ArcadiaAppData> getInstalledApps() {

        // iterate Enumerated AppList search relevant info
        for (ArcadiaApps app : ArcadiaApps.values()) {
            File dir = this.getArcadiaAppDir(app);
            if (dir != null) {
                // App found collect relevant data
                System.out.println("App found collect relevant data");
                ArcadiaAppData arcadiaAppData = new ArcadiaAppData(
                        app,
                        dir,
                        getArcadiaAppPort(app),
                        getArcadiaVersion(app));
                System.out.printf("getInstalledApps: %s\n", arcadiaAppData.toString());
                //arcadiaController.installedApps.add(arcadiaAppData);
            }
        }
        return installedApps;
    }
}
