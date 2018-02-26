package app.controllers;

import app.models.OS;
import app.models.ReturnValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class ServiceController {


    public OS os;

    public ServiceController() {
        this.os = getOs();
    }

    public OS getOs() {
        String osName = System.getProperty("os.name");
        return osStringToEnum(osName);

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
        StringBuffer stdOut = new StringBuffer();
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
                stdOut.append(line);// + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ReturnValues(exitStatus, stdOut.toString());
    }

    public ReturnValues serviceAction(String serviceName, String command) {
        String msg;
        ReturnValues returnedValues = new ReturnValues(0, "");
        final List<String> availableActions = Arrays.asList("start", "stop", "restart", "status");
        if (!availableActions.contains(command))
            return new ReturnValues(-1, "Invalid Action");
        switch (this.os) {
            case LINUX:
                switch (command) {
                    case "restart":
                        returnedValues = this.runCommand(new String[]{"service", serviceName, "stop"});
                        returnedValues = this.runCommand(new String[]{"service", serviceName, "start"});
                        break;
                    default:
                        returnedValues = this.runCommand(new String[]{"service", serviceName, command});
                        break;
                }

            case WINDOWS:
                String[] windowsScript;
                switch (command) {
                    case "status":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "query", serviceName, "|", "find", "/C", "\"RUNNING\""};
                        returnedValues = this.runCommand(windowsScript);
                        break;
                    case "start":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "start", serviceName};
                        returnedValues = this.runCommand(windowsScript);
                        break;
                    case "stop":
                        windowsScript = new String[]{"cmd.exe", "/c", "sc", "stop", serviceName};
                        returnedValues = this.runCommand(windowsScript);
                        break;
                }
                break;
            case OTHER:
                System.out.println("Unhandled OS");
                break;
        }
        return returnedValues;
    }

    // TODO Check process exists
    public Boolean serviceAlive(String serviceName) {
        Process p;
        try {
            String line;
            if (this.os.equals(OS.WINDOWS)) {
                p = Runtime.getRuntime().exec
                        (System.getenv("windir") + "\\system32\\" + "tasklist.exe /SVC");
            } else {
                p = Runtime.getRuntime().exec("ps -ef");
            }
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains(serviceName)) return true;
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        ServiceController sc = new ServiceController();
        /*ReturnValues rv = sc.serviceAction("apache2", "status");
        System.out.println("Return code: " + rv.t);
        System.out.println("stdOut: " + rv.u);
        //System.getProperties().list(System.out);*/
        System.out.println(sc.serviceAlive("tomcat"));
    }
}
