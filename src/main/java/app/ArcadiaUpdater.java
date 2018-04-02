package app;

import app.controllers.ArcadiaController;
import app.controllers.UpdateController;
import app.core.UpdateException;
import app.models.ArcadiaAppData;
import app.models.Errorlevels;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ArcadiaUpdater {

    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private ArcadiaController arcadiaController = ArcadiaController.getInstance();

    public static void main(String[] args) {
        // create the command line parser
        CommandLine line = null;
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();

        Option s = Option.builder("S").longOpt("standalone")
                .hasArg(false).desc("standalone legacy update.").build();
        Option t = Option.builder("T").longOpt("targetted")
                .hasArg(false).desc("targetted update.").build();
        OptionGroup executionMode = new OptionGroup();
        executionMode.setRequired(false);
        executionMode.addOption(s);
        executionMode.addOption(t);
        options.addOptionGroup(executionMode);

        options.addOption("F", "force", false, "do not check servers for version. Force update.");
        options.addOption(Option.builder("R").longOpt("repository").hasArg(true)
                .argName("repodir").desc("Update repository e.g. ./updates or /opt/arcadiaVersions")
                .required(false).build());
        options.addOption("b", "ignore-backupsize", false, "do not check backups size");
        options.addOption("B", "ignore-backout", false, "do not preserve old version to backout.");
        try {
            // parse the command line arguments
            line = parser.parse(options, args);

            // validate that block-size has been set
            if ((line.hasOption("h") || (args.length == 0)) || (!line.hasOption("S") && !line.hasOption("T"))) {
                // display help
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("arcadia-updater", options);
                System.exit(1);
            }
            if (line.hasOption("repository") && (!new File(line.getOptionValue("repository")).exists())) {
                System.out.printf("%s %s\n", line.getOptionValue("repository"), Errorlevels.E2.getErrorDescription());
                System.exit(Errorlevels.E2.getErrorLevel());
            }
        } catch (ParseException exp) {
            System.out.println(exp.getMessage());
        }

        if (line.hasOption("S")) {
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
            System.exit(0);
        }
        if (line.hasOption("T") && !line.hasOption("repository")) {
            System.out.println(Errorlevels.E4.getErrorDescription());
            System.exit(Errorlevels.E4.getErrorLevel());
        }

    }
}
