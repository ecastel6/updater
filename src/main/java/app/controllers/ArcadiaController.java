package app.controllers;

import app.core.Version;
import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.SearchType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArcadiaController {
    private static ArcadiaController ourInstance = new ArcadiaController();
    private static LogController logController = LogController.getInstance();
    private Path arcadiaUpdatesRepository;
    private Map<String, ArcadiaAppData> installedApps = new HashMap<>();
    private Map<String, ArcadiaAppData> availableUpdates = new HashMap<>();
    private CommandLine commandLine;

    public ArcadiaController(Map<String, ArcadiaAppData> installedApps) {
        this.installedApps = installedApps;
    }

    private ArcadiaController() {

    }

    public static ArcadiaController getInstance() {
        return ourInstance;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    /*
                Read tomcat conf get HTTP connector port
        */
    private String getArcadiaAppPort(File configFile) {

        logController.log.info(String.format("Parsing config %s", configFile));
        // get port from config
        try {
            Configurations configs = new Configurations();
            XMLConfiguration config = configs.xml(configFile.toString());
            NodeList listNodes = config.getDocument().getElementsByTagName("Connector");

            //Map<String,String> nodeValues = new HashMap<>();
            for (int i = 0; i < listNodes.getLength(); i++) {
                Node node = listNodes.item(i);
                if (node.getAttributes().getNamedItem("protocol").getTextContent().contains("HTTP")) {
                    return node.getAttributes().getNamedItem("port").getTextContent();
                }
            }
        } catch (ConfigurationException e) {
            logController.log.severe(e.toString());
            return null;
        }
        return null;
    }

    /*
            Auxiliary method decode response to get Arcadia AppVersion
    */
    public Version getVersionFromResponse(String response) {
        final Pattern pattern = Pattern.compile("^Version:\\s*(.*?)(\\-.*?)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return new Version(new SystemCommons().normalizeVersion(matcher.group(1)));
        } else {
            return null;
        }
    }

    /*
            getArcadiaVersion Get applicattion version with HTTP GET
            to version.html
    */
    public Version getArcadiaVersion(ArcadiaApp app, String port) {
        StringBuffer response = null;
        URL url = null;
        try {
            url = new URL("http://localhost:" + port + "/" + app.getVersionInfo() + "/version.html");
        } catch (MalformedURLException e) {
            logController.log.severe(String.format("invalid URL: %s", url.toString()));
            return null;
        }
        try {
            HttpURLConnection httpURLConnection = null;
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200) return null;

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));

            String inputLine;
            response = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();
            logController.log.config(String.format("Response from server: %s", response));
            return (getVersionFromResponse(response.toString()));
        } catch (IOException e) {
            logController.log.severe(String.format("%s Server not listening. unable to get version in %s", app.getLongName(), url));
            return null;
        }
    }

    public File getLowerDepthDirectory(ArrayList<Path> alternatives) {
        Integer lowerDepth = Integer.MAX_VALUE;
        Path lowerDepthPath = null;
        for (Path path : alternatives) {
            logController.log.config(String.format("Path=%s", path.toString()));
            if (path.getNameCount() < lowerDepth) {
                lowerDepth = path.getNameCount();
                lowerDepthPath = path;
            }
        }
        return lowerDepthPath.toFile();
    }

    public File getTomcatDir(String tomcatAppDir) {
        // find lower directory pattern + short name
        // pattern
        // tomcat_ for application
        // arcadiaVersions

        FileFinderControllerStr arcadiaDir = FileFinderControllerStr.doit("/", tomcatAppDir, SearchType.Directories);
        if (arcadiaDir.getNumMatches() > 1) {
            // if multiple directories found, the node with less depth is returned
            return getLowerDepthDirectory(arcadiaDir.getResults());
        } else if (arcadiaDir.getNumMatches() == 1) {
            return arcadiaDir.getResults().get(0).toFile();
        }
        return null;
    }

    /*
        Search installed Arcadia Apps
        returns Map String -> ArcadiaAppData
    */
    public Map<String, ArcadiaAppData> getInstalledApps() {
        if (this.installedApps.size() > 0) {
            return installedApps;
        }
        logController.log.info("Looking for ArcadiaApp. Please standby...");
        // iterate Enum AppList collect relevant info
        for (ArcadiaApp app : ArcadiaApp.values()) {
            logController.log.config(String.format("Searching %s ...", app.getLongName()));
            File tomcatDir = this.getTomcatDir("opt/tomcat_" + app.getShortName());
            if (tomcatDir != null) {
                // App found collect relevant data
                logController.log.info(String.format("App %s found collecting data...", app.getLongName()));
                String appPort = getArcadiaAppPort(FileUtils.getFile(tomcatDir, "conf", "server.xml"));
                Version appVersion = getArcadiaVersion(app, appPort);
                logController.log.info(String.format("App version: %s.", appVersion));

                if (appPort != null) {
                    ArcadiaAppData arcadiaAppData = new ArcadiaAppData(app, tomcatDir, appPort, appVersion);
                    installedApps.put(app.getShortName().toUpperCase(), arcadiaAppData);
                }
            }
        }
        return installedApps;
    }

    public Path getArcadiaUpdatesRepository() {
        if (arcadiaUpdatesRepository != null) {
            return arcadiaUpdatesRepository;
        }
        logController.log.config("FilefinderControllerStr searching opt/arcadiaVersions in filesystem...");
        FileFinderControllerStr fileFinder = FileFinderControllerStr.doit("/", "opt/arcadiaVersions", SearchType.Directories);
        if (fileFinder.getNumMatches() > 1)
            this.arcadiaUpdatesRepository = getLowerDepthDirectory(fileFinder.getResults()).toPath();
        else this.arcadiaUpdatesRepository = fileFinder.getResults().get(0);
        logController.log.config(String.format("ArcadiaUpdater.updateApp updatesdir:%s", arcadiaUpdatesRepository));
        return arcadiaUpdatesRepository;
    }

    public void setArcadiaUpdatesRepository(Path arcadiaUpdatesRepository) {
        this.arcadiaUpdatesRepository = arcadiaUpdatesRepository;
    }

    /*
        Search available updates within arcadiaUpdatesRepository
        returns Map String -> ArcadiaAppData
    */
    public Map<String, ArcadiaAppData> getAvailableUpdates() {
        if (!availableUpdates.isEmpty()) {
            return availableUpdates;
        }
        logController.log.info("Checking available updates...");
        final List<String> validArcadiaDirectories = validArcadiaDirectories();

        logController.log.info("Searching UpdatesRepository...");
        File[] updatesSubdirs = getArcadiaUpdatesRepository().toFile().listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory()
                                && validArcadiaDirectories.contains(pathname.getName());
                    }
                });
        SystemCommons systemCommons = new SystemCommons();
        if (updatesSubdirs.length > 0)
            for (File directory : updatesSubdirs) {
                logController.log.info(String.format("Checking versions from %s", directory));
                File[] versionDirs = directory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().matches("[0-9]+?(\\.[0-9]+)(R[0-9]+)?");
                    }
                });
                if (versionDirs.length != directory.listFiles().length)
                    logController.log.warning(String.format("WARNING: update directory %s contains invalid folders", directory));
                if (versionDirs.length > 0) {
                    File[] sortedDirectoryList = systemCommons.sortDirectoriesByVersion(versionDirs);
                    File newestVersion = (sortedDirectoryList == null) ? null : sortedDirectoryList[0];
                    if (newestVersion != null) {
                        ArcadiaAppData arcadiaAppData = new ArcadiaAppData();
                        arcadiaAppData.setDirectory(newestVersion);
                        arcadiaAppData.setVersion(
                                new Version(systemCommons.normalizeVersion(
                                        FilenameUtils.getName(newestVersion.getAbsolutePath()))
                                ));
                        availableUpdates.put(directory.getName().toUpperCase(), arcadiaAppData);
                    }
                } else logController.log.warning(String.format("ERROR: empty updates directory %s", directory));
            }
        return availableUpdates;
    }

    /*
            Generate a List with Arcadia shortnames
    */
    public List<String> validArcadiaDirectories() {
        List<String> appnames = new ArrayList<>();
        for (ArcadiaApp app : ArcadiaApp.values())
            appnames.add(app.getShortName());
        return appnames;
    }

}