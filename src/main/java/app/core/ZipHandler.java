package app.core;
///////
// DEPRECATED GO WITH ZIP4J
/////////////

import app.models.CompresionLevel;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHandler
{

    List<String> fileList;
    //    private static final String OUTPUT_ZIP_FILE = "C:\\tmp\\ziptest.zip";
//    private static final String SOURCE_FOLDER = "C:\\tmp\\ziptest";
    String Folder;
    static String ZipFile;

    public ZipHandler() {
        fileList = new ArrayList<String>();
    }

    public static void main(String[] args) {

        String zip = "/home/ecastel/opt/arcadiaVersions/cbos/3.12R2(fw)/wars/ArcadiaResources.war";
        String destination = "/tmp/ziptest";
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zip);
            FileUtils.cleanDirectory(new File(destination));
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFolder() {
        return Folder;
    }

    public void setFolder(String folder) {
        Folder = folder;
    }

    public String getZipFile() {
        return ZipFile;
    }

    public void setZipFile(String zipFile) {
        ZipFile = zipFile;
    }

    /**
     * compress
     *  @param sourceFolder
     * @param outputZip
     * @param level        compression level
     */
    public void zip(String sourceFolder, String outputZip, CompresionLevel level) {
        byte[] buffer = new byte[1024];
        this.setFolder(sourceFolder);
        this.setZipFile(outputZip);
        generateFileList(new File(sourceFolder));
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputZip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            zipOutputStream.setLevel(level.getLevel());
            for (String file : this.fileList) {
                File absoluteFilename = new File(sourceFolder + File.separator + file);
                ZipEntry zipEntry = new ZipEntry(file);
                zipEntry.setTime(absoluteFilename.lastModified());
                zipOutputStream.putNextEntry(zipEntry);

                FileInputStream fileInputStream =
                        new FileInputStream(sourceFolder + File.separator + file);

                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }
                fileInputStream.close();
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            System.out.println("Done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    private void generateFileList(File node) {

        //add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNode = node.list();
            for (String filename : subNode) {
                generateFileList(new File(node, filename));
            }
        }
    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
        return file.substring(this.getFolder().length() + 1, file.length());
    }

    /**
     * unzip zipfile to outputFolder
     *
     * @param zipFile
     * @param outputFolder
     * @return void
     */
    public void unzip(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];
        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zipInputStream =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non existing folders
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();

                if (!newFile.setLastModified(zipEntry.getTime()))
                    System.out.println("ERROR: Unable to set file timestamp");
                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();
            System.out.println("Done");

        } catch (IOException ex) {
            //ex.printStackTrace();
        }
    }
}



