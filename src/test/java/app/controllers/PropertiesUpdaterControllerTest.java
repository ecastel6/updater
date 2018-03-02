package app.controllers;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesUpdaterControllerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void updatePropertyFile() {
        PropertiesUpdaterController puc = new PropertiesUpdaterController();

        puc.updatePropertyFile(
                FileUtils.getFile("c:/prop/rabbitmq_old.properties"),
                FileUtils.getFile("c:/prop/rabbitmq_new.properties"),
                true);
    }
}