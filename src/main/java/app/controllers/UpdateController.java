package app.controllers;

import app.core.UpdateException;
import app.core.ZipHandler;
import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.ReturnValues;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class UpdateController {
    final static int DBTHRESHOLD = 1;
    // instance BackupsController
    BackupsController backupsController = BackupsController.getInstance();
    // instance ServiceController
    ServiceController serviceController = ServiceController.getInstance();

    // this appupdater
    private ArcadiaAppData appData;
    // instance ArcadiaController
    // access installed apps details

    private ArcadiaController arcadiaController = ArcadiaController.getInstance();
    private File arcadiaUpdatesRepository;
    private File latestAppUpdatesDirectory;
    private File installedAppDir;

    public UpdateController(ArcadiaAppData appData) {
        this.appData = appData;
    }

    public UpdateController() {
    }

    /*
     *
     Main method
     *
     */
    public Boolean updateApp(ArcadiaAppData app) throws UpdateException {
        sysinit(app);
        checkDbServer();
        checkRabbitmq();
        checkZookeeper();
        stopAppServer(app.getApp());
        Long lastBackupSize = backupsController.getLatestBackupSize(app.getApp());
        Long databaseBackupDirSize = backupDatabase(app.getApp());
        if (currentBackupSizeMismatch(lastBackupSize, databaseBackupDirSize))
            throw new UpdateException("ERROR: Backup size mismatch!!!");
        backupArcadiaResources();
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

    private boolean currentBackupSizeMismatch(Long lastBackupSize, Long databaseBackupDirSize) {
        return backupsController.differencePercentage(lastBackupSize, databaseBackupDirSize) > DBTHRESHOLD;
    }

    private void sysinit(ArcadiaAppData arcadiaApp) throws UpdateException {
        // Initialize general Directories variables
        //check valid backups directory
        if (backupsController.getRootBackupsDir() == null)
            throw new UpdateException("ERROR: invalid database backup directory");

        this.appData = arcadiaApp;
        arcadiaUpdatesRepository = arcadiaController.getArcadiaUpdatesRepository().toFile();
        System.out.printf("ArcadiaUpdater.updateApp updatesdir:%s\n", arcadiaUpdatesRepository);

        // latest update available
        //
        Path eachApp = Paths.get(arcadiaController.getArcadiaUpdatesRepository().toString(), arcadiaApp.getApp().getShortName());
        File[] subdirs = eachApp.toFile().listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        if (subdirs.length > 0) {
            latestAppUpdatesDirectory = new FileSystemCommons().sortDirectoriesByName(subdirs)[0];
            System.out.printf("ArcadiaUpdater.updateApp latestupdDir:%s\n", latestAppUpdatesDirectory.toString());
        }
        installedAppDir = this.appData.getInstalledDir();
    }

    private void checkDbServer() throws UpdateException {
        // Check database service
        if (!serviceController.serviceAlive("postgres")) {
            throw new UpdateException("Database not started");
        } else System.out.println("OK: Database Server available");
    }

    private void updateCustom() throws UpdateException {
        // Update custom
        File sourceOldCustom = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout", "custom");
        File sourceNewCustom = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "custom");
        File targetCustom = FileUtils.getFile(installedAppDir.toString(), "custom");

        PropertiesUpdaterController puc = new PropertiesUpdaterController(sourceOldCustom, sourceNewCustom, targetCustom);
        try {
            puc.updateCustom();
        } catch (IOException e) {
            throw new UpdateException("Error updating custom");
        }
    }

    private void updateWars() throws UpdateException {
        // Copy wars but ArcadiaResources reuse filterWebapps created before
        copyFilteredDir(
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "wars"),
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                new NotFileFilter(new NameFileFilter("ArcadiaResources")));
    }

    private void updateSharedlib() throws UpdateException {
        // Copy new sharedlib
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestAppUpdatesDirectory.toString(), "sharedlib"),
                    installedAppDir
            );
        } catch (IOException e) {
            throw new UpdateException("Error copying sharedlib");
        }
        // Copy jars
        FilenameFilter jarsFilter = new SuffixFileFilter(".jar");
        copyFilteredDir(
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "jars"),
                FileUtils.getFile(installedAppDir.toString(), "sharedlib"),
                jarsFilter);
    }

    private void updateLogBack() throws UpdateException {
        // Copy new logback
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestAppUpdatesDirectory.toString(), "lib"),
                    installedAppDir
            );
        } catch (IOException e) {
            throw new UpdateException("Error copying logback configuration");
        }
    }

    private void backoutApp() throws UpdateException {
        // Move sharedlib to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir, "sharedlib"),
                FileUtils.getFile(latestAppUpdatesDirectory, "backout"),
                TrueFileFilter.TRUE);

        // Move logback-common.xml to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "lib", "logback-common.xml"),
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout"), TrueFileFilter.TRUE);

        // Moving old wars and deployed dirs
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout", "wars"),
                new NotFileFilter(new NameFileFilter("ArcadiaResources")));

        // Move custom to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "custom"),
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout"),
                TrueFileFilter.TRUE);

        // Move commons WEB-INF to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources"),
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout"),
                new OrFileFilter(
                        new NameFileFilter("commons"),
                        new NameFileFilter("WEB-INF")));

        // Move logs to backout
        moveFilteredDir(
                FileUtils.getFile(installedAppDir.toString(), "logs"),
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout"),
                TrueFileFilter.TRUE);

        // Create logs directory
        try {
            FileUtils.forceMkdir(FileUtils.getFile(installedAppDir.toString(), "logs"));
        } catch (IOException e) {
            throw new UpdateException("Error creating logs directory");
        }

        // Clean tomcat cache
        try {
            FileUtils.cleanDirectory(
                    FileUtils.getFile(installedAppDir.toString(), "work"));
        } catch (IOException e) {
            throw new UpdateException("Error cleaning Tomcat cache");
        }
    }

    private void updateArcadiaResources() throws UpdateException {
        // Update resources
        File tempOutputExtractZip = FileUtils.getFile(
                FileUtils.getTempDirectory(), backupsController.getToday());
        File warAr = FileUtils.getFile(latestAppUpdatesDirectory.toString(),
                "wars",
                "ArcadiaResources.war");
        try {
            ZipFile zipFile = new ZipFile(warAr);
            zipFile.extractAll(tempOutputExtractZip.toString());
        } catch (ZipException e) {
            throw new UpdateException("Error extracting ArcadiaResources");
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
            throw new UpdateException("Error copying new ArcadiaResources");
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
            throw new UpdateException("Error copying new ArcadiaResources");
        }
    }

    private void backupArcadiaResources() {
        // Backup ArcadiaResources
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
    }


    private Long backupDatabase(ArcadiaApp app) throws UpdateException {
        // Backup database
        File targetBackupDir = FileUtils.getFile(
                backupsController.getRootBackupsDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + backupsController.getToday());
        if (backupsController.databaseBackup(app.getDatabaseName(), targetBackupDir) > 0) {
            throw new UpdateException("Error while backup'in database");
        }
        return FileUtils.sizeOfDirectory(targetBackupDir);
    }

    private void stopAppServer(ArcadiaApp app) throws UpdateException {
        // Stop tomcat service
        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "stop");

        // Check tomcat stopped
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            throw new UpdateException("Tomcat not stopped!!");
        } else System.out.println("OK: Tomcat is stopped");
    }

    private void startAppServer(ArcadiaApp app) throws UpdateException {
        // Stop tomcat service
        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "start");

        // Check tomcat started-ing
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            System.out.println("OK: Tomcat started");
        } else
            throw new UpdateException("ERROR: Tomcat not started!!");
    }

    private void checkZookeeper() {
        // Check zookeeper
        if (!serviceController.serviceAlive("zookeeper")) {
            System.out.println("ERROR: Zookeeper not started");
            //throw new UpdateException("ERROR: Zookeeper not started");
        } else System.out.println("OK: Zookeeper Server available");
    }

    private void checkRabbitmq() {
        // Check rabbitmq
        if (!serviceController.serviceAlive("rabbitmq")) {
            System.out.println("ERROR: Rabbitmq not started");
            //throw new UpdateException("ERROR: Rabbitmq not started");
        } else System.out.println("OK: RabbitMq Server available");
    }

    private void moveFilteredDir(File source, File target, FilenameFilter filter) throws UpdateException {
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
                throw new UpdateException("ERROR moving " + file + " " + e.getMessage());
            }
        }
    }

    private void copyFilteredDir(File source, File target, FilenameFilter filter) throws UpdateException {
        Collection<File> filteredDir = new ArrayList<>();
        filteredDir.addAll(Arrays.asList(source.listFiles(filter)));
        for (File file : filteredDir) {
            if (file.isDirectory())
                try {
                    FileUtils.copyDirectoryToDirectory(file, target);
                } catch (IOException e) {
                    throw new UpdateException("Error copying " + source + " to " + target);
                }
            else
                try {
                    FileUtils.copyFileToDirectory(file, target, true);
                } catch (IOException e) {
                    throw new UpdateException("Error copying " + source + " to " + target);
                }
        }
    }


    // Input Array of directories
    // returns first sorted
    public File getLatestUpdate(File[] updatesDir) {
        new FileSystemCommons().sortDirectoriesByDate(updatesDir);
        return updatesDir[0];
    }
}
