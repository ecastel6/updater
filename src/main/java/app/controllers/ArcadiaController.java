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

public class ArcadiaController {

    //ArrayList<ArcadiaAppData> installedApps;
    public Map<String, ArcadiaAppData> installedApps = new HashMap<>();

    private static ArcadiaController ourInstance = new ArcadiaController();

    public static ArcadiaController getInstance() {
        return ourInstance;
    }

    private ArcadiaController() {

    }

    private String getArcadiaAppPort(File configFile) {
        // todo maybe interesting to throw a blind detection of installed Apps or ports with HTTP GET queries
        // Look for Port in tomcat conf
        /*System.out.printf("getArcadiaAppPor - getArcadiaAppDir(%s)\n", app.getShortName());
        if (installedApps.

                == null) return null;
        File tomcatConf = FileUtils.getFile(getArcadiaAppDir(app), "conf/server.xml");
        if (tomcatConf == null) return null;*/
        System.out.printf("Fichero conf=%s\n", configFile);
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

    public String getArcadiaVersion(ArcadiaApp app, String port) {
        // get arcadia app version parsing placed HTTP GET to version.html
        // get Port used with getArcadiaAppPort
        StringBuffer response = null;
        System.out.println(app.name());
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
            System.out.printf("Server not listening %s\n", url);
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

    public File getArcadiaDir(String pattern) {
        // find lower directory pattern + short name
        // pattern
        // tomcat_ for application
        // arcadiaVersions

        FileFinderController arcadiaDir = FileFinderController.doit("/", pattern, SearchType.Directories);
        if (arcadiaDir.getNumMatches() > 1) {
            // if multiple directories found, the node with less depth is returned
            return getLowerDepthDirectory(arcadiaDir.getResults());
        } else if (arcadiaDir.getNumMatches() == 1) {
            return arcadiaDir.getResults().get(0).toFile();
        }
        return null;
    }

    public String getArcadiaUpdateVersion(ArcadiaApp app) {
        // find update directory arcadiaVersions + short name

        return null;
    }

    // Search system, returns ArcadiaAppData Arraylist
    public Map<String, ArcadiaAppData> getInstalledApps() {
        System.out.println("Looking for ArcadiaApp. \nPlease standby...");
        // iterate Enumerated AppList search relevant info
        for (ArcadiaApp app : ArcadiaApp.values()) {
            System.out.printf("Searching for %s\n", app.getLongName());
            File dir = this.getArcadiaDir("tomcat_" + app.getShortName());
            if (dir != null) {
                // App found collect relevant data
                System.out.println("App dir found collecting data...");
                String appPort = getArcadiaAppPort(FileUtils.getFile(dir, "conf", "server.xml"));
                String appVersion = getArcadiaVersion(app, appPort);

                // only register running Arcadia apps with valid responsive ports
                if ((appVersion != null) && (appPort != null)) {
                    ArcadiaAppData arcadiaAppData = new ArcadiaAppData(app, dir, appPort, appVersion);
                    installedApps.put(app.name(), arcadiaAppData);
                }
            }
        }
        return installedApps;
    }
}
