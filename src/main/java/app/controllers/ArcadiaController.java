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

public class ArcadiaController
{

    ArrayList<ArcadiaAppData> installedApps;

    public String getArcadiaAppPort(ArcadiaApps app) {
        // todo maybe interesting to throw a blind detection of installed Apps or ports with HTTP GET queries
        // Look for Port in tomcat conf
        File tomcatConf = FileUtils.getFile(getArcadiaAppDir(app), "conf/server.xml");
        System.out.printf("Looking %s. Fichero conf=%s\n", app.getLongName(), tomcatConf.toString());
        // get port from config
        try {
            Configurations configs = new Configurations();
            XMLConfiguration config = configs.xml(getArcadiaAppDir(app) + "/conf/server.xml");
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

    public String getArcadiaVersion(ArcadiaApps app) {
        String version = null;
        try {
            String arcadiaAppPort = getArcadiaAppPort(app);
            if (arcadiaAppPort != null) {
                URL url = new URL("http://localhost:" + arcadiaAppPort + "/" + app.getVersionInfo() + "/version.html");
                System.out.printf("Querying %s\n", url.toString());
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("GET");
                int responseCode = httpURLConnection.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                

            } else
                return null;
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            return version;
        }
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