package app.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileFinderController
        extends SimpleFileVisitor<Path>
{
    private final PathMatcher matcher;
    private double numMatches = 0;
    private int searchType;
    ArrayList<Path> results = new ArrayList<Path>();

    public FileFinderController(String pattern, int searchType) {
        matcher = FileSystems.getDefault().getPathMatcher("glob:{" + pattern + "}");
        this.searchType = searchType;
    }

    void find(Path file) {
        Path name = file.toAbsolutePath();//getFileName();
        System.out.println(name.toString());
        if (name != null && matcher.matches(name)) {
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
        /*if (attr.isSymbolicLink()) {
            System.out.format("Symbolic link: %s ", file);
        } else if (attr.isRegularFile()) {
            System.out.format("Regular file: %s ", file);
        } else {
            System.out.format("Other: %s ", file);
        }
        System.out.println("(" + attr.size() + "bytes)");*/

        // searchtype=0 all searchtype=1 only files
        if (this.searchType < 2) find(file);
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
        // searchtype=0 all searchtype=2 only dirs
        if ((this.searchType == 0) || (this.searchType == 2)) find(dir);
        //System.out.format("Directory: %s%n", dir);
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

    public static FileFinderController done(String startPath, String pattern, int searchType) {
        // what =0 all
        // what =1 files
        // what =2 dirs
        FileFinderController finder = new FileFinderController(pattern, searchType);
        try {
            Files.walkFileTree(Paths.get(startPath), finder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return finder;
    }
}