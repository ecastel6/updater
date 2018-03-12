package app.controllers;

import app.models.ArcadiaAppData;
import app.models.ArcadiaApps;
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
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArcadiaController
{

    //ArrayList<ArcadiaAppData> installedApps;
    Map<String, ArcadiaAppData> installedApps = new HashMap<>();

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
            e.printStackTrace();
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

    public String getArcadiaVersion(ArcadiaApps app, String port) {
        // get arcadia app version parsing placed HTTP GET to version.html
        // get Port used with getArcadiaAppPort
        StringBuffer response = null;
        System.out.println(app.name());
        try {
            URL url = new URL("http://localhost:" + port + "/" + app.getVersionInfo() + "/version.html");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
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
            //e.printStackTrace();
        }
        return null;
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

    public File getArcadiaAppDir(ArcadiaApps appName) {
        // find directory tomcat + short name
        FileFinderController arcadiaAppDir = FileFinderController.doit("/", "tomcat_" + appName.getShortName(), SearchType.Directories);
        if (arcadiaAppDir.getNumMatches() > 1) {
            // if multiple directories found, the node with less depth is returned
            return getLowerDepthDirectory(arcadiaAppDir.getResults());
        } else if (arcadiaAppDir.getNumMatches() == 1) {
            return arcadiaAppDir.getResults().get(0).toFile();
        }
        return null;
    }

    // Search system, returns ArcadiaAppData Arraylist
    public Map<String, ArcadiaAppData> getInstalledApps() {
        System.out.println("Looking for ArcadiaApps. \nPlease standby...");
        // iterate Enumerated AppList search relevant info
        for (ArcadiaApps app : ArcadiaApps.values()) {
            System.out.printf("Searching for %s\n", app.getLongName());
            File dir = this.getArcadiaAppDir(app);
            if (dir != null) {
                // App found collect relevant data
                System.out.println("App found collect relevant data");
                String appPort = getArcadiaAppPort(FileUtils.getFile(dir, "conf", "server.xml"));
                ArcadiaAppData arcadiaAppData = new ArcadiaAppData(
                        app,
                        dir,
                        appPort,
                        getArcadiaVersion(app, appPort));
                System.out.printf("getInstalledApps: %s\n", arcadiaAppData.toString());
                installedApps.put(app.name(), arcadiaAppData);
            }
        }
        return installedApps;
    }
}
