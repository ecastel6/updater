package app.core;

import app.models.CompresionLevel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipHandler {

    List<String> fileList;
    //    private static final String OUTPUT_ZIP_FILE = "C:\\tmp\\ziptest.zip";
//    private static final String SOURCE_FOLDER = "C:\\tmp\\ziptest";
    String sourceFolder;
    String OutputZip;

    public ZipHandler(String sourceFolder, String outputZip) {
        this.sourceFolder = sourceFolder;
        this.OutputZip = outputZip;
        fileList = new ArrayList<String>();
        generateFileList(new File(sourceFolder));
    }

    /**
     * Zipit
     *
     * @param level compression level
     */
    public void zipIt(int level) {
        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(this.OutputZip);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.setLevel(level);
            for (String file : this.fileList) {
                File f = new File(sourceFolder + File.separator + file);
                //System.out.printf("File Added : %s. LastModified: %s\n",f.toString()+String.valueOf(f.lastModified()));
                ZipEntry ze = new ZipEntry(file);
                ze.setTime(f.lastModified());
                zos.putNextEntry(ze);

                FileInputStream in =
                        new FileInputStream(sourceFolder + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

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
        return file.substring(sourceFolder.length() + 1, file.length());
    }

    public static void main(String[] args) {
        ZipHandler appZip = new ZipHandler("c:\\tmp\\ziptest", "c:\\tmp\\ziptest.zip");
        appZip.zipIt(CompresionLevel.UNCOMPRESSED.getLevel());
    }
}
