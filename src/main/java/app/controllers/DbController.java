package app.controllers;

import org.apache.commons.configuration2.ex.ConfigurationException;

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
    String serverPort;
    // Database status
    byte status;
    ArrayList<String> databaseList;
    String serverConfFilename;
    String serverVersion;
    String adminUser;
    String adminPasswd;

    private DbController() throws IOException {
        this.serverDir = getServerDir();
        this.serverPort = getServerPort();
        this.serverConfFilename = getServerConfFilename();
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


    public String getServerPort() {
        // todo getServerPort other database servers
        try {
            FileBasedConfigurationHandler fbch = new FileBasedConfigurationHandler(this.serverConfFilename);
            if (fbch.isKeyPresent("port")) {
                return fbch.getKeyValue("port");
            } else
                return "5432";
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getServerConfFilename() {
        //todo getServerConf other database servers
        FileFinderController postgresConf = FileFinderController.doit("/", "postgres.conf", 1);
        return postgresConf.getResults().get(0).toString();
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