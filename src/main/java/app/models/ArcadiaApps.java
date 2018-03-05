package app.models;

public enum ArcadiaApps
{
    // ID ("LongName","ShortName","database")
    CBOS("cbos", "cbos", "arcadia_cbos"),
    OPENCARD("opencard", "oc", "opencard"),
    ELLIOTT("elliott", "elliott", "elliott"),
    EVENTDETECT("EventDetect", "event", "eventdetect"),
    INTERFACES("interfaces", "interfaces", "interfaces");

    private final String longName;
    private final String shortName;
    private final String databaseName;

    ArcadiaApps(String longName, String shortName, String databaseName) {
        this.longName = longName;
        this.shortName = shortName;
        this.databaseName = databaseName;
    }

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String toString() {
        return "ArcadiaApps{" +
                "longName='" + longName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }
}
