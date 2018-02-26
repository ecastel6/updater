package app.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class DbController
{
    private static DbController ourInstance;

    static {
        try {
            ourInstance = new DbController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DbController getInstance() {
        return ourInstance;
    }

    // Root database directory e.g. /opt/pgsql
    Path serverDir;
    int serverPort;
    // Database status
    byte status;
    ArrayList<String> databaseList;
    String serverVersion;
    String adminUser;
    String adminPasswd;

    private DbController() throws IOException {
        this.serverDir = getServerDir();
        this.serverPort = getServerPort();
        this.status = getStatus();
        this.databaseList = getDatabaseList();
        this.serverVersion = getServerVersion();
        this.adminUser = getAdminUser();
        this.adminPasswd = getAdminPasswd();
    }

    public Path getServerDir() throws IOException {
        FileFinderController rootDB = FileFinderController.doit("/home/ecastel/opt", "pgsql", 2);
        if (rootDB.getNumMatches() == 0) {
            throw new IOException("No Postgres installation detected");
        }
        return rootDB.results.get(0);
    }


    public int getServerPort() {
        return serverPort;
    }

    public byte getStatus() {
        return status;
    }

    public ArrayList<String> getDatabaseList() {
        return databaseList;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPasswd() {
        return adminPasswd;
    }


// todo check database online
// todo  search postgres enabled port
// todo search postgres dir
}