package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        String response = arcadiaController.getArcadiaVersionFromHTTP(ArcadiaApp.CBOS, "81").toString();
        System.out.printf("Version from response: %s\n", response);
    }

    @Test
    void getAvailableUpdatesTest() {
        Map<String, ArcadiaAppData> testMap = new HashMap<>();
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        testMap = arcadiaController.getAvailableUpdates();
        for (Map.Entry<String, ArcadiaAppData> entry : testMap.entrySet()) {
            System.out.printf("App: %s -> Version: %s Directory: %s\n", entry.getKey(), entry.getValue().getVersion(), entry.getValue().getDirectory());
        }
    }

    @Test
    void getVersionFromFileTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        System.out.printf("Valor devuelto es %s", arcadiaController.getVersionFromFile("c:/tmp/version"));

    }

    @Test
    void getArcadiaDatabasesTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        Map<String, String[]> testdbs;
        testdbs = arcadiaController.getArcadiaDatabases(new File("D:\\opt\\tomcat_cbos\\conf\\server.xml"));
        for (Map.Entry<String, String[]> entry : testdbs.entrySet()) {
            System.out.printf("Clave:%s user:%s pass:%s\n", entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
        }
    }

    @Test
    void getDBUrlDecodedTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        Map<String, String> testUrl;
        testUrl = arcadiaController.getDBUrlDecoded("jdbc:postgresql://localhost:5432/arcadia_cbos");
        System.out.println(testUrl.toString());
        assertEquals(testUrl.get("sgbd"), "postgresql");
        assertEquals(testUrl.get("host"), "localhost");
        assertEquals(testUrl.get("port"), "5432");
        assertEquals(testUrl.get("dbname"), "arcadia_cbos");
    }


}