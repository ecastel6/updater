package app.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateControllerTest {


    @Test
    void relativePercentageTest() {
        BackupsController backupsController = BackupsController.getInstance();
        assertEquals(
                backupsController.differencePercentage(1050L, 1000L),
                backupsController.differencePercentage(1000L, 1050L));
    }
}