package app.models;

public enum ArcadiaApp {
    // ID ("LongName","ShortName","versionInfo","database")
    CBOS("cbos", "cbos", "cbos", "arcadia_cbos"),
    OPENCARD("opencard", "oc", "openCard", "opencard"),
    ELLIOTT("elliott", "elliott", "elliott", "elliott"),
    EVENTDETECT("EventDetect", "event", "event", "eventdetect"),
    INTERFACES("interfaces", "interfaces", "interfaceMonitor", "interfaces");

    private final String longName;
    private final String shortName;
    private final String versionInfo;
    private final String databaseName;

    ArcadiaApp(String longName, String shortName, String versionInfo, String databaseName) {
        this.longName = longName;
        this.shortName = shortName;
        this.versionInfo = versionInfo;
        this.databaseName = databaseName;
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
