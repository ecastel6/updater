package app.models;

public enum ArcadiaApp {
    // ID ("LongName","ShortName","versionInfo","database","warjarwithversion")
    CBOS("cbos", "cbos", "cbos", "arcadia_cbos", "/sharedlib/cboscommons.jar"),
    OPENCARD("opencard", "oc", "openCard", "opencard", "/sharedlib/opencardcreditmodel.jar"),
    ELLIOTT("elliott", "elliott", "elliott", "elliott", "/webapps/elliott/WEB-INF/classes/version");
    /*
    EVENTDETECT("EventDetect", "event", "event", "eventdetect"),
    INTERFACES("interfaces", "interfaces", "interfaceMonitor", "interfaces");*/

    private final String longName;
    private final String shortName;
    private final String versionInfo;
    private final String databaseName;
    private final String warJarVersionFile;

    ArcadiaApp(String longName, String shortName, String versionInfo, String databaseName, String warJarVersionFile) {
        this.longName = longName;
        this.shortName = shortName;
        this.versionInfo = versionInfo;
        this.databaseName = databaseName;
        this.warJarVersionFile = warJarVersionFile;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public String getWarJarVersionFile() {
        return warJarVersionFile;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String toString() {
        return "ArcadiaApp{" +
                "longName='" + longName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", versionInfo='" + versionInfo + '\'' +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }
}
