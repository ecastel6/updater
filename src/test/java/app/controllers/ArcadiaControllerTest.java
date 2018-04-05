package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
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
    void getVersionFromResponseTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        String response = "3.11R4-RELEASE<br>Core-Version: 3.20-RELEASE<br>Messaging-Version: 1.11";
        System.out.println(arcadiaController.getVersionFromResponse(response));
    }

    @Test
    void getVersionFromHTTPResponseTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        String response = arcadiaController.getArcadiaVersion(ArcadiaApp.CBOS, "81");
        System.out.printf("Version from response: %s\n", response);
    }

    @Test
    void getAvailableUpdatesTest() {
        Map<String, ArcadiaAppData> testMap = new HashMap<>();
        testMap = ArcadiaController.getInstance().getAvailableUpdates(Paths.get("/home/ecastel/opt/arcadiaVersions"));
        for (Map.Entry<String, ArcadiaAppData> entry : testMap.entrySet()) {
            System.out.printf("App: %s -> Version: %s Directory: %s\n", entry.getKey(), entry.getValue().getVersion(), entry.getValue().getDirectory());
        }
    }

    @Test
    void pp() {
        String version = "latest";
        if (!version.matches("[0-9]+(\\.[0-9]+)*.*")) {
            System.out.println("Dont match");
        }
    }
}