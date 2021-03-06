package app.models;

import app.core.Version;

import java.io.File;

public class ArcadiaAppData
{
    ArcadiaApp app;
    File directory;
    String portNumber;
    Version version;

    public ArcadiaAppData(ArcadiaApp app, File directory, String portNumber, Version version) {
        this.app = app;
        this.directory = directory;
        this.portNumber = portNumber;
        this.version = version;
    }

    public ArcadiaAppData() {
    }

    public ArcadiaApp getApp() {
        return app;
    }

    public void setApp(ArcadiaApp app) {
        this.app = app;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ArcadiaAppData{" +
                "app=" + app +
                ", directory=" + directory +
                ", portNumber=" + portNumber +
                ", version='" + version + '\'' +
                '}';
    }
}
