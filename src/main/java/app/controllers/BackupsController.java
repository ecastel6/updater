package app.controllers;

import app.models.ArcadiaApps;
import app.models.SearchType;
import com.google.common.io.ByteStreams;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class BackupsController
{
    // todo check backups
    // todo get backup size
    // todo backup ArcadiaResources
    // todo backup database

    private File rootBackupsDir;

    private static BackupsController ourInstance = new BackupsController();

    public static BackupsController getInstance() {
        return ourInstance;
    }

    private BackupsController() {
        this.rootBackupsDir = getRootBackupsDir();
    }

    public BigInteger getDirSize(File directory) {
        return FileUtils.sizeOfDirectoryAsBigInteger(directory);
    }

    public File getLastBackupDir(ArcadiaApps app) {
        File directory = FileUtils.getFile(this.rootBackupsDir, app.getDatabaseName());
        System.out.printf("Searching %s directory\n", directory);

        // app backups directory not found
        if (!directory.exists()) return null;
        File[] listDirectories = directory.listFiles();

        // app backups directory empty
        if (listDirectories.length == 0) return null;

        Arrays.sort(listDirectories, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
        System.out.printf("directories found: %s newer is: %s\n", listDirectories.length, listDirectories[0]);
        return listDirectories[0];
    }

    public File getRootBackupsDir() {
        ArcadiaController arcadiaController = new ArcadiaController();
        // Simple shot, lowerDepthDirectory, guess system has daily
        FileFinderController fileFinderController = FileFinderController.doit("/", "daily", SearchType.Directories);
        return arcadiaController.getLowerDepthDirectory(fileFinderController.results);
    }

    public void setRootBackupsDir(File rootBackupsDir) {
        this.rootBackupsDir = rootBackupsDir;
    }

    // https://www.programcreek.com/java-api-examples/index.php?class=org.apache.commons.compress.archivers.tar.TarArchiveEntry&method=setSize
    private static TarArchiveOutputStream buildTarStream(Path outputPath, boolean gZipped) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputPath));
        if (gZipped) {
            outputStream = new GzipCompressorOutputStream(outputStream);
        }
        TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(outputStream);
        tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        return tarArchiveOutputStream;
    }

    static void putTarEntry(TarArchiveOutputStream tarOutputStream, TarArchiveEntry tarEntry, Path file)
            throws IOException {
        tarEntry.setSize(Files.size(file));
        tarOutputStream.putArchiveEntry(tarEntry);
        try (InputStream input = new BufferedInputStream(Files.newInputStream(file))) {
            ByteStreams.copy(input, tarOutputStream);
            tarOutputStream.closeArchiveEntry();
        }
    }


    public static void tar(Path inputPath, Path outputPath) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new FileNotFoundException("File not found " + inputPath);
        }

        try (TarArchiveOutputStream tarArchiveOutputStream = buildTarStream(outputPath.toFile())) {
            if (!Files.isDirectory(inputPath)) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(inputPath.toFile().getName());
                if (inputPath.toFile().canExecute()) {
                    tarEntry.setMode(tarEntry.getMode() | 0755);
                }
                putTarEntry(tarArchiveOutputStream, tarEntry, inputPath);
            } else {
                Files.walkFileTree(inputPath,
                        new TarDirWalker(inputPath, tarArchiveOutputStream));
            }
            tarArchiveOutputStream.flush();
        }
    }

    public void createZipBackup(File startDir) {

        //https://commons.apache.org/proper/commons-compress/examples.html

    }

}










