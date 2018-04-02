package app;

import app.controllers.ArcadiaController;
import app.controllers.UpdateController;
import app.core.UpdateException;
import app.models.ArcadiaAppData;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArcadiaUpdater
{

    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();

    public static void main(String[] args) {
        // create the command line parser
        CommandLine line = null;
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("a", "auto", false, "automated update.");
        options.addOption("F", "force", false, "do not check servers for version. Force update.");

        options.addOption(Option.builder("R").longOpt("repository").hasArg(true).argName("repodir").required(false).build());
        options.addOption("B", "ignore-backupsize", false, "do not check backups size");

        try {
            // parse the command line arguments
            line = parser.parse(options, args);

            // validate that block-size has been set
            if (line.hasOption("h") || (args.length == 0)) {
                // display help
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("updater", options);
                System.exit(0);
            }
            if (line.hasOption("repository") && (!new File(line.getOptionValue("repository")).exists())) {
                System.out.printf("updates repository %s doesn't exists", line.getOptionValue("repository"));
                System.exit(-1);
            }
        } catch (ParseException exp) {
            System.out.println(exp.getMessage());
        }


        if (line.hasOption("a")) {
            ArcadiaController arcadiaController = ArcadiaController.getInstance();
            arcadiaController.getInstalledApps();
            System.out.printf("%s valid app targets\n", arcadiaController.installedApps.size());
            for (ArcadiaAppData arcadiaAppData : arcadiaController.installedApps.values()) {
                System.out.printf("Updating %s ...\n", arcadiaAppData.toString());
                boolean result;
                UpdateController updateController = new UpdateController(line, arcadiaAppData);
                try {
                    result = updateController.updateApp(arcadiaAppData);
                } catch (UpdateException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        System.exit(0);
    }
}
