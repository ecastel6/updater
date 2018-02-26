package app;

import app.controllers.DbController;

import java.io.IOException;

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
        try {
            DbController dbController = DbController.getInstance();
            System.out.printf("DB dir : %s\n", dbController.getServerDir());
        } catch (IOException e) {
            e.printStackTrace();
        }

            /*FileFinderController f = FileFinderController.done("/tmp", "data", 2);
        if (f.getNumMatches() > 0) {
            System.out.printf("Total matches %s\n", f.getNumMatches());
            for (Path p : f.getResults()) System.out.println(p);
        } else System.out.println("No results found");
*/
        /*if ((args.length < 1) || (args.length > 3)) {
            System.out.println(
                    "Two or three parameters required. \n Usage: merger ActualConfig.properties NewConfig.properties <dry>");
        } else {

        }*/

    }
}
