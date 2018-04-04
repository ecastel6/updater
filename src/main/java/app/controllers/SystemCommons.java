package app.controllers;

import app.core.Version;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class SystemCommons {

    public static void endstuff() {
        int min = 0, max = 20;

        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        System.out.printf("Check stopped. Random: %s\n", randomNum);
    }


    public static void timedExecution(long timeout, String service) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int count = 0;
        while (true) {
            if (!ServiceController.getInstance().serviceAlive(service)) {
                System.out.println("Service stoped");
                break;
            }
            System.out.printf("Check %s\n", count++);
            sleep(1000);
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeout)
                throw new RuntimeException("timeout");
        }
    }

    public static void main(String[] args) {
        ServiceController.getInstance().serviceAction("tomcat_cbos", "stop");
        try {
            timedExecution(20000, "tomcat_cbos");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public File[] sortDirectoriesByDate(File[] listDirectories) {
        Arrays.sort(listDirectories, new Comparator<File>() {
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

    /*
        Converts dir filename R Version
        to normalized ArrayList X.Y.Z (major.minor.patch)
     */
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

    /*
    Converts R version standard X.Y.Z (major,minor,patch)
     */
    public String normalizeVersion(String arcadiaVersion) {
        return arcadiaVersion.replace("R", ".");
    }

    public File[] sortDirectoriesByVersion(File[] listDirectories) {
        Arrays.sort(listDirectories, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return new Version(normalizeVersion(f2.getName()))
                        .compareTo(new Version(normalizeVersion(f1.getName())));
            }
        });
        return listDirectories;
    }

    public String getToday() {
        String today = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm.EEEE");
        Date now = new Date();
        today = simpleDateFormat.format(now);
        return today;
    }
}

