package app.controllers;

import app.core.DiffMatchPatch;
import app.core.FileBasedConfigurationHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PropertiesUpdaterController {
    File sourceCustomOldDir, sourceCustomNewDir, targetCustomDir;

    public PropertiesUpdaterController(File sourceCustomOldDir, File sourceCustomNewDir, File targetCustomDir) {
        this.sourceCustomOldDir = sourceCustomOldDir;
        this.sourceCustomNewDir = sourceCustomNewDir;
        this.targetCustomDir = targetCustomDir;
    }

    public PropertiesUpdaterController() {
    }

    @Override
    public String toString() {
        return "PropertiesUpdaterController{" +
                "sourceCustomOldDir=" + sourceCustomOldDir +
                ", sourceCustomNewDir=" + sourceCustomNewDir +
                ", targetCustomDir=" + targetCustomDir +
                '}';
    }

    static String readFile(File inputFile, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(inputFile.toPath());
        return new String(encoded, encoding);
    }

    static void writeFile(File outputFile, String inputStr, Charset encoding) throws IOException {
        try (PrintWriter out = new PrintWriter(outputFile, encoding.toString())) {
            out.write(inputStr);
        }
    }

    public void updatePropertyFile(File oldPropertyFile, File newPropertyFile, boolean dryRun) {
        // parameters
        // old (actual) properties file
        // new (released) properties file
        // DryRun whether should overwrite old(actual) file inplace or dump json in stdout

        DiffMatchPatch patchObj = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Patch> patches = new LinkedList<DiffMatchPatch.Patch>();
        try {
            // Patch old configuration file preserve comments
            String oldProps = readFile(oldPropertyFile, StandardCharsets.UTF_8);
            String newProps = readFile(newPropertyFile, StandardCharsets.UTF_8);
            patches = patchObj.patch_make(oldProps, newProps);
            Object[] results = patchObj.patch_apply(patches, oldProps);

            File tempFile = FileUtils.getFile(FileUtils.getTempDirectory(), "temp.properties");
            File targetFile = oldPropertyFile;
            System.out.println("DiffPatch firstFile=" + oldPropertyFile + " newFile=" + newPropertyFile);
            // System.out.println("Writing patched ...");
            writeFile(tempFile, results[0].toString(), StandardCharsets.UTF_8);

            // Mix properties
            // System.out.println("Merging properties ...");
            FileBasedConfigurationHandler oldConfigurationHandler = new FileBasedConfigurationHandler(oldPropertyFile.toString());
            FileBasedConfigurationHandler newConfigurationHandler = new FileBasedConfigurationHandler(newPropertyFile.toString());

            FileBasedConfigurationHandler targetConfigurationHandler = new FileBasedConfigurationHandler(tempFile.toString());
            // Get final values of properties
            CombinedConfiguration finalProperties = oldConfigurationHandler
                    .patchPropertiesFile(newConfigurationHandler);

            // System.out.println("Loading preserved properties...");
            // key updatedProperties list properties which update must be forced
            List<Object> updated = finalProperties.getList("updatedProperties");

            Iterator<String> keys = finalProperties.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (updated.contains(key)) {
                    // System.out.println("update value key= " + key);
                } else {
                    // System.out.println("preserve value key= " + key + ": Value= " +
                    targetConfigurationHandler.getConfig().setProperty(key, finalProperties.getString(key));
                }
            }
            targetConfigurationHandler.getFileHandler().save();
            if (dryRun) {
                System.out.printf("Dry run, output to %s.dryrun\n", oldPropertyFile.getName());
                targetFile = FileUtils.getFile(oldPropertyFile.getParent(), oldPropertyFile.getName() + ".dryrun");
                System.out.println(targetFile.toString());
            }
            // todo check performance of FileUtils.moveFile(tempFile,targetFile) alternative
            System.out.printf("Moving: %s to %s REPLACE_EXISTING\n", tempFile.toString(), targetFile.toString());
            java.nio.file.Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.out.println(e.hashCode());
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void updateCustom() {
        // main method
        // takes source and target from class
        // Generate new custom directory with changes from release
        PropertiesUpdaterController puc = new PropertiesUpdaterController();

        puc.updatePropertyFile(
                FileUtils.getFile("/tmp/rabbitmq_old.properties"),
                FileUtils.getFile("/tmp/rabbitmq_new.properties"),
                true);

        // todo add copying all non properties files
        // process all properties but
        // find $currentDir/ -type f -iname "*.properties" | sed "s@$currentDir/@@g" | grep -v "jobs.properties" | grep -v "Templates.properties")
        // calls updatePropertyFile with every property file

    }
}