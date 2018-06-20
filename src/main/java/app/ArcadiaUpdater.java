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
import java.util.*;

public class ArcadiaUpdater
{
    private static LogController logController = LogController.getInstance();

    //private static Map<String, ArcadiaAppData> installedApps = new HashMap<>();
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
                .argName("time").desc("set stop tomcat services timeout (ms)")
                .required(false).build());
        options.addOption("s", "ignore-backups-size", false, "do not check backups size");
        options.addOption("b", "override-backups", false, "Don't do security backups");
        options.addOption("B", "force-backout", false, "Cleanout backout directory before backout");
        options.addOption("n", "ignore-checkservices", false, "do not check services availability (Rabbitmq,Zookeeper).");
        options.addOption("r", "reinstall-services", false, "reinstall tomcat services.");

        /*Option apps=new Option("A",true,"Apps to install e.g -A oc cbos");
        apps.setArgs(Option.UNLIMITED_VALUES);
        apps.setLongOpt("apps");
        apps.setRequired(false);
        options.addOption(apps);
*/
        options.addOption(Option.builder("A").longOpt("apps").hasArg(true)
                .argName("apps").desc("Apps to install e.g. --apps oc cbos").numberOfArgs(Option.UNLIMITED_VALUES)
                .required(false).build());

        List<String> selectedApps = null;
        try {
            // parse the command line arguments
            commandLine = parser.parse(options, args);

            // validate that block-size has been set
            if (commandLine.hasOption("h") || args.length == 0 || (!commandLine.hasOption("S") && !commandLine.hasOption("R"))) {
                // display help
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("arcadia-updater", options);
                System.exit(Errorlevels.E2.getErrorLevel());
            }
            if (commandLine.hasOption("repository")) {
                if (!Files.isDirectory(Paths.get(commandLine.getOptionValue("repository")))) {
                    logController.log.severe(String.format("Path %s %s", commandLine.getOptionValue("repository"), Errorlevels.E2.getErrorDescription()));
                    System.exit(Errorlevels.E3.getErrorLevel());
                } else {
                    arcadiaController.setArcadiaUpdatesRepository(Paths.get(commandLine.getOptionValue("repository")));
                }
            }
            if (commandLine.hasOption("A")) {
                selectedApps = Arrays.asList(commandLine.getOptionValues("A"));
                List<String> validArcadiaApps = arcadiaController.validArcadiaApps();
                if (!validArcadiaApps.containsAll(selectedApps)) {
                    logController.log.severe(String.format("%s %s", Errorlevels.E7.getErrorDescription(), selectedApps));
                    System.exit(Errorlevels.E7.getErrorLevel());
                } else {
                    logController.log.warning(String.format("Selected to update %s", selectedApps));
                }
            } else selectedApps = arcadiaController.validArcadiaApps();
        } catch (ParseException e) {
            logController.log.severe(String.format("%s : %s", Errorlevels.E6.getErrorDescription(), e.getMessage()));
            System.exit(Errorlevels.E6.getErrorLevel());
        }

        arcadiaController.setCommandLine(commandLine);
        Map<String, ArcadiaAppData> availableUpdates = arcadiaController.getAvailableUpdates();
        logController.log.info(String.format("Found %s app update folders", availableUpdates.size()));
        for (Map.Entry<String, ArcadiaAppData> app : availableUpdates.entrySet()) {
            logController.log.info(String.format("Update found. %s version: %s", app.getKey(), app.getValue().getVersion()));
        }

        Map<String, ArcadiaAppData> installedApps = arcadiaController.getInstalledApps();
        for (Map.Entry<String, ArcadiaAppData> app : installedApps.entrySet()) {
            logController.log.info(String.format("Installed apps found: %s version: %s", app.getKey(), app.getValue().getVersion()));
        }

        Collection intersection;
        if (selectedApps.size() > 0) {
            ListIterator listIterator = selectedApps.listIterator();
            String appCapitalized;
            while (listIterator.hasNext()) {
                appCapitalized = listIterator.next().toString().toUpperCase();
                listIterator.set(appCapitalized);
            }
            logController.log.info(String.format("Selected applications to update: %s", selectedApps.toString()));
            intersection = CollectionUtils.intersection(availableUpdates.keySet(), selectedApps);
            intersection = CollectionUtils.intersection(intersection, installedApps.keySet());
            logController.log.info(String.format("updates apps : %s installed apps: %s selected apps: %s Intersection: %s",
                    availableUpdates.keySet(), installedApps.keySet(), selectedApps, intersection));
        } else {
            intersection = CollectionUtils.intersection(availableUpdates.keySet(), installedApps.keySet());
            logController.log.info(String.format("updates apps : %s installed apps: %s intersection: %s", availableUpdates.keySet(), installedApps.keySet(), intersection));
        }
        for (Object appName : intersection) {
            Version installedVersion = installedApps.get(appName).getVersion();
            Version updateVersion = availableUpdates.get(appName).getVersion();
            logController.log.info(String.format("Checking %s Update version %s with installed app %s",
                    appName,
                    updateVersion,
                    installedVersion));
            if (installedVersion == null && !commandLine.hasOption("F")) {
                logController.log.warning(String.format("unable to update %s. Installed version not available. Use -F (--force) to force updating.", appName));
            } else if ((updateVersion.compareTo(installedVersion) > 0) || commandLine.hasOption("F")) {
                logController.log.warning(String.format("Updating %s to version %s", appName, updateVersion));

                UpdateController updateController = new UpdateController((String) appName);
                try {
                    boolean result = updateController.updateApp();
                } catch (RuntimeException e) {
                    logController.log.severe(e.getMessage());
                    System.exit(1);
                }
            } else logController.log.warning(String.format("Application %s already up to date", appName));
        }
        System.exit(Errorlevels.E0.getErrorLevel());
    }
}
