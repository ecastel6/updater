package app.controllers;

import app.models.ReturnValues;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DbController {
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

    public Path getServerDir() throws IOException {
        Path serverConf = Paths.get(getServerConfFilename());
        System.out.println(serverConf);
        System.out.println(serverConf.getParent().getParent());
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
        FileFinderController postgresConf = FileFinderController.doit("/", "postgresql.conf", 1);

        if (postgresConf.getNumMatches() == 0) {
            throw new IOException("Unable to find out postgres conf file!!!!");
        } else {
            ArrayList<Path> results = postgresConf.getResults();
            if (postgresConf.getNumMatches() > 1) {
                //System.out.println("Multiple postgres conf file detected. Guessing correct one");
                for (Path path : results) {
                    //System.out.printf("Path: %s. depth=%d\n", path.toString(), path.getNameCount());
                    if ((path.toString().contains("opt")) && (path.getNameCount() < 5)) {
                        // System.out.printf("Guessed dir: %s ", path.toString());
                        return path.toString();
                    }
                }
                //System.out.println("Unable to guess returning the first one.");
            } else {
                if ((results.get(0).toString().contains("opt")) && (results.get(0).getNameCount() < 5)) {
                    return results.get(0).toString();
                }
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

    public int databaseBackup(String database, Path targetFolder) {
        String[] command = new String[]{
                "pg_dump",
                "-U",
                "postgres",
                "-Fd",
                "-b",
                "-f",
                targetFolder.toString(),
                database
        };
        ServiceController serviceController = ServiceController.getInstance();
        ReturnValues returnValues = serviceController.runCommand(command);
        return (int) returnValues.t;
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