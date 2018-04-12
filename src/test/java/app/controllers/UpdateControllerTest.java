package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateControllerTest {

    @Test
    void relativePercentageTest() {
        BackupsController backupsController = BackupsController.getInstance();
        assertEquals(
                backupsController.differencePercentage(1050L, 1000L),
                backupsController.differencePercentage(1000L, 1050L));
    }

    @Test
    void updateAppTest() {
        UpdateController updateController = new UpdateController();
        ArcadiaAppData appData = new ArcadiaAppData(
                ArcadiaApp.OPENCARD,
                new File("d:/opt/tomcat_oc"),
                null, null);
        updateController.reinstallServices(appData);
    }
}