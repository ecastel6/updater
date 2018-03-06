package app.controllers;

import app.models.ArcadiaAppData;
import app.models.ArcadiaApps;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArcadiaControllerTest
{

    @Test
    void getArcadiaAppDir() {
        ArcadiaController ac = new ArcadiaController();
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
        ArcadiaController ac = new ArcadiaController();
        ArrayList<ArcadiaAppData> arcadiaAppDataArrayList = ac.getInstalledApps();
        for (ArcadiaAppData appdata : arcadiaAppDataArrayList) {
            System.out.println(appdata.toString());
        }
    }


    @Test
    void getArcadiaAppPort() {
        ArcadiaController arcadiaController = new ArcadiaController();
        assertEquals("8", arcadiaController.getArcadiaAppPort(ArcadiaApps.CBOS).substring(0, 1));
    }
}