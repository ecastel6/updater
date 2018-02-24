package app.controllers;

import java.nio.file.Path;
import java.util.ArrayList;

public class DbController
{
    // Root database directory e.g. /opt/pgsql
    Path serverDir;
    int serverPort;
    // Database status
    byte status;
    ArrayList<String> databaseList;
    String serverVersion;
    String adminUser;
    String adminPasswd;

    public DbController(Path serverDir, int serverPort, byte status, ArrayList<String> databaseList, String serverVersion) {
        this.serverDir = getServerDir();
        this.serverPort = getServerPort();
        this.status = getStatus();
        this.databaseList = getDatabaseList();
        this.serverVersion = getServerVersion();
        this.adminUser = getAdminUser();
        this.adminPasswd = getAdminPasswd();
    }

    public Path getServerDir() {
        return serverDir;
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
