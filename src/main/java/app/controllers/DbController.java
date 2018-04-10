package app.controllers;

import app.core.FileBasedConfigurationHandler;
import app.models.ReturnValues;
import app.models.SearchType;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DbController
{
    private static LogController logController = LogController.getInstance();

    private static DbController ourInstance;

    static {
        try {
            ourInstance = new DbController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Root database directory e.g. /opt/pgsql
    Path serverDir;
    String serverPort;
    // Database status
    boolean status;
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

    public static DbController getInstance() {
        return ourInstance;
    }

    public Path getServerDir() throws IOException {
        Path serverConf = Paths.get(getServerConfFilename());
        return (serverConf.getParent().getParent());
        /*
        FileFinderController rootDB = FileFinderController.doit("/", "pgsql", 2);
        if (rootDB.getNumMatches() == 0) {
            throw new IOException("No Postgres installation detected");
        }
        return rootDB.results.get(0);*/
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

    public String getServerConfFilename() throws IOException {
        //todo getServerConf other database servers
        logController.log.info("Looking for directory pattern data/postgresql.conf");
        FileFinderControllerStr postgresConf = FileFinderControllerStr.doit("/", "data/postgresql.conf", SearchType.Files);

        if (postgresConf.getNumMatches() == 0) {
            throw new IOException("Unable to find out postgres conf file!!!!");
        } else {
            ArrayList<Path> results = postgresConf.getResults();
            if (postgresConf.getNumMatches() > 1) {
                logController.log.warning("Multiple postgres conf file detected. Guessing correct one");
                for (Path path : results) {
                    logController.log.config(String.format("Path: %s. depth=%d", path.toString(), path.getNameCount()));
                    if ((path.toString().contains("opt")) && (path.getNameCount() < 5)) {
                        logController.log.info(String.format("Guessed dir: %s ", path.toString()));
                        return path.toString();
                    }
                }
                logController.log.warning("Unable to guess returning the first one.");
            } else {
                logController.log.config(String.format("Exact match %s", results.get(0).toString()));
                return results.get(0).toString();
            }

        }
        throw new IOException("Unable to find out postgres conf file!!!!");
    }

    public boolean getStatus() {
        ServiceController serviceController = ServiceController.getInstance();
        return serviceController.serviceAlive("postgres");
    }

    public ArrayList<String> getDatabaseList() {
        String[] command = new String[]{"psql", "-U", "postgres", "-c", "SELECT datname AS result FROM pg_database;"};
        ArrayList<String> databaseList = new ArrayList<>();
        ServiceController serviceController = ServiceController.getInstance();
        ReturnValues returnValues = serviceController.runCommand(command);
        if (returnValues.t != "0") {
            ArrayList<String> listHandler = ((ArrayList<String>) returnValues.u);
            databaseList.addAll(listHandler.subList(2, listHandler.size() - 2));
            return databaseList;
        }
        return null;
    }

    public String getServerVersion() {
        String[] command = new String[]{"psql", "-U", "postgres", "-c", "SELECT version();"};
        ServiceController serviceController = ServiceController.getInstance();
        ReturnValues returnValues = serviceController.runCommand(command);
        if (returnValues.t != "0") {
            return ((ArrayList<String>) returnValues.u)
                    .subList(2, ((ArrayList<String>) returnValues.u).size() - 2)
                    .toString();
        }
        return null;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPasswd() {
        return adminPasswd;
    }


    @Override
    public String toString() {
        return "DbController{" +
                "serverDir=" + serverDir +
                ", serverPort='" + serverPort + '\'' +
                ", status=" + status +
                ", databaseList=" + databaseList +
                ", serverConfFilename='" + serverConfFilename + '\'' +
                ", serverVersion='" + serverVersion + '\'' +
                ", adminUser='" + adminUser + '\'' +
                ", adminPasswd='" + adminPasswd +
                '}';
    }
}