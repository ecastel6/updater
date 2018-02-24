package app;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

// todo rename dir
// todo move dir
// todo backup directory
// todo create directory
// todo delete directory
// todo copy file
// todo find directory
// todo directory size

public class ArcadiaUpdater
{

    public static void main(String[] args) {


        /*Iterable<Path> p=FileSystems.getDefault().getRootDirectories();
        for (Path path:p) {
            System.out.println(path.toString());
        }*/
        File[] paths;
        FileSystemView fsv = FileSystemView.getFileSystemView();

        // returns pathnames for files and directory
        paths = File.listRoots();

        // for each pathname in pathname array
        for (File path : paths) {
            // prints file and directory paths
            System.out.println("Drive Name: " + path);
            System.out.println("Description: " + fsv.getSystemTypeDescription(path));
        }


//        FileFinderController f = FileFinderController.done("/var", "lib/mysql", 2);
//        if (f.getNumMatches() > 0) {
//            System.out.printf("Total matches %s\n", f.getNumMatches());
//            for (Path p : f.getResults()) System.out.println(p);
//        } else System.out.println("No results found");

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
