package app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbControllerTest {

    @BeforeEach
    void setUp() {


    }

    @Test
    void getServerDir() {
        DbController dbController = DbController.getInstance();
        System.out.println(dbController.serverDir.toString());
        assertTrue(dbController.serverDir.endsWith("pgsql"));
    }

    @Test
    void getServerPort() {
        DbController dbController = DbController.getInstance();
        assertTrue(dbController.getServerPort().contains("543"));
    }

    @Test
    void getServerConfFilename() {
        DbController dbController = DbController.getInstance();
        try {
            assertTrue(dbController.getServerConfFilename().toString().contains("postgresql.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void getStatus() {
        DbController dbController = DbController.getInstance();
        ServiceController serviceController = ServiceController.getInstance();
        assertEquals(dbController.getStatus(),
                serviceController.serviceAlive("postgres"));
    }

    @Test
    void getDatabaseList() {
        DbController dbController = DbController.getInstance();
        assertTrue(dbController.getDatabaseList().size() > 2);
    }

    @Test
    void getServerVersion() {
        DbController dbController = DbController.getInstance();
        String serverVersion = dbController.getServerVersion();
        System.out.printf("Server version: \n %s\n", serverVersion);
        assertTrue(serverVersion.contains("PostgreSQL"));
    }

    @Test
    void getAdminUser() {
    }

    @Test
    void getAdminPasswd() {
    }

    @Test
    void databaseBackup() {
        DbController dbController = DbController.getInstance();
        try {
            Path targetFolder = Files.createTempDirectory(this.getClass().toString());
            System.out.println(targetFolder.toString());
            assertEquals(0, dbController.databaseBackup("template1", targetFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}