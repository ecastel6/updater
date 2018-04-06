package app;

import app.controllers.ArcadiaController;
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
        CommandLine commandLine = null;
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption("S", "standalone", false, "Standalone run. Search local updates repository");
        options.addOption("F", "force", false, "Forced update. do not check servers for version");
        options.addOption(Option.builder("R").longOpt("repository").hasArg(true)
                .argName("repodir").desc("Update repository e.g. ./updates or /opt/arcadiaVersions")
                .required(false).build());
        options.addOption(Option.builder("t").longOpt("timeout").hasArg(true)
                .argName("time").desc("set stop tomcat services timeout")
                .required(false).build());
        options.addOption("s", "ignore-backups-size", false, "do not check backups size");
        options.addOption("b", "override-backups", false, "Do not make security backups");
        options.addOption("B", "override-backout", false, "Do not move old version to backout");
        options.addOption("n", "ignore-checkservices", false, "do not check services availability (Rabbitmq,Zookeeper).");
        try {
            // parse the command line arguments
            commandLine = parser.parse(options, args);

            // validate that block-size has been set
            if ((commandLine.hasOption("h") || (args.length == 0)) || (!commandLine.hasOption("S"))) {
                // display help
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("arcadia-updater", options);
                System.exit(1);
            }
            if (commandLine.hasOption("repository") && (!new File(commandLine.getOptionValue("repository")).exists())) {
                System.out.printf("%s %s\n", commandLine.getOptionValue("repository"), Errorlevels.E2.getErrorDescription());
                System.exit(Errorlevels.E2.getErrorLevel());
            }

        } catch (ParseException exp) {
            System.out.println(exp.getMessage());
        }

        ArcadiaController arcadiaController = ArcadiaController.getInstance();
        //System.out.printf("%s updates found.\n", arcadiaController.getInstalledApps().size());
        for (String update : arcadiaController.getAvailableUpdates(
                arcadiaController.getArcadiaUpdatesRepository(commandLine)).keySet()) {
            System.out.printf("Update: %s\n", update);
//        for (ArcadiaAppData arcadiaAppData : arcadiaController.getAvailableUpdates(
//                arcadiaController.getArcadiaUpdatesRepository(commandLine)).values()) {
//            System.out.printf("Update: %s\n", arcadiaAppData.getApp());
            /*
            boolean result;
            UpdateController updateController = new UpdateController(commandLine, arcadiaAppData);
            try {
                result = updateController.updateApp(arcadiaAppData);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }*/
        }
        System.exit(0);
    }
}
