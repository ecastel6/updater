package app;

import app.controllers.ArcadiaController;
import app.controllers.LogController;
import app.controllers.UpdateController;
import app.core.Version;
import app.models.ArcadiaAppData;
import app.models.Errorlevels;
import org.apache.commons.cli.*;
import org.apache.commons.collections.CollectionUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArcadiaUpdater {
    private static LogController logController = LogController.getInstance();

    private static Map<String, ArcadiaAppData> testInstalledApps = new HashMap<>();
    private static ArcadiaController arcadiaController = ArcadiaController.getInstance();

    public ArcadiaUpdater() {
    }

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
        options.addOption("B", "force-backout", false, "Cleanout backout directory before backout");
        options.addOption("n", "ignore-checkservices", false, "do not check services availability (Rabbitmq,Zookeeper).");
        try {
            // parse the command line arguments
            commandLine = parser.parse(options, args);

            // validate that block-size has been set
            if ((commandLine.hasOption("h") || (args.length == 0)) && (!commandLine.hasOption("S"))) {
                // display help
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("arcadia-updater", options);
                System.exit(Errorlevels.E1.getErrorLevel());
            }
            if (commandLine.hasOption("repository")) {
                if (!Files.isDirectory(Paths.get(commandLine.getOptionValue("repository")))) {
                    logController.log.severe(String.format("Path %s %s", commandLine.getOptionValue("repository"), Errorlevels.E2.getErrorDescription()));
                    System.exit(Errorlevels.E2.getErrorLevel());
                } else {
                    arcadiaController.setArcadiaUpdatesRepository(Paths.get(commandLine.getOptionValue("repository")));
                }
            }
        } catch (ParseException e) {
            logController.log.severe(e.getMessage());
            System.exit(1);
        }


        arcadiaController.setCommandLine(commandLine);
        Map<String, ArcadiaAppData> availableUpdates = arcadiaController.getAvailableUpdates();
        logController.log.info(String.format("Found %s updates", availableUpdates.size()));
        for (Map.Entry<String, ArcadiaAppData> app : availableUpdates.entrySet()) {
            logController.log.info(String.format("updates found. %s version: %s", app.getKey(), app.getValue().getVersion()));
        }

        Map<String, ArcadiaAppData> installedApps = arcadiaController.getInstalledApps();
        for (Map.Entry<String, ArcadiaAppData> app : installedApps.entrySet()) {
            logController.log.info(String.format("Installed apps found: %s version: %s", app.getKey(), app.getValue().getVersion()));
        }

        logController.log.info(String.format("updates keys : %s install keys: %s", availableUpdates.keySet(), installedApps.keySet()));
        Collection intersection = CollectionUtils.intersection(availableUpdates.keySet(), installedApps.keySet());
        for (Object appName : intersection) {
            Version installedVersion = installedApps.get(appName).getVersion();
            Version updateVersion = availableUpdates.get(appName).getVersion();
            logController.log.info(String.format("Checking %s Update version %s with installed app %s",
                    appName,
                    updateVersion,
                    installedVersion));
            if (installedVersion == null && !commandLine.hasOption("F")) {
                logController.log.severe(String.format("ERROR: unable to update %s. Installed version not available. Use -F (--force) to force updating.", appName));
            } else if ((updateVersion.compareTo(installedVersion) > 0) || commandLine.hasOption("F")) {
                logController.log.info(String.format("OK: Updating %s to version %s", appName, updateVersion));
                UpdateController updateController = new UpdateController((String) appName);
                try {
                    boolean result = updateController.updateApp();
                } catch (RuntimeException e) {
                    logController.log.severe(e.getMessage());
                    System.exit(1);
                }
            } else logController.log.info(String.format("WARNING: Application %s already up to date", appName));
        }
        System.exit(0);
    }
}
