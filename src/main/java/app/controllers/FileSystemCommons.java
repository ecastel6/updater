package app.controllers;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public List<String> getVersionFromDir(String dir) {
        List<String> version = new ArrayList<>();
        //Pattern pattern = Pattern.compile("^(\\d*?)\\.(\\d*?)R?(\\d*?)$");
        Pattern pattern = Pattern.compile("^(\\d*?)\\.(\\d*)R*(\\d*)$");

        Matcher matcher = pattern.matcher(dir);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++)
                version.add(matcher.group(i));
        }
        return version;
    }

    public File[] sortDirectoriesByVersion(File[] listDirectories) {
        //System.out.println(new ComparableVersion("3.12R1").compareTo(new ComparableVersion("3.13")));
        /*Arrays.sort(listDirectories, new Comparator<File>()
        {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
        return listDirectories;*/
        return null;
    }

}