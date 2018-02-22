package app;

import app.controllers.Finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ArcadiaUpdater {

    public static ArrayList<Path> testFinder(String sd, String pat) throws IOException {
        Finder finder = new Finder(pat);
        Files.walkFileTree(Paths.get(sd), finder);
        return finder.getResults();
    }

    public static void main(String[] args) {
        try {
            ArrayList<Path> result = testFinder("/", "acpi*");
            if (!result.isEmpty()) {
                for (Path p : result) System.out.println(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

    }
}
