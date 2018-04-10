package app.controllers;

import app.models.OS;
import app.models.SearchType;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileFinderControllerStr
        extends SimpleFileVisitor<Path> {
    static ServiceController serviceController = ServiceController.getInstance();
    ArrayList<Path> results = new ArrayList<Path>();
    private double numMatches = 0;
    private SearchType searchType;
    private String pattern;

    public FileFinderControllerStr(String pattern, SearchType searchType) {
        this.searchType = searchType;
        if (serviceController.os.equals(OS.WINDOWS)) {
            this.pattern = FilenameUtils.separatorsToSystem(pattern);
            //System.out.printf("Pattern converted: %s\n", this.pattern);
        } else
            this.pattern = pattern;

    }

    public static ArrayList<String> getDriveList() {
        ArrayList<String> driveList = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            if (Files.isWritable(root)) {
                try {
                    FileStore fileStore = Files.getFileStore(root);
                    if ((!fileStore.isReadOnly()) && (!fileStore.getAttribute("volume:isRemovable").equals(true))) {
                        driveList.add(root.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return driveList;
    }

    public static FileFinderControllerStr doit(String startPath, String pattern, SearchType searchType) {
        // what =0 all
        // what =1 files
        // what =2 dirs
        //for (Path p: getDriveList()) System.out.println(p.toString());
        long startTime = System.currentTimeMillis();

        FileFinderControllerStr finder = new FileFinderControllerStr(pattern, searchType);


        List<String> driveList;
        if (serviceController.os.equals(OS.LINUX)) {
            //List<String> driveList = Arrays.asList("");
            driveList = Collections.singletonList("");
        } else {
            driveList = getDriveList();
        }
        for (String everyDrive : driveList)
            try {
                Files.walkFileTree(Paths.get(everyDrive, startPath), finder);
            } catch (IOException e) {
                e.printStackTrace();
            }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.printf("FileSystem search elapsed time: %s\n", elapsedTime);

        return finder;
    }

    void find(Path file) {
        //System.out.printf("find Path File: %s\n",file.toString());
        if (file != null && file.toString().endsWith(this.pattern)) {
            results.add(file);
            numMatches++;
        }
    }

    public ArrayList<Path> getResults() {
        return results;
    }

    public double getNumMatches() {
        return numMatches;
    }

    // Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) {
        // what =0 all
        // what =1 only files
        // what =2 only dirs

        if ((searchType == SearchType.All) | (searchType == SearchType.Files)) {
            find(file);
        }
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
        // searchtype=0 all searchtype=2 only dirs 1 only files
        if ((searchType == SearchType.All) | (searchType == SearchType.Directories)) {
            find(dir);
        }
        /*if ((this.searchType == 0) || (this.searchType == 2)) {
            //System.out.printf("Checking dir %s", dir.toString());
            find(dir);
        }*/
        return CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        //System.err.println(exc);
        return CONTINUE;
    }
}