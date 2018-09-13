package app.controllers;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class PropertiesUpdaterControllerTest
{

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testUpdatePropertyFile() {
        PropertiesUpdaterController puc = new PropertiesUpdaterController();

        puc.updatePropertyFile(
                FileUtils.getFile("c:/prop/rabbitmq_old.properties"),
                FileUtils.getFile("c:/prop/rabbitmq_new.properties")
        );

    }

    @Test
    void testUpdateCustom() throws IOException {
        File sourceOldCustom = FileUtils.getFile("/tmp/sendra/custom.oc.old");
        File sourceNewCustom = FileUtils.getFile("/tmp/sendra/custom.oc.new");
        File targetCustom = FileUtils.getFile("/tmp/sendra/custom.oc.patched");

        PropertiesUpdaterController puc = new PropertiesUpdaterController(sourceOldCustom, sourceNewCustom, targetCustom);
        puc.updateCustom();

    }

    /*@
    relativice() {
        File path = new File("/var/data/stuff/xyz.dat");
        File base = new File("/var/data");
        String relative = base.toURI().relativize(path.toURI()).getPath();
        System.out.println(relative);
        // relative == "stuff/xyz.dat"

    }*/
}