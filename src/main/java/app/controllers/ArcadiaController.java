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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArcadiaController
{

    ArrayList<ArcadiaAppData> installedApps;

    public String getArcadiaAppPort(ArcadiaApps app) {
        // todo maybe interesting to throw a blind detection of installed Apps or ports with HTTP GET queries
        // Look for Port in tomcat conf
        System.out.printf("getArcadiaAppDir(%s)\n", app.getShortName());
        if (getArcadiaAppDir(app) == null) return null;
        File tomcatConf = FileUtils.getFile(getArcadiaAppDir(app), "conf/server.xml");
        if (tomcatConf == null) return null;
        System.out.printf("Fichero conf=%s\n", tomcatConf.toString());
        // get port from config
        try {
            Configurations configs = new Configurations();
            XMLConfiguration config = configs.xml(tomcatConf);
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

    public String getVersionFromResponse(String response) {
        final Pattern pattern = Pattern.compile("^Version:\\s(.*)\\-RELEASE");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public String getArcadiaVersion(ArcadiaApps app) {
        // get arcadia app version parsing placed HTTP GET to version.html
        // get Port used with getArcadiaAppPort
        StringBuffer response = null;
        String appPort = getArcadiaAppPort(app);
        if (appPort == null) return null;
        try {
            URL url = new URL("http://localhost:" + getArcadiaAppPort(app) + "/" + app.getVersionInfo() + "/version.html");
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
            e.printStackTrace();
        }
        return null;
    }

    public File getLowerDepthDirectory(ArrayList<Path> alternatives) {
        Integer lowerDepth = Integer.MAX_VALUE;
        Path lowerDepthPath = null;
        for (Path path : alternatives) {
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
    public ArrayList<ArcadiaAppData> getInstalledApps() {

        // iterate Enumerated AppList search relevant info
        for (ArcadiaApps app : ArcadiaApps.values()) {
            File dir = this.getArcadiaAppDir(app);
            if (dir != null) {
                // App found collect relevant data
                System.out.println("App found collect relevant data");
                ArcadiaAppData arcadiaAppData = new ArcadiaAppData(
                        app,
                        dir,
                        getArcadiaAppPort(app),
                        getArcadiaVersion(app));
                System.out.printf("getInstalledApps: %s\n", arcadiaAppData.toString());
                //arcadiaController.installedApps.add(arcadiaAppData);
            }
        }
        return installedApps;
    }
}
