package app.controllers;

import app.core.Version;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemCommonsTest
{

    @Test
    void getVersionFromDir() {
        String textVersion;
        textVersion = "3.12R2";
        //SystemCommons fsc=new SystemCommons();
        //System.out.println(fsc.getVersionFromDir(textVersion).toString());
        //System.out.println(new ComparableVersion("3.12R1").compareTo(new ComparableVersion("3.13")));


        /*for (String tag:fsc.getVersionFromDir(textVersion)) {
            System.out.println(tag);
        }
        textVersion="3.12";
        for (String tag:fsc.getVersionFromDir(textVersion)) {
            System.out.println(tag);
        }*/
    }

    @Test
    void sortDirectoriesByVersionTest() {
        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        File updatesRepository = arcadiaController.getArcadiaUpdatesRepository().toFile();
        System.out.printf("UpdatesRepository: %s\n", updatesRepository);
        File[] updatesDirList = updatesRepository
                .listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        System.out.println("Unsorted");
        for (File f : updatesDirList)
            System.out.println(f);
        System.out.println("Sorted");
        SystemCommons fsc = new SystemCommons();
        for (File f : fsc.sortDirectoriesByVersion(updatesDirList))
            System.out.println(f);
    }

    @Test
    void normalizeVersion() {
        SystemCommons fsc = new SystemCommons();
        String rVersion = "3.12R4";
        String normalizedVersion = fsc.normalizeVersion(rVersion);
        Version version = new Version(normalizedVersion);

        System.out.printf("R version: %s Normalized: %s VersionToString: %s\n",
                rVersion,
                normalizedVersion,
                version.toString());
        assertEquals("3.12.2", fsc.normalizeVersion("3.12R2"));
    }

    @Test
    void getTodayTest() {
        String today = new SystemCommons().getToday();
        System.out.println(today);
        assertTrue(Integer.parseInt(today.split("-")[0]) > 2000);
    }

    @Test
    void getAvailableCoresTest() {
        SystemCommons systemCommons = new SystemCommons();
        int availableCores = systemCommons.getAvailableCores();
        System.out.printf("Available cores: %s\n", availableCores);
        assertTrue(availableCores > 0);
    }

    @Test
    void stackTest() {
        Stack stack = new Stack();
        assertTrue(stack.empty());
        System.out.println("Stack push: rollbackArcadiaResources");
        stack.push("rollbackArcadiaResources");
        System.out.println("Stack push: rollbackLogBack");
        stack.push("rollbackLogBack");
        System.out.println("Stack push: rollbackSharedlib");
        stack.push("rollbackSharedlib");
        System.out.println("Stack push: rollbackWars");
        stack.push("rollbackWars");
        System.out.println("Stack push: rollbackCustom");
        stack.push("rollbackCustom");
        assertEquals(5, stack.size());
        while (!stack.empty()) {
            System.out.printf("Stack pops: %s\n", stack.pop());
        }
        assertTrue(stack.empty());
    }
}