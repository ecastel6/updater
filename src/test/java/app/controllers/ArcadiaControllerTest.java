package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArcadiaControllerTest {

    @Test
    void getArcadiaAppDirTest() {
        ArcadiaController ac = ArcadiaController.getInstance();
        for (ArcadiaApp app : ArcadiaApp.values()) {
            File o = ac.getTomcatDir("tomcat_" + app.getShortName());
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
    void getInstalledAppsTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();

        Map<String, ArcadiaAppData> arcadiaAppDataArrayList = arcadiaController.getInstalledApps();

        for (Map.Entry<String, ArcadiaAppData> entry : arcadiaAppDataArrayList.entrySet())
            System.out.println(entry.toString());
    }

    @Test
    void getArcadiaUpdatesRepositoryTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        assertNotNull(arcadiaController.getArcadiaUpdatesRepository());
    }

}


