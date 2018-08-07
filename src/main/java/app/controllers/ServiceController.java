package app.controllers;

import app.models.OS;
import app.models.ReturnValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ServiceController {
    private static LogController logController = LogController.getInstance();

    private static ServiceController ourInstance = new ServiceController();

    public static ServiceController getInstance() {
        return ourInstance;
    }

    public OS os;
    public ArrayList<String> driveList;

    private ServiceController() {
        this.os = getOs();
        this.driveList = getDriveList();
        logController.log.info(String.format("Detected OS: %s", osToString(os)));
    }

    private ArrayList<String> getDriveList() {
        ArrayList<String> driveList = new ArrayList<>();
        if (driveList.isEmpty()) {
            for (Path root : FileSystems.getDefault().getRootDirectories()) {
                if (Files.isWritable(root)) {
                    try {
                        FileStore fileStore = Files.getFileStore(root);
                        if ((!fileStore.isReadOnly()) && (!fileStore.getAttribute("volume:isRemovable").equals(true))) {
                            driveList.add(root.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return driveList;
    }

    public OS getOs() {
        String osName = System.getProperty("os.name");
        return osStringToEnum(osName);
    }

    public String osToString(OS os) {
        switch (os) {
            case LINUX:
                return "Linux";
            case WINDOWS:
                return "Windows";
            default:
                return "Other";
        }
    }

    public OS osStringToEnum(String osName) {
        try {
            if (osName.contains("Linux")) {
                return OS.LINUX;
            } else if (osName.contains("Windows")) {
                return OS.WINDOWS;
            } else if (osName == null) {
                throw new IOException("os.name not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OS.OTHER;
    }

    public ReturnValues runCommand(String[] command) {
        //StringBuffer stdOut = new StringBuffer();
        ArrayList<String> stdOut = new ArrayList<>();
        Process process;
        int exitStatus = 0;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            exitStatus = process.exitValue();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";

            while ((line = reader.readLine()) != null)
                stdOut.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ReturnValues(exitStatus, stdOut);
    }

    public ReturnValues serviceAction(String serviceName, String command) {
        String msg;
        logController.log.info(String.format("Servicename: %s Command: %s", serviceName, command));
        ReturnValues returnedValues = new ReturnValues(0, "");
        final List<String> availableActions = Arrays.asList("start", "stop", "forcestop", "restart", "status");
        if (!availableActions.contains(command))
            return new ReturnValues(-1, "Invalid Action");
        switch (os) {
            case LINUX:
                switch (command) {
                    case "restart":
                        returnedValues = this.runCommand(new String[]{"sudo", "/etc/init.d/", serviceName, "stop", "-force"});
                        returnedValues = this.runCommand(new String[]{"sudo", "/etc/init.d/", serviceName, "start"});
                        return returnedValues;
                    case "stop":
                        if (serviceName.startsWith("tomcat_")) {
                            returnedValues = this.runCommand(
                                    new String[]{"sudo", "/etc/init.d/" + serviceName, "stop", "-force"});
                        } else {
                            returnedValues = this.runCommand(new String[]{"sudo", "service", serviceName, command});
                        }
                        return returnedValues;
                    case "start":
                        if (serviceName.startsWith("tomcat_")) {
                            returnedValues = this.runCommand(
                                    new String[]{"sudo", "/etc/init.d/" + serviceName, "start"});
                        } else {
                            returnedValues = this.runCommand(new String[]{"sudo", "service", serviceName, command});
                        }
                        return returnedValues;
                    default:
                        returnedValues = this.runCommand(new String[]{"sudo", "service", serviceName, command});

                        return returnedValues;
                }

            case WINDOWS:
                String[] windowsScript;
                switch (command) {
                    case "status":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "query", serviceName, "|", "find", "/C", "\"RUNNING\""};
                        returnedValues = this.runCommand(windowsScript);
                        return returnedValues;
                    case "start":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "start", serviceName};
                        returnedValues = this.runCommand(windowsScript);
                        return returnedValues;
                    case "stop":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "stop", serviceName};
                        returnedValues = this.runCommand(windowsScript);
                        break;
                }
                break;
            case OTHER:
                logController.log.severe("Unhandled OS");
                break;
        }
        return returnedValues;
    }

    public Boolean serviceAlive(String serviceName) {
        Process p;
        try {
            String line;
            if (this.os.equals(OS.WINDOWS)) {
                p = Runtime.getRuntime().exec
                        (System.getenv("windir") + "\\system32\\" + "tasklist.exe /SVC");
            } else {
                if ((serviceName.contains("tomcat"))) {
                    String[] cmd = {"/bin/sh", "-c", "ps -ax | grep 'java' | grep -v 'grep'"};
                    p = Runtime.getRuntime().exec(cmd);
                    p.waitFor();
                } else {
                    p = Runtime.getRuntime().exec("ps -ax");
                }
            }
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.toLowerCase().contains(serviceName.toLowerCase())) return true;
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public String getUserHome() {
        // baseline java7. This method requires java8
        // return System.getProperty("user.home");
        Properties props = System.getProperties();
        return props.get("user.home").toString();
    }

    public String getAppdata() {
        return System.getenv("APPDATA");
    }


}
