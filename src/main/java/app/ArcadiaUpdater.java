package app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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

    public static void main(String[] args) throws IOException {
        ArrayList<Path> driveList = null;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            if (Files.isWritable(root)) {
                FileStore fileStore = Files.getFileStore(root);
                if ((!fileStore.isReadOnly()) && (!fileStore.getAttribute("volume:isRemovable").equals(true))) {
                    driveList.add(root);
                }
            }
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
