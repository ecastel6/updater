package app.controllers;

import app.models.ArcadiaAppData;
import app.models.ArcadiaApps;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArcadiaControllerTest
{

    @Test
    void getArcadiaAppDir() {
        ArcadiaController ac = new ArcadiaController();
        for (ArcadiaApps app : ArcadiaApps.values()) {
            File o = ac.getArcadiaAppDir(app);
            if (o != null) {
                System.out.printf("%s instalada: %s\n", app.getLongName(), o.toString());
                assertNotNull(o);
            } else {
                System.out.printf("%s no instalada\n", app.getLongName());
                assertNull(o);
            }
        }
    }


    @Test
    void getInstalledApps() {
        ArcadiaController ac = new ArcadiaController();
        ArrayList<ArcadiaAppData> arcadiaAppDataArrayList = ac.getInstalledApps();
        for (ArcadiaAppData appdata : arcadiaAppDataArrayList) {
            System.out.println(appdata.toString());
        }
    }

    @Test
    void getXMLconfig() {
        /* Primera aproximación
        Configurations configs = new Configurations();
        try
        {
            XMLConfiguration config = configs.xml("d:/opt/tomcat_oc/conf/server.xml");
            NodeList listNodes=config.getDocument().getElementsByTagName("Connector");
            Map<String,String> nodeValues = new HashMap<String,String>();
            for (int i=0; i<listNodes.getLength();i++) {
                Node node=listNodes.item(i);
                String key=node.getAttributes().getNamedItem("port").getTextContent();
                String val=node.getTextContent();
                nodeValues.put(key,val);
            }
            System.out.println(nodeValues);
        }
        catch (ConfigurationException cex)
        {
            // Something went wrong
        }*/

    /* Segunda opción
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                        .configure(params.fileBased().setFileName("d:/opt/tomcat_oc/conf/server.xml"));

        try {
            XMLConfiguration xmlConfiguration = fileBuilder.getConfiguration();
            Iterator<String> conf = xmlConfiguration.getKeys();

            for (Iterator<String> it = conf; it.hasNext(); ) {
                String s = it.next();
                System.out.println(s);
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
*/

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> fileBuilder =
                new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                        .configure(params.fileBased().setFileName("d:/opt/tomcat_oc/conf/server.xml"));

        try {
            XMLConfiguration xmlConfiguration = fileBuilder.getConfiguration();
            Iterator<String> conf = xmlConfiguration.getKeys();

            for (Iterator<String> it = conf; it.hasNext(); ) {
                String s = it.next();
                System.out.println(s);
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }


}