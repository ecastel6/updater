package app.controllers;

import app.models.SearchType;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class DirFinder
{
    public static void main(String[] args) {
        // New code timing
        long startTime = System.currentTimeMillis();
        DirFinder df = new DirFinder();
        String[] multilevelDir = new String[]{"opt", "arcadiaVersions"};
        System.out.printf("Searching for %s\n", StringUtils.join(multilevelDir, File.separator));
        String result = df.findDir(new File("/"), multilevelDir);
        long endTime = System.currentTimeMillis();
        System.out.printf("Resultado: %s. Execution time %s\n", result, (endTime - startTime));

        // previous code timing
        startTime = System.currentTimeMillis();
        FileFinderController fileFinder = FileFinderController.doit("/", "arcadiaVersions", SearchType.Directories);
        endTime = System.currentTimeMillis();
        System.out.printf("Resultado: %s. Execution time %s\n", fileFinder.getResults().toString(), (endTime - startTime));
        System.exit(0);
    }

    private String findDir(File root, String[] dirNames) {
        if (root.getName().equals(dirNames[dirNames.length - 1])) {
            System.out.printf("Found %s\n", root.getAbsolutePath());
            if (root.getAbsolutePath().contains(StringUtils.join(dirNames, File.separator))) {
                return root.getAbsolutePath();
            }
        }

        File[] files = root.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    String myResult = findDir(f, dirNames);
                    //this just means this branch of the
                    //recursion reached the end of the
                    //directory tree without results, but
                    //we don't want to cut it short here,
                    //we still need to check the other
                    //directories, so continue the for loop
                    if (myResult != null) {
                        return myResult;
                    }
                }
            }
        }

        //we don't actually need to change this. It just means we reached
        //the end of the directory tree (there are no more sub-directories
        //in this directory) and didn't find the result
        return null;
    }

}
