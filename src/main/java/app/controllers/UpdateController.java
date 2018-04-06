package app.controllers;

import app.core.ZipHandler;
import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.ReturnValues;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class UpdateController
{
    final static int dbThreshold = 3;
    final static long defaultTimeout = 20000;

    // instance BackupsController
    private BackupsController backupsController = BackupsController.getInstance();
    // instance ServiceController
    private ServiceController serviceController = ServiceController.getInstance();

    // this appupdater
    private ArcadiaAppData appData;
    // instance ArcadiaController
    // access installed apps details
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();
    private CommandLine commandLine;

    private File arcadiaUpdatesRepository;
    private File latestUpdatesVersionDir;
    private File installedAppDir;

    public UpdateController(CommandLine commandLine, ArcadiaAppData arcadiaAppData) {
        this.commandLine = commandLine;
        this.appData = appData;
    }

    /*
     *
     Main method
     *
     */
    public Boolean updateApp(ArcadiaAppData app) throws RuntimeException {
        sysinit(app);
        if (!this.commandLine.hasOption("n")) {
            checkRabbitmq();
            checkZookeeper();
        }
        stopAppServer(app.getApp());
        if (!this.commandLine.hasOption("b"))
            backupDatabase(app.getApp());
        if (!this.commandLine.hasOption("B"))
            backoutApp();
        // Now place new version
        updateArcadiaResources();
        updateLogBack();
        updateSharedlib();
        updateWars();
        updateCustom();
        startAppServer(app.getApp());
        // Check schema_version all ok
        return true;
    }

    private void backupDatabase(ArcadiaAppData app) throws RuntimeException {
        checkDbServer();
        Long lastBackupSize = backupsController.getLatestBackupSize(app.getApp());
        Long databaseBackupDirSize = backupDatabase(app.getApp());
        if (currentBackupSizeMismatch(lastBackupSize, databaseBackupDirSize) && !this.commandLine.hasOption("b"))
            throw new RuntimeException("ERROR: Backup size mismatch!!!");
        backupArcadiaResources();
    }

    private boolean currentBackupSizeMismatch(Long lastBackupSize, Long databaseBackupDirSize) {
        return backupsController.differencePercentage(lastBackupSize, databaseBackupDirSize) > dbThreshold;
    }

    private void sysinit(ArcadiaAppData arcadiaApp) throws RuntimeException {
        // Initialize general Directories variables


        this.appData = arcadiaApp;


        /* DEPRECATED - FUNCIONALITY REFACTORIZED TO ARCADIACONTROLLER// latest update available
        //
        Path eachApp = Paths.get(arcadiaController.getArcadiaUpdatesRepository().toString(), arcadiaApp.getApp().getShortName());
        SystemCommons systemCommons = new SystemCommons();
        Version updateVersion = null;
        File[] subdirs = eachApp.toFile().listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        if (subdirs.length > 0) {
            latestUpdatesVersionDir = systemCommons.sortDirectoriesByVersion(subdirs)[0];
            System.out.printf("ArcadiaUpdater.updateApp latestUpdatesVersionDir:%s\n", latestUpdatesVersionDir);
            updateVersion = new Version(systemCommons.normalizeVersion(FilenameUtils.getName(latestUpdatesVersionDir.getAbsolutePath())));
        }

        if (arcadiaApp.getVersion() == null) {
            throw new RuntimeException(
                    String.format("ERROR: Unable to update %s. Current version unknown. Maybe server is stopped\n",
                            arcadiaApp.getApp().getShortName()));

        }
        if (new Version(systemCommons.normalizeVersion(arcadiaApp.getVersion())).compareTo(updateVersion) > 0) {
            throw new RuntimeException(arcadiaApp.getApp().getLongName() + " already updated.");
        }
        System.out.printf("New version detected %s. Updating in progress...\n", latestUpdatesVersionDir.toString());
        */
        installedAppDir = this.appData.getDirectory();

    }

    private void checkDbServer() throws RuntimeException {
        // Check database service
        if (!serviceController.serviceAlive("postgres")) {
            throw new RuntimeException("Database not started");
        } else System.out.println("OK: Database Server available");
    }

    private void updateCustom() throws RuntimeException {
        // Update custom
        File sourceOldCustom = FileUtils.getFile(latestUpdatesVersionDir.toString(), "backout", "custom");
        File sourceNewCustom = FileUtils.getFile(latestUpdatesVersionDir.toString(), "custom");
        File targetCustom = FileUtils.getFile(installedAppDir.toString(), "custom");

        PropertiesUpdaterController puc = new PropertiesUpdaterController(sourceOldCustom, sourceNewCustom, targetCustom);
        try {
            puc.updateCustom();
        } catch (IOException e) {
            throw new RuntimeException("Error updating custom");
        }
    }

    private void updateWars() throws RuntimeException {
        // Copy wars but ArcadiaResources reuse filterWebapps created before
        copyFilteredDir(
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "wars"),
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                new NotFileFilter(new NameFileFilter("ArcadiaResources")));
    }

    private void updateSharedlib() throws RuntimeException {
        // Copy new sharedlib
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "sharedlib"),
                    installedAppDir
            );
        } catch (IOException e) {
            throw new RuntimeException("Error copying sharedlib");
        }
        // Copy jars
        FilenameFilter jarsFilter = new SuffixFileFilter(".jar");
        copyFilteredDir(
                FileUtils.getFile(latestUpdatesVersionDir.toString(), "jars"),
                FileUtils.getFile(installedAppDir.toString(), "sharedlib"),
                jarsFilter);
    }

    private void updateLogBack() throws RuntimeException {
        // Copy new logback
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestUpdatesVersionDir.toString(), "lib"),
                    installedAppDir
            );
        } catch (IOException e) {
            throw new RuntimeException("Error copying logback configuration");
        }
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

    private void updateArcadiaResources() throws RuntimeException {
        // Update resources

        File tempOutputExtractZip = FileUtils.getFile(
                FileUtils.getTempDirectory(), new SystemCommons().getToday());
        File warAr = FileUtils.getFile(latestUpdatesVersionDir.toString(),
                "wars",
                "ArcadiaResources.war");
        try {
            ZipFile zipFile = new ZipFile(warAr);
            zipFile.extractAll(tempOutputExtractZip.toString());
        } catch (ZipException e) {
            throw new RuntimeException("Error extracting ArcadiaResources");
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
            throw new RuntimeException("Error copying new ArcadiaResources");
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
            throw new RuntimeException("Error copying new ArcadiaResources");
        }
    }

    private void backupArcadiaResources() {
        // Backup ArcadiaResources
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
        // todo migrate to zip4j
    }


    private Long backupDatabase(ArcadiaApp app) throws RuntimeException {
        //check valid backups directory
        if (backupsController.getRootBackupsDir() == null)
            throw new RuntimeException("ERROR: invalid database backup directory");
        // Backup database
        File targetBackupDir = FileUtils.getFile(
                backupsController.getRootBackupsDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + new SystemCommons().getToday());
        if (backupsController.databaseBackup(app.getDatabaseName(), targetBackupDir) > 0) {
            throw new RuntimeException("Error while backup'in database");
        }
        return FileUtils.sizeOfDirectory(targetBackupDir);
    }

    private void stopAppServer(ArcadiaApp app) throws RuntimeException {
        // Stop tomcat service
        String service = "tomcat_" + app.getShortName();
        ReturnValues returnValues = serviceController.serviceAction(
                service, "stop");

        // Check tomcat stopped
        try {
            Long timeout = (this.commandLine.hasOption("t")) ? Long.valueOf(this.commandLine.getOptionValue("timeout")) : defaultTimeout;
            SystemCommons.timedServiceStop(timeout, service);
        } catch (InterruptedException e) {
            System.out.println("Timer interrupt");
        }
    }

    private void startAppServer(ArcadiaApp app) throws RuntimeException {
        // Stop tomcat service
        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "start");

        // Check tomcat started-ing
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            System.out.println("OK: Tomcat started");
        } else
            throw new RuntimeException("ERROR: Tomcat not started!!");
    }

    private void checkZookeeper() {
        // Check zookeeper
        if (!serviceController.serviceAlive("zookeeper")) {
            System.out.println("ERROR: Zookeeper not started");
            //throw new RuntimeException("ERROR: Zookeeper not started");
        } else System.out.println("OK: Zookeeper Server available");
    }

    private void checkRabbitmq() {
        // Check rabbitmq
        if (!serviceController.serviceAlive("rabbitmq")) {
            System.out.println("ERROR: Rabbitmq not started");
            //throw new RuntimeException("ERROR: Rabbitmq not started");
        } else System.out.println("OK: RabbitMq Server available");
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
                System.out.printf("Moving %s to %s\n", file.toString(), target.toString());
                if (file.isDirectory())
                    FileUtils.moveDirectoryToDirectory(file, target, true);
                else
                    FileUtils.moveFileToDirectory(file, target, true);
            } catch (IOException e) {
                throw new RuntimeException("ERROR moving " + file + " " + e.getMessage());
            }
        }
    }

    private void copyFilteredDir(File source, File target, FilenameFilter filter) throws RuntimeException {
        Collection<File> filteredDir = new ArrayList<>();
        filteredDir.addAll(Arrays.asList(source.listFiles(filter)));
        for (File file : filteredDir) {
            if (file.isDirectory())
                try {
                    FileUtils.copyDirectoryToDirectory(file, target);
                } catch (IOException e) {
                    throw new RuntimeException("Error copying " + source + " to " + target);
                }
            else
                try {
                    FileUtils.copyFileToDirectory(file, target, true);
                } catch (IOException e) {
                    throw new RuntimeException("Error copying " + source + " to " + target);
                }
        }
    }
}
