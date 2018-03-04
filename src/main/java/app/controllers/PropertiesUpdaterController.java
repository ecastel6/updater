package app.controllers;

import app.core.DiffMatchPatch;
import app.core.FileBasedConfigurationHandler;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
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

    public File updatePropertyFile(File oldPropertyFile, File newPropertyFile) {
        // parameters
        // old (actual) properties file
        // new (released) properties file
        // Returns File handle to merged properties file

        File tempFile = FileUtils.getFile(FileUtils.getTempDirectory(), "temp.properties");
        DiffMatchPatch patchObj = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Patch> patches = new LinkedList<DiffMatchPatch.Patch>();
        try {
            // Patch old configuration file preserve comments
            String oldProps = readFile(oldPropertyFile, StandardCharsets.UTF_8);
            String newProps = readFile(newPropertyFile, StandardCharsets.UTF_8);
            patches = patchObj.patch_make(oldProps, newProps);
            Object[] results = patchObj.patch_apply(patches, oldProps);

            //System.out.println("DiffPatch firstFile=" + oldPropertyFile + " newFile=" + newPropertyFile);
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
            //java.nio.file.Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.out.println(e.hashCode());
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } finally {
            return tempFile;
        }
    }

    public void updateCustom() throws IOException {
        // main method

        //targetCustomDir=FileUtils.getFile(FileUtils.getTempDirectory(),"tempTarget");

        // Copy sourceCustomOldDir to targetCustomDir
        FileUtils.copyDirectory(sourceCustomOldDir, targetCustomDir, true);

        // Copy all but properties or urlrewrite.xml from sourceCustomNewDir to targetCustomDir
        IOFileFilter excludeFilter = new NotFileFilter(
                new OrFileFilter(
                        new NameFileFilter("urlrewrite.xml"),
                        new SuffixFileFilter(".properties")
                )
        );
        FileUtils.copyDirectory(this.sourceCustomNewDir, this.targetCustomDir, excludeFilter);

        // Process all .properties files from sourceCustomNewDir, copy all non existing files to targetCustomDir
        IOFileFilter processFilter = new AndFileFilter(new SuffixFileFilter(".properties"),
                new NotFileFilter(
                        new OrFileFilter(
                                new NameFileFilter("Templates.properties"),
                                new NameFileFilter("jobs.properties")
                        )
                )
        );
        Collection<File> collectionProperties = FileUtils.listFiles(
                this.sourceCustomNewDir,
                processFilter,
                TrueFileFilter.INSTANCE);

        for (File newPropertiesFile : collectionProperties) {

            String newRelativePath = getRelativePath(newPropertiesFile, sourceCustomNewDir);
            File targetPropertiesFile = FileUtils.getFile(targetCustomDir, newRelativePath);
            File oldPropertiesFile = FileUtils.getFile(sourceCustomOldDir, newRelativePath);

            if (oldPropertiesFile.exists()) { // Previous version found merging
                System.out.printf("Merging %s with new %s\n",
                        oldPropertiesFile,
                        newPropertiesFile);
                FileUtils.copyFile(
                        updatePropertyFile(oldPropertiesFile, newPropertiesFile),
                        targetPropertiesFile,
                        true);
            } else {
                System.out.printf("Copying source file %s to %s\n",
                        newPropertiesFile,
                        FileUtils.getFile(targetCustomDir, newRelativePath));
                FileUtils.copyFile(
                        newPropertiesFile,
                        targetPropertiesFile,
                        true);
            }
        }
    }

    public String getRelativePath(File absolutePath, File basePath) {
        return basePath.toURI().relativize(absolutePath.toURI()).getPath();
    }
}