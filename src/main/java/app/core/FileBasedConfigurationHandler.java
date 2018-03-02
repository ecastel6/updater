package app.core;

import org.apache.commons.configuration2.CombinedConfiguration;
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

    public boolean isKeyPresent(String key) {
        return this.getArrayList().contains(key);
    }

    public String getKeyValue(String key) throws ConfigurationException {

        if (this.isKeyPresent(key)) {
            return this.getConfig().getString(key);
        } else throw new ConfigurationException("Key not present");
    }

    // newConfigurationHandler FileBasedConfigurationHandler pointing to the new
    // properties file
    // dryRun write this file or printout json to stdout
    public CombinedConfiguration patchPropertiesFile(FileBasedConfigurationHandler newConfigurationHandler) {

        // Add only new properties
        // Delete deprecated properties

        List<String> oldProperties = this.getArrayList();
        List<String> newProperties = newConfigurationHandler.getArrayList();

        PropertiesOperators operator = new PropertiesOperators();
        List<String> propertiesToDelete = operator.DiffList(oldProperties, newProperties);

        // Join configurations
        CombinedConfiguration combined = operator.mergeProperties(this.getConfig(),
                newConfigurationHandler.getConfig());

        // Deleting deprecated properties
        for (int i = 0; i < propertiesToDelete.size(); i++) {
            // TODO logger System.out.println("Borrando: " + propertiesToDelete.get(i));
            combined.clearProperty(propertiesToDelete.get(i));
            this.getConfig().clearProperty(propertiesToDelete.get(i));
        }
        return combined;
        /*
         * // Update old configuration file Iterator<String> keys = combined.getKeys();
         * while (keys.hasNext()) { String key = keys.next(); if
         * (!this.getConfig().containsKey(key)) { // key not present add it
         * this.getConfig().addProperty(key, combined.getString(key)); } }
         *
         * try { if (dryRun) { // TODO logger System.out.println("Dry run.");
         * System.out.println(this.toString()); } else {
         * System.out.println("Patching file " + this.getFileName());
         * this.builder.save(); } } catch (ConfigurationException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */
    }

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
