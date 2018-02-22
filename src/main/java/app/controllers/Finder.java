package app.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static java.nio.file.FileVisitResult.CONTINUE;

public class Finder
        extends SimpleFileVisitor<Path>
{
    private final PathMatcher matcher;
    private double numMatches = 0;

    ArrayList<Path> results = new ArrayList<Path>();

    public Finder(String pattern) {
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    void find(Path file) {
        Path name = file.getFileName();
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
        find(file);
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
        find(dir);
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
        System.err.println(exc);
        return CONTINUE;
    }

    public static void main(String[] args) throws IOException {
        String startDir = "/";
        String pattern = "acp*";

        Finder finder = new Finder(pattern);
        Files.walkFileTree(Paths.get(startDir), finder);
        if (finder.getNumMatches() > 0) {
            for (Path path : finder.getResults()) {
                System.out.println(path);
            }

        }

    }
}