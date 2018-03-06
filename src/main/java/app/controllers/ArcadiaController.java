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
import java.util.ArrayList;

public class ArcadiaController
{

    ArrayList<ArcadiaAppData> installedApps;

    public String getArcadiaAppPort(ArcadiaApps app) {
        // grep tomcat conf search Port
        File tomcatConf = FileUtils.getFile(getArcadiaAppDir(app), "conf/server.xml");
        System.out.printf("Fichero conf=%s\n", tomcatConf.toString());
        // get port from config
        try {
            Configurations configs = new Configurations();
            XMLConfiguration config = configs.xml("/home/ecastel/opt/tomcat_cbos/conf/server.xml");
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
        try {
            URL url = new URL("http://localhost:" + getArcadiaAppPort(app) + "/" + app.getShortName() + "/version.html");
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
            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "10.0";
    }

    public File getArcadiaAppDir(ArcadiaApps appName) {
        // find directory tomcat + short name
        FileFinderController arcadiaAppDir = FileFinderController.doit("/", "tomcat_" + appName.getShortName(), SearchType.Directories);
        if (arcadiaAppDir.getNumMatches() > 0) {
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
