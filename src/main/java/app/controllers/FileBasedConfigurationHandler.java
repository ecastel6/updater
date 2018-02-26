package app.controllers;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileBasedConfigurationHandler {

    private String fileName;

    FileBasedConfigurationBuilder<FileBasedConfiguration> builder;

    Configuration config;

    public FileBasedConfigurationHandler(String propertiesFileName) throws ConfigurationException {
        fileName = propertiesFileName;
        Parameters params = new Parameters();
        this.builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFileName(propertiesFileName)
                        // .setListDelimiterHandler(new DefaultListDelimiterHandler(','))
                );
        this.config = this.builder.getConfiguration();
    }

    public String getFileName() {
        return fileName;
    }

    public FileBasedConfigurationBuilder<FileBasedConfiguration> getFileHandler() {
        return builder;
    }

    public void setFileHandler(FileBasedConfigurationBuilder<FileBasedConfiguration> fileHandler) {
        this.builder = fileHandler;
    }

    public Iterator<String> getKeys() {
        return this.getConfig().getKeys();
    }

    public Configuration getConfig() {
        return config;
    }

    public List<String> getArrayList() {
        Iterator<String> keys = this.getKeys();
        List<String> keyList = new ArrayList<String>();
        while (keys.hasNext()) {
            keyList.add(keys.next());
        }
        return keyList;
    }

    // newConfigurationHandler FileBasedConfigurationHandler pointing to the new
    // properties file
    // dryRun write this file or printout json to stdout

    @Override
    public String toString() {
        String str = "";
        for (Iterator<String> iterator = this.getKeys(); iterator.hasNext(); ) {
            String key = iterator.next();
            Object value = this.getConfig().getString(key);
            str += "\"" + key + "\":\"" + value + "\",";
        }
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return "[" + str + "]";
    }
}
