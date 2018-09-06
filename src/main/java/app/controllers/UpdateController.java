package app.controllers;

import app.core.ZipHandler;
import app.models.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class UpdateController {
    final static int dbThreshold = 3;
    final static long defaultTimeout = 20000;

    //Instance LogController
    private static LogController logController = LogController.getInstance();
    // instance BackupsController
    private BackupsController backupsController = BackupsController.getInstance();
    // instance ServiceController
    private ServiceController serviceController = ServiceController.getInstance();
    // instance ArcadiaController
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();
    // values from ArcadiaController
    private CommandLine commandLine;
    private ArcadiaAppData installedAppData;
    // general variables
    private File latestUpdatesVersionDir;
    private File installedAppDir;
    private String app;

    public UpdateController(String appName) {
        this.commandLine = arcadiaController.getCommandLine();
        this.app = appName;
        this.installedAppData = arcadiaController.getInstalledApps().get(appName);
        this.installedAppDir = installedAppData.getDirectory();
        this.latestUpdatesVersionDir = arcadiaController.getAvailableUpdates().get(appName).getDirectory();
    }

    public UpdateController() {
    }

    public void setLatestUpdatesVersionDir(File latestUpdatesVersionDir) {
        this.latestUpdatesVersionDir = latestUpdatesVersionDir;
    }

    public void setInstalledAppDir(File installedAppDir) {
        this.installedAppDir = installedAppDir;
    }

    /*
     Main method
     */
    public Boolean updateApp() throws RuntimeException {
        // store completed operations jic rollback
        //Stack stack = new Stack();

        if (!this.commandLine.hasOption("n")) {
            checkRabbitmq();
            checkZookeeper();
        }
        stopAppServer(installedAppData.getApp());
        if (!this.commandLine.hasOption("b"))
            preupdateBackup(installedAppData.getApp());
        if (this.commandLine.hasOption("B")) {
            try {
                FileUtils.cleanDirectory(FileUtils.getFile(latestUpdatesVersionDir, "backout"));
            } catch (IOException e) {
                logController.log.severe(String.format("Unable to cleanout backout. %s", e.getMessage()));
                throw new RuntimeException("Unable to cleanout backout, please check permissions");
            }
        }
        backoutApp();
        // Now place new version
        if (!updateArcadiaResources()) {
            logController.log.severe("Couldn't update ArcadiaResources.");
            rollbackApp();
            return false;
        }

        if (!updateLogBack()) {
            logController.log.severe("Couldn't update logback.");
            rollbackApp();
            return false;
        }

        if (!updateSharedlib()) {
            logController.log.severe("Couldn't update sharedlib.");
            rollbackApp();
            return false;
        }

        if (!updateWars()) {
            logController.log.severe("Couldn't update Wars");
            rollbackApp();
            return false;
        }
        if (!updateCustom()) {
            logController.log.severe("Couldn't update custom");
            rollbackApp();
            return false;
        }

        logController.log.warning(String.format("Aplication %s updated to %s version",
                installedAppData.getApp().getLongName(),
                arcadiaController.getAvailableUpdates().get(app).getVersion()));
        if (this.commandLine.hasOption("r")) {
            reinstallServices(installedAppData);
        }
        startAppServer(installedAppData.getApp());
        // Check schema_version all ok
        return true;
    }

    private void rollbackApp() {
        if (!rollbackWars()) {
            logController.log.severe("Could not rollback wars");
            System.exit(Errorlevels.E9.getErrorLevel());
        }
        if (!rollbackArcadiaResources()) {
            logController.log.severe("Could not rollback ArcadiaResources");
            System.exit(Errorlevels.E9.getErrorLevel());
        }
        if (!rollbackCustom()) {
            logController.log.severe("Could not rollback Custom");
            System.exit(Errorlevels.E9.getErrorLevel());
        }
        if (!rollbackLogBack()) {
            logController.log.severe("Could not rollback logback");
            System.exit(Errorlevels.E9.getErrorLevel());
        }
        if (!rollbackSharedlib()) {
            logController.log.severe("Could not rollback sharedlib");
            System.exit(Errorlevels.E9.getErrorLevel());
        }
        // Cleanup backout directory content to avoid error when re-applying update
        try {
            FileUtils.cleanDirectory(FileUtils.getFile(latestUpdatesVersionDir, "backout"));
        } catch (IOException e) {
            logController.log.config("Couldn't' delete content of backout directory");
        }
    }


    /*public void rollbackApplication(Stack stack) {
     *//* No necesario al hacerse el backout se produzca cuando sea el error hay que
     * restablecer la aplicación entera *//*
        String operation;
        Class updateClass = this.getClass();
        logController.log.config(updateClass.toString());
        while (!stack.empty()) {
            operation = (String) stack.pop();
            logController.log.info(String.format("Stack pops: %s", operation));

            try {
                Method method = updateClass.getMethod(operation);
                logController.log.config(String.format("Calling method %s\n",method.toString()));
                method.invoke(this);
            } catch (SecurityException e) {
                logController.log.severe("Security exception");
            } catch (NoSuchMethodException e) {
                logController.log.severe(String.format("Unable to found method %s", operation));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();

            }
        }
    }*/

    public void reinstallServices(ArcadiaAppData appData) {
        String serviceName = String.format("tomcat_%s", appData.getApp().getShortName());
        File sourceServiceScript, targetServiceScript;
        switch (serviceController.os) {
            case WINDOWS:
                if (serviceController.serviceAlive(serviceName)) {
                    logController.log.severe(String.format("Could not reinstall service %s while its running", serviceName));
                    throw new RuntimeException(String.format("Could not reinstall service %s while its running", serviceName));
                }
                String drive;
                try {
                    sourceServiceScript = FileUtils.getFile(arcadiaController.getArcadiaUpdatesRepository().toString(), "base/windows/" + serviceName + ".bat");
                    //sourceServiceScript = FileUtils.getFile("d:/opt/arcadiaVersions/base/windows/" + serviceName + ".bat");
                    targetServiceScript = FileUtils.getFile(appData.getDirectory(), "bin", serviceName + ".bat");
                    drive = FilenameUtils.getPrefix(targetServiceScript.toString().substring(0, 2));
                    FileUtils.copyFile(sourceServiceScript, targetServiceScript, true);
                } catch (IOException e) {
                    logController.log.severe(String.format("Unable to copy service %s to destination", serviceName));
                    logController.log.severe(e.getMessage());
                    throw new RuntimeException(String.format("Unable to copy service %s to destination", serviceName));
                }

                ReturnValues retDestroy = serviceController.runCommand(new String[]{"cmd.exe", "/c",
                        drive + " && " + "cd " + targetServiceScript.getParent() + " && "
                                + targetServiceScript.toString(), "remove", serviceName});
                logController.log.config(retDestroy.u.toString());
                ReturnValues retCreate = serviceController.runCommand(new String[]{"cmd.exe", "/c",
                        drive + " && " + "cd " + targetServiceScript.getParent() + " && "
                                + targetServiceScript.toString(), "install", serviceName});
                logController.log.config(retCreate.u.toString());
                if (Integer.parseInt(String.valueOf(retDestroy.t)) > 0 || Integer.parseInt(String.valueOf(retCreate.t)) > 0) {
                    logController.log.severe(String.format("Destroy retValue=%s retMsg=%s", retDestroy.t, retDestroy.u));
                    logController.log.severe(String.format("Create retValue=%s retMsg=%s", retCreate.t, retCreate.u));
                    //throw new RuntimeException("Got in trouble recreating services");
                } else {
                    logController.log.config("Configuring service to auto start");
                    ReturnValues retConfigService = serviceController.runCommand(new String[]{"cmd.exe", "/c",
                            "sc", "config", serviceName, "start=", "auto"});
                    if (Integer.parseInt(String.valueOf(retConfigService.t)) > 0)
                        logController.log.severe(String.format("Config service retValue=%s retMsg=%s", retConfigService.t, retConfigService.u));
                }
                break;
            case LINUX:
                sourceServiceScript = FileUtils.getFile(arcadiaController.getArcadiaUpdatesRepository().toString(), "base/linux/" + serviceName);
                targetServiceScript = FileUtils.getFile("/etc/init.d/");
                drive = FilenameUtils.getPrefix(targetServiceScript.toString().substring(0, 2));
                try {
                    FileUtils.copyFileToDirectory(sourceServiceScript, targetServiceScript, true);
                } catch (IOException e) {
                    logController.log.severe(String.format("Unable to copy service %s to destination", serviceName));
                    logController.log.severe(e.getMessage());
                    throw new RuntimeException(String.format("Unable to copy service %s to destination", serviceName));
                }
                break;
        }
        logController.log.info(String.format("Service %s reinstalled", "tomcat_" + appData.getApp().getShortName()));
    }

    private void preupdateBackup(ArcadiaApp app) throws RuntimeException {
        if ((commandLine.hasOption("x")) || serviceController.serviceAlive("postgres") == false)
            tomcatConfigBackup(app);
        else {
            parameterizedBackup(app);
        }
        backupArcadiaResources();
    }

    /* data to fullfill backup taken from command line or default values, single database backup */
    public void parameterizedBackup(ArcadiaApp app) {
        logController.log.config("ParameterizedBackup. Getting default or command line data");
        //check valid backups directory
        File targetBackupDir = FileUtils.getFile(getValidRootBackupDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + new SystemCommons().getToday());

        logController.log.warning(String.format("Backup'in database %s to %s directory", app.getDatabaseName(), targetBackupDir));
        String dbhost = "localhost";
        String dbport = "5432";
        String dbuser = "postgres";
        //TODO cleartext password disclosure possible
        String dbpass = "postavalon";


        if (this.commandLine.hasOption("h")) dbhost = this.commandLine.getOptionValue("host");
        if (this.commandLine.hasOption("p")) dbport = this.commandLine.getOptionValue("port");
        if (this.commandLine.hasOption("u")) dbuser = this.commandLine.getOptionValue("user");
        if (this.commandLine.hasOption("w")) dbpass = this.commandLine.getOptionValue("password");

        logController.log.config(String.format("Host=%s Port=%s User=%s Password=%s",
                dbhost, dbport, dbuser, dbpass));

        if (backupsController.databaseBackup(
                app.getDatabaseName(),
                targetBackupDir,
                dbhost,
                dbport,
                dbuser,
                dbpass) > 0) {
            throw new RuntimeException("Error while backup'in database");
        }
        Long databaseBackupDirSize = FileUtils.sizeOfDirectory(targetBackupDir);
        if (backupsController.getRootBackupsDir() != null) {
            Long lastBackupSize = backupsController.getLatestBackupSize(app);
            if (currentBackupSizeMismatch(lastBackupSize, databaseBackupDirSize))
                logController.log.warning(String.format("Database backup size %s doesn't match with latest one", databaseBackupDirSize));
        }
    }

    private File getValidRootBackupDir() {
        File tempRootDir;
        if (backupsController.noRootBackupDirectory)
            return FileUtils.getFile(
                    FileUtils.getUserDirectory(), "upgrade");
        else if (backupsController.getRootBackupsDir() == null) {
            logController.log.config("DB backup directory not found. Using user backup directory");
            return FileUtils.getFile(
                    FileUtils.getUserDirectory(), "upgrade");
        } else return backupsController.getRootBackupsDir();
    }

    /* data to fullfill backup taken from xml tomcat config multiple database backup */
    public void tomcatConfigBackup(ArcadiaApp app) throws RuntimeException {
        logController.log.config(String.format("tomcatConfigbackup of %s. Getting values from tomcat configfile", app.getLongName()));
        Map<String, String[]> arcadiaDbPools = new HashMap<>();

        arcadiaDbPools = arcadiaController.getArcadiaDatabases(
                FileUtils.getFile(this.installedAppDir,
                        "conf", "server.xml"));
        // Now process every database for backup using pool values
        File targetBackupDir;
        File rootBackupDir = getValidRootBackupDir();
        String dbname;
        String dbhost;
        String dbport;
        String dbuser;
        String dbpass;
        for (Map.Entry<String, String[]> entry : arcadiaDbPools.entrySet()) {
            //url=jdbc:postgresql://localhost:5432/arcadia_cbos
            //entry.getValue[0]=username
            //entry.getValue[1]=password
            Map<String, String> urlDecoded = arcadiaController.getDBUrlDecoded(entry.getKey());
            // check for non postgresql database server
            if (!urlDecoded.get("sgbd").equals("postgresql")) {
                logController.log.severe("Unsupported database system");
                System.exit(Errorlevels.E11.getErrorLevel());
            }
            dbname = urlDecoded.get("dbname");
            dbhost = urlDecoded.get("host");
            dbport = urlDecoded.get("port");
            dbuser = entry.getValue()[0];
            dbpass = entry.getValue()[1];

            targetBackupDir = FileUtils.getFile(
                    rootBackupDir,
                    dbname,
                    dbname + "_" + new SystemCommons().getToday());
            logController.log.config(String.format("Backup'in Host=%s Port=%s User=%s Password=%s Database=%s to directory %s",
                    dbhost, dbport, dbuser, dbpass, dbname, targetBackupDir));
            if (backupsController.databaseBackup(
                    dbname,
                    targetBackupDir,
                    dbhost,
                    dbport,
                    dbuser,
                    dbpass) > 0) {
                throw new RuntimeException(String.format("Error while backup'in database %s", dbname));
            }
        }
    }

    private boolean currentBackupSizeMismatch(Long lastBackupSize, Long databaseBackupDirSize) {
        // todo configurable threshold through commandline option
        return backupsController.differencePercentage(lastBackupSize, databaseBackupDirSize) > dbThreshold;
    }

    private void checkDbServer() throws RuntimeException {
        // Check database service
        if (!serviceController.serviceAlive("postgres")) {
            throw new RuntimeException("Database server not available");
        } else logController.log.info("Database Server available");
    }

    private boolean updateCustom() throws RuntimeException {
        // Update custom
        File sourceOldCustom = FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "custom");
        File sourceNewCustom = FileUtils.getFile(latestUpdatesVersionDir.toString(), "custom");
        File targetCustom = FileUtils.getFile(installedAppDir.toString(), "custom");

        PropertiesUpdaterController puc = new PropertiesUpdaterController(sourceOldCustom, sourceNewCustom, targetCustom);
        try {
            puc.updateCustom();
        } catch (IOException e) {
            logController.log.config("Error updating custom");
            return false;
        }
        return true;
    }

    private boolean updateWars() {
        // Copy wars but ArcadiaResources reuse filterWebapps created before

        try {
            copyFilteredDir(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "wars"),
                    FileUtils.getFile(installedAppDir.toString(), "webapps"),
                    new NotFileFilter(new NameFileFilter("ArcadiaResources.war")));
        } catch (RuntimeException e) {
            logController.log.config("Cannot update wars");
            return false;
        }
        return true;
    }

    private boolean updateSharedlib() {
        // Copy new sharedlib
        // Copy jars
        try {
            FilenameFilter jarsFilter = new SuffixFileFilter(".jar");
            copyFilteredDir(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "jars"),
                    FileUtils.getFile(installedAppDir.toString(), "sharedlib"),
                    jarsFilter);

            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "sharedlib"),
                    installedAppDir
            );
        } catch (IOException e) {
            logController.log.config("Cannot update sharedlib");
            return false;
        }
        return true;
    }

    private boolean updateLogBack() {
        // Copy new logback
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "lib"),
                    installedAppDir
            );
        } catch (IOException e) {
            logController.log.config("Error copying logback configuration");
            return false;
        }
        return true;
    }

    private void backoutApp() throws RuntimeException {
        // Move sharedlib to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir, "sharedlib"),
                FileUtils.getFile(latestUpdatesVersionDir, "backout"),
                TrueFileFilter.TRUE);

        // Move logback-common.xml to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "lib", "logback-common.xml"),
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout"), TrueFileFilter.TRUE);

        // Moving old wars and deployed dirs
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "wars"),
                new NotFileFilter(new NameFileFilter("ArcadiaResources")));

        // Move custom to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "custom"),
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout"),
                TrueFileFilter.TRUE);

        // Move commons WEB-INF to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources"),
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout"),
                new OrFileFilter(
                        new NameFileFilter("commons"),
                        new NameFileFilter("WEB-INF")));

        // Move logs to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "logs"),
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout"),
                TrueFileFilter.TRUE);

        // Create logs directory
        try {
            FileUtils.forceMkdir(FileUtils.getFile(installedAppDir.toString(), "logs"));
        } catch (IOException e) {
            throw new RuntimeException("Error creating logs directory");
        }

        // Clean tomcat cache
        try {
            FileUtils.cleanDirectory(
                    FileUtils.getFile(installedAppDir.toString(), "work"));
        } catch (IOException e) {
            throw new RuntimeException("Error cleaning Tomcat cache");
        }
    }

    private boolean updateArcadiaResources() throws RuntimeException {
        File tempOutputExtractZip = FileUtils.getFile(
                FileUtils.getTempDirectory(), new SystemCommons().getToday());
        File warAr = FileUtils.getFile(latestUpdatesVersionDir.toString(),
                "wars",
                "ArcadiaResources.war");
        try {
            ZipFile zipFile = new ZipFile(warAr);
            zipFile.extractAll(tempOutputExtractZip.toString());
        } catch (ZipException e) {
            logController.log.config("Error extracting ArcadiaResources");
            return false;
        }

        // Copy commons
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(
                            tempOutputExtractZip.toString(),
                            "commons"),
                    FileUtils.getFile(
                            installedAppDir.toString(),
                            "webapps",
                            "ArcadiaResources")
            );
        } catch (IOException e) {
            logController.log.config("Cannot copy commons to ArcadiaResources");
            return false;
        }
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(
                            tempOutputExtractZip.toString(),
                            "WEB-INF"),
                    FileUtils.getFile(
                            installedAppDir.toString(),
                            "webapps",
                            "ArcadiaResources")
            );
        } catch (IOException e) {
            logController.log.config("Cannot copy WEBINF to ArcadiaResources");
            return false;
        }
        return true;
    }


    public boolean rollbackArcadiaResources() {
        logController.log.config("Rolling Back ArcadiaResources");
        try {
            FileUtils.deleteDirectory(FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources", "WEB-INF"));
            FileUtils.deleteDirectory(FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources", "commons"));
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "WEB-INF"),
                    FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources"));
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "commons"),
                    FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources"));
        } catch (IOException e) {
            return false;
        }
        logController.log.config("ArcadiaResources rolled back");
        return true;
    }

    public boolean rollbackLogBack() {
        logController.log.config("Rolling Back LogBack");
        try {
            FileUtils.copyFileToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "logback-common.xml"),
                    FileUtils.getFile(installedAppDir.toString(), "lib"));
        } catch (IOException e) {
            return false;
        }
        logController.log.config("Logback-common.xml rolled back");
        return true;
    }

    public boolean rollbackWars() {
        logController.log.config("RollingBack Wars");
        if (!deleteFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                new NotFileFilter(new NameFileFilter("ArcadiaResources")))) {
            logController.log.config("Cannot delete application wars");
            return false;
        }
        logController.log.config("webapps cleaned up");
        if (!copyFilteredDir(
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "wars"),
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                TrueFileFilter.TRUE)) {
            logController.log.config("Unable to rollback wars. Error copying wars");
            return false;
        }
        logController.log.config("Wars rolled back");
        return true;
    }

    public boolean rollbackSharedlib() {
        logController.log.config("Rolling Back Sharedlib");
        try {
            FileUtils.deleteDirectory(FileUtils.getFile(installedAppDir.toString(), "sharedlib"));
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "sharedlib"),
                    FileUtils.getFile(installedAppDir.toString()));
        } catch (IOException e) {
            return false;
        }
        logController.log.config("Sharedlib rolled back");
        return true;
    }

    public boolean rollbackCustom() {
        logController.log.config("Rolling Back Custom");
        try {
            FileUtils.deleteDirectory(FileUtils.getFile(installedAppDir.toString(), "custom"));
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "custom"),
                    FileUtils.getFile(installedAppDir.toString()));
        } catch (IOException e) {
            return false;
        }
        logController.log.config("Custom rolled back");
        return true;
    }

    private void backupArcadiaResources() {
        // Backup ArcadiaResources
        String targetDir = FileUtils.getFile(getValidRootBackupDir(),
                String.format("ArcadiaResources.%s_%s.zip", installedAppData.getApp().getShortName(), new SystemCommons().getToday())).toString();
        logController.log.config(String.format("Zipping ArcadiaResources from %s to targetdir: %s", installedAppDir, targetDir));
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
        // TODO migrate to zip4j
        zipHandler.zip(
                FileUtils.getFile(installedAppDir, "webapps", "ArcadiaResources").toString(),
                targetDir,
                CompresionLevel.UNCOMPRESSED);
    }


    public void stopAppServer(ArcadiaApp app) throws RuntimeException {
        // Stop tomcat service
        String service = "tomcat_" + app.getShortName();
        ReturnValues returnValues = serviceController.serviceAction(
                service, "stop");

        // Check tomcat stopped
        try {
            Long timeout = (this.commandLine.hasOption("t")) ? Long.valueOf(this.commandLine.getOptionValue("timeout")) : defaultTimeout;
            logController.log.config(String.format("Waiting %s segs for service to stop", timeout / 1000));
            SystemCommons.timedServiceStop(timeout, service);
        } catch (InterruptedException e) {
            logController.log.severe(String.format("Timer interrupt. %s", e.getMessage()));
        }
    }

    private void startAppServer(ArcadiaApp app) throws RuntimeException {
        // Stop tomcat service
        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "start");

        // Check tomcat started-ing
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            logController.log.config("OK: Tomcat started");
        } else
            throw new RuntimeException("ERROR: Tomcat not started!!");
    }

    private void checkZookeeper() {
        // Check zookeeper
        if (!serviceController.serviceAlive("zookeeper")) {
            logController.log.severe("ERROR: Zookeeper not started");
            //throw new RuntimeException("ERROR: Zookeeper not started");
        } else logController.log.config("OK: Zookeeper Server available");
    }

    private void checkRabbitmq() {
        // Check rabbitmq
        if (!serviceController.serviceAlive("rabbitmq")) {
            logController.log.severe("ERROR: Rabbitmq not started");
            //throw new RuntimeException("ERROR: Rabbitmq not started");
        } else logController.log.config("OK: RabbitMq Server available");
    }

    private void moveFilteredDir(File source, File target, FilenameFilter filter) throws RuntimeException {
        Collection<File> filteredDir = new ArrayList<>();
        if (filter.equals(TrueFileFilter.INSTANCE)) {
            if (source.exists())
                filteredDir.add(source);
        } else {
            filteredDir = Arrays.asList(source.listFiles(filter));
            //FileUtils.listFilesAndDirs(source, (IOFileFilter) filter,TrueFileFilter.TRUE);
        }

        for (File file :
                filteredDir) {
            try {
                logController.log.config(String.format("Moving %s to %s", file.toString(), target.toString()));
                if (file.isDirectory())
                    FileUtils.moveDirectoryToDirectory(file, target, true);
                else
                    FileUtils.moveFileToDirectory(file, target, true);
            } catch (IOException e) {
                throw new RuntimeException("ERROR moving " + file + " " + e.getMessage() + ". Hint: try using -B option to empty backout first");
            }
        }
    }


    private boolean copyFilteredDir(File source, File target, FilenameFilter filter) throws RuntimeException {
        Collection<File> filteredDir = new ArrayList<>();
        filteredDir.addAll(Arrays.asList(source.listFiles(filter)));
        for (File file : filteredDir) {
            logController.log.config(String.format("Copying file/dir %s", file.toString()));
            if (file.isDirectory())
                try {
                    FileUtils.copyDirectoryToDirectory(file, target);
                } catch (IOException e) {
                    logController.log.severe(String.format("Error copying " + source + " to " + target));
                    return false;
                }
            else
                try {
                    FileUtils.copyFileToDirectory(file, target, true);
                } catch (IOException e) {
                    logController.log.severe(String.format("Error copying " + source + " to " + target));
                    return false;
                }
        }
        return true;
    }

    private boolean deleteFilteredDir(File target, FilenameFilter filter) throws RuntimeException {
        if (filter.equals(TrueFileFilter.INSTANCE)) {
            try {
                FileUtils.cleanDirectory(target);
            } catch (IOException e) {
                logController.log.severe(String.format("Unable to cleanout dir %s", target.toString()));
                return false;
            }
        } else {
            Collection<File> filteredDir = new ArrayList<>();
            filteredDir = Arrays.asList(target.listFiles(filter));
            for (File file :
                    filteredDir) {
                logController.log.config(String.format("Deleting %s.", file.toString()));
                if (!FileUtils.deleteQuietly(file)) {
                    logController.log.severe(String.format("Unexpected error while deleting %s", file));
                    return false;
                }
            }
        }
        return true;
    }
}
