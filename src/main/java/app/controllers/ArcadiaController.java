package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import app.models.SearchType;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArcadiaController
{

    private static ArcadiaController ourInstance = new ArcadiaController();
    public Map<String, ArcadiaAppData> installedApps = new HashMap<>();
    private Path arcadiaUpdatesRepository;

    public ArcadiaController(Map<String, ArcadiaAppData> installedApps) {
        this.installedApps = installedApps;
    }

    private ArcadiaController() {
    }

    public static ArcadiaController getInstance() {
        return ourInstance;
    }

    /*
            Read tomcat conf get HTTP connector port
    */
    private String getArcadiaAppPort(File configFile) {

        System.out.printf("Parsing config %s\n", configFile);
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
            return null;
            //e.printStackTrace();
        }
        return null;
    }

    /*
            Auxiliary method decode response to get Arcadia AppVersion
    */
    private String getVersionFromResponse(String response) {
        final Pattern pattern = Pattern.compile("^Version:\\s(.*)\\-RELEASE");
        Matcher matcher = pattern.matcher(response);
        System.out.println("getVersionFromResponse");
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    /*
            getArcadiaVersion Get applicattion version with HTTP GET
            to version.html
    */
    public String getArcadiaVersion(ArcadiaApp app, String port) {
        StringBuffer response = null;
        URL url = null;
        try {
            url = new URL("http://localhost:" + port + "/" + app.getVersionInfo() + "/version.html");
        } catch (MalformedURLException e) {
            System.out.printf("ERROR: invalid URL: %s", url.toString());
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
            return (getVersionFromResponse(response.toString()));
        } catch (IOException e) {
            System.out.printf("%s Server not listening. unable to get version in %s\n", app.getLongName(), url);
            return null;
        }
    }


    public File getLowerDepthDirectory(ArrayList<Path> alternatives) {
        Integer lowerDepth = Integer.MAX_VALUE;
        Path lowerDepthPath = null;
        for (Path path : alternatives) {
            System.out.printf("Path=%s\n", path.toString());
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
            todo find update version for App in updates directory arcadiaVersions
     */
    public String getArcadiaUpdateVersion(ArcadiaApp app) {


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
        System.out.println("Looking for ArcadiaApp. Please standby...");
        // iterate Enum AppList collect relevant info
        for (ArcadiaApp app : ArcadiaApp.values()) {
            System.out.printf("Searching for %s\n", app.getLongName());
            File tomcatDir = this.getTomcatDir("opt/tomcat_" + app.getShortName());
            if (tomcatDir != null) {
                // App found collect relevant data
                System.out.println("App dir found collecting data...");
                String appPort = getArcadiaAppPort(FileUtils.getFile(tomcatDir, "conf", "server.xml"));
                String appVersion = getArcadiaVersion(app, appPort);

                if (appPort != null) {
                    ArcadiaAppData arcadiaAppData = new ArcadiaAppData(app, tomcatDir, appPort, appVersion);
                    installedApps.put(app.name(), arcadiaAppData);
                }
            }
        }
        return installedApps;
    }

    public Path getArcadiaUpdatesRepository() {
        if (arcadiaUpdatesRepository != null) return arcadiaUpdatesRepository;
        FileFinderControllerStr fileFinder = FileFinderControllerStr.doit("/", "opt/arcadiaVersions", SearchType.Directories);
        if (fileFinder.getNumMatches() > 1)
            this.arcadiaUpdatesRepository = ArcadiaController.getInstance().getLowerDepthDirectory(fileFinder.getResults()).toPath();
        else this.arcadiaUpdatesRepository = fileFinder.getResults().get(0);

        return arcadiaUpdatesRepository;
    }
}