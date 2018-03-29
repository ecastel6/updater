package app.controllers;

import app.core.Version;
import app.models.ArcadiaApp;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSystemCommonsTest
{

    @Test
    void getVersionFromDir() {
        String textVersion;
        textVersion = "3.12R2";
        //FileSystemCommons fsc=new FileSystemCommons();
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
        File updatesRepository = Paths.get(
                arcadiaController.getArcadiaUpdatesRepository().toString(),
                ArcadiaApp.CBOS.getShortName()).toFile();
        System.out.printf("UpdatesRepository: %s\n", updatesRepository);
        File[] updatesDirList = updatesRepository
                .listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        System.out.println("Unsorted");
        for (File f : updatesDirList)
            System.out.println(f);
        System.out.println("Sorted");
        FileSystemCommons fsc = new FileSystemCommons();
        for (File f : fsc.sortDirectoriesByVersion(updatesDirList))
            System.out.println(f);
    }

    @Test
    void normalizeVersion() {
        FileSystemCommons fsc = new FileSystemCommons();
        String rVersion = "3.12R4";
        String normalizedVersion = fsc.normalizeVersion(rVersion);
        Version version = new Version(normalizedVersion);

        System.out.printf("R version: %s Normalized: %s VersionToString: %s\n",
                rVersion,
                normalizedVersion,
                version.toString());
        assertEquals("3.12.2", fsc.normalizeVersion("3.12R2"));
    }
}