package app.controllers;

import app.models.ArcadiaApps;

import java.io.File;

public class ArcadiaController
{
    public int getArcadiaAppPort(String ArcadiaApp) {

        return 0;
    }

    public String getArcadiaVersion(String ArcadiaApp) {

        return "";
    }

    public File getArcadiaAppDir(ArcadiaApps appName) {
        // find directory tomcat + short name
        //FileFinderController directoryFinder = new FileFinderController("/",2,"tomcat_"+appName,)
        return new File("/opt");
    }
}
