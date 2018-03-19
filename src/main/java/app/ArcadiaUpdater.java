package app;

import app.controllers.ArcadiaController;
import app.controllers.BackupsController;
import app.controllers.PropertiesUpdaterController;
import app.controllers.ServiceController;
import app.core.UpdateException;
import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.OS;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class ArcadiaUpdater
{

    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();
    private BackupsController backupsController = BackupsController.getInstance();

    public static void main(String[] args) {
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/
        if (args.length == 0) System.out.println("No parameters, auto update in progress...");

        //public ArcadiaAppData(ArcadiaApp app, File installedDir, String portNumber, String version) {
        ServiceController serviceController = ServiceController.getInstance();
        ArcadiaAppData testArcadiaAppData;
        if (serviceController.getOs() == OS.WINDOWS) {
            testArcadiaAppData = new ArcadiaAppData(
                    ArcadiaApp.CBOS,
                    new File("d:/opt/tomcat_cbos"),
                    "81",
                    "12R1");
        } else {
            testArcadiaAppData = new ArcadiaAppData(
                    ArcadiaApp.CBOS,
                    new File("/home/ecastel/opt/tomcat_cbos"),
                    "81",
                    "12R1");
        }
        testInstalledApps.put("CBOS", testArcadiaAppData);

        ArcadiaUpdater arcadiaUpdater = new ArcadiaUpdater();
        try {
            arcadiaUpdater.updateApp(ArcadiaApp.CBOS);
        } catch (UpdateException e) {
            e.printStackTrace();
        }
        /*ArcadiaController arcadiaController=ArcadiaController.getInstance();
        arcadiaController.getInstalledApps();
        System.out.printf("%s valid app targets\n", arcadiaController.installedApps.size());
        for (ArcadiaAppData arcadiaAppData : arcadiaController.installedApps.values()) {
            System.out.printf("Updating %s ...\n",arcadiaAppData.toString());
            boolean result;
            try {
                result = updateApp(arcadiaAppData.getApp());
            } catch (UpdateException e) {
                System.out.printf("ERROR: %s", e.getMessage());
                //e.printStackTrace();
            }
        }*/
//        for (ArcadiaAppData arcadiaAppData:
//                arcadiaController.getInstalledApps()) {
//            System.out.println(arcadiaAppData.toString());
//        }
        System.exit(0);


    }

    public Boolean updateApp(ArcadiaApp app) throws UpdateException {


/*

        // Initialize general Directories variables
      FileFinderController fileFinder =
                FileFinderController.doit("/", "arcadiaVersions", SearchType.Directories);

        // updates base directory
        // /opt/arcadiaVersions
        arcadiaController.getLowerDepthDirectory(fileFinder.getResults()),app.getShortName());
        System.out.printf("ArcadiaUpdater.updateApp updatesdir:%s\n", appUpdatesDirectory.toString());

        // latest update available

        File latestAppUpdatesDirectory = getLatestUpdate(appUpdatesDirectory.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        }));

        System.out.printf("ArcadiaUpdater.updateApp latestupdDir:%s\n", latestAppUpdatesDirectory.toString());

        // updating app base dir
        // /opt/tomcat_cbos e.g
        File installedAppDir = testInstalledApps.get(app.name()).getInstalledDir();
*/
        // Check database service
        ServiceController serviceController = ServiceController.getInstance();
        if (!serviceController.serviceAlive("postgres")) {
            throw new UpdateException("Database not started");
        }
        else System.out.println("OK: Database Server available");

        // Check rabbitmq
        if (!serviceController.serviceAlive("rabbitmq")) {
            throw new UpdateException("ERROR: Rabbitmq not started");
        } else System.out.println("OK: RabbitMq Server available");

        // Check zookeeper
        if (!serviceController.serviceAlive("zookeeper")) {
            throw new UpdateException("ERROR: Zookeeper not started");
        } else System.out.println("OK: Zookeeper Server available");

        /*
        // Stop tomcat service
        //serviceController.serviceAction("tomcat_cbos","stop");

        ReturnValues returnValues = serviceController.serviceAction(
                "tomcat_" + app.getShortName(), "stop");

        // Check tomcat stopped
        if (serviceController.serviceAlive("tomcat_" + app.getShortName())) {
            throw new UpdateException("Tomcat not stopped!!");
        } else System.out.println("OK: Tomcat is stopped");

        // Backup database

        File targetBackupDir = FileUtils.getFile(
                backupsController.getRootBackupsDir(),
                app.getDatabaseName(),
                app.getDatabaseName() + "_" + backupsController.getToday());
        if (backupsController.databaseBackup(app.getDatabaseName(), targetBackupDir) > 0) {
            throw new UpdateException("Error while backup'in database");
        }

        // Check last database backup size
        BigInteger lastBackupSize = backupsController.getDirSize(backupsController.getLastBackupDir(app));

        // Check database backup size
        BigInteger targetBackupDirSize = backupsController.getDirSize(targetBackupDir);
        System.out.printf("LastBackupSize: %s\ntargetBackupDirSize: %s\n", lastBackupSize, targetBackupDirSize);
        // Backup ArcadiaResources
        String separator = File.separator;
        ZipHandler zipHandler = new ZipHandler();
        */
/*String sourceFolder =
                arcadiaController.installedApps.get(
                        app.name()).getInstalledDir().toString() +
                        separator +
                        "webapps" +
                        separator +
                        "Arcadiaresources";*//*

        String sourceFolder =
                installedAppDir.toString() +
                        separator +
                        "webapps" +
                        separator +
                        "Arcadiaresources";

        String outputZip = backupsController.getRootBackupsDir().toString() +
                separator +
                "ArcadiaResources." + app.getShortName() + "." +
                backupsController.getToday() + ".zip";
        System.out.printf("Compressing folder %s to output zip: %s\n", sourceFolder, outputZip);
        zipHandler.zip(sourceFolder, outputZip, CompresionLevel.UNCOMPRESSED.getLevel());
*/


        File source, target;
        // Move sharedlib to backout
        File installedAppDir = testInstalledApps.get(app.name()).getInstalledDir();
        //File latestAppUpdatesDirectory = FileUtils.getFile("/home/ecastel/opt/arcadiaVersions/cbos/3.12R2(fw)");
        File latestAppUpdatesDirectory = FileUtils.getFile("D:/opt/arcadiaVersions/cbos/3.12R4(fw)");
        source = FileUtils.getFile(installedAppDir, "sharedlib");
        target = FileUtils.getFile(latestAppUpdatesDirectory, "backout");
        moveFilteredDir(source, target, TrueFileFilter.TRUE);

        // Move logback-common.xml to backout
        source = FileUtils.getFile(installedAppDir.toString(), "lib", "logback-common.xml");
        target = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout");
        moveFilteredDir(source, target, TrueFileFilter.TRUE);

        // Moving old wars and deployed dirs
        source = FileUtils.getFile(installedAppDir.toString(), "webapps");
        target = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout", "wars");
        FilenameFilter filterWebapps = new NotFileFilter(new NameFileFilter("ArcadiaResources"));
        moveFilteredDir(source, target, filterWebapps);

        // Move custom to backout
        source = FileUtils.getFile(installedAppDir.toString(), "custom");
        target = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout");
        moveFilteredDir(source, target, TrueFileFilter.TRUE);

        // Move commons WEB-INF to backout
        source = FileUtils.getFile(installedAppDir.toString(), "webapps", "ArcadiaResources");
        target = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout");
        FilenameFilter resourcesToBackout = new OrFileFilter(
                new NameFileFilter("commons"),
                new NameFileFilter("WEB-INF")
        );
        moveFilteredDir(source, target, resourcesToBackout);

        // Move logs to backout
        source = FileUtils.getFile(installedAppDir.toString(), "logs");
        target = FileUtils.getFile(latestAppUpdatesDirectory.toString(), "backout");
        moveFilteredDir(source, target, TrueFileFilter.TRUE);

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

        //
        // Now place new version
        //
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


        // Copy new logback
        try {
            FileUtils.copyDirectoryToDirectory(
                    FileUtils.getFile(latestAppUpdatesDirectory.toString(), "lib"),
                    installedAppDir
            );
        } catch (IOException e) {
            throw new UpdateException("Error copying logback configuration");
        }

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

        // Copy wars but ArcadiaResources reuse filterWebapps created before
        copyFilteredDir(
                FileUtils.getFile(latestAppUpdatesDirectory.toString(), "wars"),
                FileUtils.getFile(installedAppDir.toString(), "webapps"),
                filterWebapps);

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

        // Start service
        // Check schema_version all ok

        return true;
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
        backupsController.sortDirectoriesByDate(updatesDir);
        return updatesDir[0];
    }
}
