package app.controllers;

import app.core.FileBasedConfigurationHandler;
import app.models.Errorlevels;
import app.models.OS;
import app.models.ReturnValues;
import app.models.SearchType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DbController {
    private static LogController logController = LogController.getInstance();

    private static DbController ourInstance = new DbController();
    // Root database directory e.g. /opt/pgsql
    Path serverDir;
    String serverPort;
    // Database status
    boolean status;
    ArrayList<String> databaseList;
    Path serverConfFilename;
    String serverVersion;
    String adminUser;
    String adminPasswd;
    private DbController() {
        if (getStatus()) {
            this.serverConfFilename = getServerConfFilename();
            this.serverDir = getServerDir();
            this.serverPort = getServerPort();
            this.status = true;
            this.databaseList = getDatabaseList();
            this.serverVersion = getServerVersion();
            this.adminUser = getAdminUser();
            this.adminPasswd = getAdminPasswd();
        } else {
            logController.log.severe("Postgres server not running unable to make backups. Hint run with -b command line option");
            System.exit(Errorlevels.E8.getErrorLevel());
        }
    }

    public static DbController getInstance() {
        return ourInstance;
    }

    public Path getServerDir() {
        if (serverDir != null)
            return serverDir;
        else {
            serverDir = getServerConfFilename().getParent().getParent();
            return serverDir;
        }
    }

    public Path getServerBin() {
        return Paths.get(getServerDir().toString(), "bin");
    }

    public String getServerPort() {
        // todo getServerPort other database servers
        try {
            FileBasedConfigurationHandler fbch = new FileBasedConfigurationHandler(getServerConfFilename().toString());
            // todo fix port value returns full line with comments
            if (fbch.isKeyPresent("port")) {
                return fbch.getKeyValue("port");
            } else
                return "5432";
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Path getServerConfFilename() {
        //todo getServerConf other database servers
        if (serverConfFilename != null) {
            return serverConfFilename;
        }
        logController.log.info("Looking for directory pattern data/postgresql.conf");
        FileFinderControllerStr postgresConf = FileFinderControllerStr.doit("/", "data/postgresql.conf", SearchType.Files);

        if (postgresConf.getNumMatches() == 0) {
            logController.log.severe("Unable to find out postgres conf file!!!!");
            System.exit(Errorlevels.E8.getErrorLevel());
        }
        ArrayList<Path> results = postgresConf.getResults();
        if (postgresConf.getNumMatches() > 1) {
            logController.log.warning("Multiple postgres conf file detected. Guessing correct one");
            for (Path path : results) {
                logController.log.config(String.format("Path: %s. depth=%d", path.toString(), path.getNameCount()));
                if ((path.toString().contains("opt")) && (path.getNameCount() < 5)) {
                    logController.log.config(String.format("Guessed dir: %s ", path.toString()));
                    this.serverConfFilename = path;
                    return serverConfFilename;
                }
            }
            logController.log.warning("Unable to guess returning the first one.");
        } else {
            logController.log.config(String.format("Exact match %s", results.get(0).toString()));
            this.serverConfFilename = results.get(0);
            return serverConfFilename;
        }
        return null;
    }


    public boolean getStatus() {
        ServiceController serviceController = ServiceController.getInstance();
        return serviceController.serviceAlive("postgres");
    }

    public ArrayList<String> getDatabaseList() {
        String[] command = new String[]{getServerBin().toString() + File.separator + "psql", "-U", "postgres", "-c", "SELECT datname AS result FROM pg_database;"};
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
        String[] command = new String[]{getServerBin().toString() + File.separator + "psql", "-U", "postgres", "-c", "SELECT version();"};
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

    /* Hacky way to pass password to postgres environment.
     * Creating .pgpass file in home user directory
     * NOTE This overwrites existent pgpass file */
    public File setCredentials(String dbuser, String dbpass) {
        ServiceController serviceController = ServiceController.getInstance();
        File pgpass;
        if (serviceController.getOs() == OS.WINDOWS) {
            pgpass = FileUtils.getFile(serviceController.getAppdata(), "postgresql", "pgpass.conf");
            File pgpassdir = FileUtils.getFile(pgpass.getParent());
            if (!pgpassdir.exists()) {
                try {
                    FileUtils.forceMkdir(pgpassdir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            pgpass = FileUtils.getFile(serviceController.getUserHome(), ".pgpass");
        }

        try {
            PrintWriter writer = new PrintWriter(pgpass);
            logController.log.config("Rellenando pgpass");
            writer.println("*:5432:*:" + dbuser + ":" + dbpass);
            writer.close();
            if (serviceController.getOs().equals(OS.LINUX)) setPermissions(pgpass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logController.log.config(String.format("Credentials file %s created", pgpass.toString()));
        return pgpass;
    }

    private void setPermissions(File pgpass) {
        logController.log.config(String.format("Changing permissions to 0600: %s", pgpass.toString()));
        ServiceController.getInstance().runCommand(new String[]{"chmod", "0600", pgpass.toString()});
    }

    /* Cleanup credentials file after execution  */
    public void unsetCredentials(File pgpass) {
        FileUtils.deleteQuietly(pgpass);
        logController.log.config(String.format("Credentials file %s deleted", pgpass.toString()));
    }
}