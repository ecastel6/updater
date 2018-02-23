package app;

import app.controllers.Finder;

import java.nio.file.Path;

// todo rename dir
// todo move dir
// todo backup directory
// todo create directory
// todo delete directory
// todo copy file
// todo find directory
// todo directory size

public class ArcadiaUpdater {

    public static void main(String[] args) {

        Finder f = Finder.doit("/", "system*", 1);
        if (f.getNumMatches() > 0) {
            System.out.printf("Total matches %s", f.getNumMatches());
            for (Path p : f.getResults()) System.out.println(p);
        }

//        try {
//            ArrayList<Path> result = testFinder("/", "acpi*");
//            if (!result.isEmpty()) {
//                for (Path p : result) System.out.println(p);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

    }
}
