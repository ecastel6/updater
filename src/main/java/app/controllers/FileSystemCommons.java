package app.controllers;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FileSystemCommons
{
    public File[] sortDirectoriesByDate(File[] listDirectories) {
        Arrays.sort(listDirectories, new Comparator<File>()
        {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
        return listDirectories;
    }

    public File[] sortDirectoriesByName(File[] listDirectories) {
        Arrays.sort(listDirectories, Collections.reverseOrder());
        return listDirectories;
    }

}
