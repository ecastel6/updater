package app.controllers;

import app.models.ReturnValues;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class ProcessController
{

    public String osType;

    public ProcessController() {
        this.osType = getOsType();
    }

    public String getOsType() {
        String sysOp = System.getProperty("os.name");
        if (sysOp.contains("Linux")) {
            return "Linux";
        } else {
            return "Windows";
        }
    }

    public ReturnValues runCommand(String command) {
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
                stdOut.append(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ReturnValues(exitStatus, stdOut.toString());
    }

    public ReturnValues serviceAction(String serviceName, String command) {
        String msg;
        ReturnValues returnedValues;
        switch (this.osType) {
            case "Linux":
                final List<String> availableActions = Arrays.asList("start", "stop", "restart", "status");
                if (!availableActions.contains(command))
                    return new ReturnValues(-1, "Invalid Action");
                switch (command) {
                    case "restart":
                        returnedValues = this.runCommand("service " + serviceName + " stop");
                        if (!returnedValues.u.equals(0)) return returnedValues;
                        returnedValues = this.runCommand("service " + serviceName + " start");
                        if (!returnedValues.u.equals(0)) return returnedValues;
                        return returnedValues;
                    default:
                        returnedValues = this.runCommand("service " + serviceName + " " + command);
                        return returnedValues;
                }

            case "Windows":
                System.out.println("Operating system is Wintendo");
                break;
        }
        return new ReturnValues(0, "Ok");
    }

    // TODO Check process exists
    public Boolean serviceAlive(String serviceName) {
        Process p;
        try {
            String line;
            if (this.osType == "Windows") {
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
        ProcessController sc = new ProcessController();
        /*ReturnValues rv = sc.serviceAction("apache2", "status");
        System.out.println("Return code: " + rv.t);
        System.out.println("stdOut: " + rv.u);
        //System.getProperties().list(System.out);*/
        System.out.println(sc.serviceAlive("tomcat"));
    }
}
