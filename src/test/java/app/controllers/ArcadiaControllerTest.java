package app.controllers;

import app.models.ArcadiaAppData;
import app.models.ArcadiaApps;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArcadiaControllerTest
{

    @Test
    void getArcadiaAppDir() {
        ArcadiaController ac = ArcadiaController.getInstance();
        for (ArcadiaApps app : ArcadiaApps.values()) {
            File o = ac.getArcadiaAppDir(app);
            if (o != null) {
                System.out.printf("%s instalada: %s\n", app.getLongName(), o.toString());
                assertNotNull(o);
            } else {
                System.out.printf("%s no instalada\n", app.getLongName());
                assertNull(o);
            }
        }
    }

    @Test
    void getInstalledApps() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        Map<String, ArcadiaAppData> arcadiaAppDataArrayList = arcadiaController.getInstalledApps();
        for (Map.Entry<String, ArcadiaAppData> entry : arcadiaAppDataArrayList.entrySet())
            System.out.println(entry.toString());
    }


/*
    @Test
    void getArcadiaVersion() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        String appVersion;
        for (ArcadiaApps app : ArcadiaApps.values()) {
            appVersion = arcadiaController.getArcadiaVersion(app,);
            if (appVersion != null) {
                System.out.printf("App: %s Version: %s\n",
                        app.getLongName(), appVersion);
            }
        }
    }*/
}


