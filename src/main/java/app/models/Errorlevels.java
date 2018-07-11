package app.models;

public enum Errorlevels {
    E0(0, "Update OK"),
    E1(1, "Error in update process"),
    E2(2, "No parameters entered"),
    E3(3, "updates repository doesn't exists"),
    E4(4, "Could not access or create backout directory"),
    E5(5, "Targetted update. No update repository entered."),
    E6(6, "Invalid command line parameter entered"),
    E7(7, "Invalid application selected"),
    E8(8, "Postgres server stopped"),
    E9(9, "Rollback error"),
    E10(10, "Malformed Tomcat configuration file"),
    E11(11, "Unsupported database system");

    private int errorLevel;
    private String errorDescription;

    Errorlevels(int errorLevel, String errorDescription) {
        this.errorLevel = errorLevel;
        this.errorDescription = errorDescription;
    }

    public int getErrorLevel() {
        return errorLevel;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
