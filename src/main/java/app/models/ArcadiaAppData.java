package app.models;

import java.io.File;

public class ArcadiaAppData
{
    ArcadiaApps app;
    File installedDir;
    int portNumber;
    String version;

    public ArcadiaAppData(ArcadiaApps app, File installedDir, int portNumber, String version) {
        this.app = app;
        this.installedDir = installedDir;
        this.portNumber = portNumber;
        this.version = version;
    }

    public ArcadiaApps getApp() {
        return app;
    }

    public void setApp(ArcadiaApps app) {
        this.app = app;
    }

    public File getInstalledDir() {
        return installedDir;
    }

    public void setInstalledDir(File installedDir) {
        this.installedDir = installedDir;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ArcadiaAppData{" +
                "app=" + app +
                ", installedDir=" + installedDir +
                ", portNumber=" + portNumber +
                ", version='" + version + '\'' +
                '}';
    }
}
