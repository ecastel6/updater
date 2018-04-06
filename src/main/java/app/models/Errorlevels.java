package app.models;

public enum Errorlevels {
    E1(1, "No parameters entered\n"),
    E2(2, "updates repository doesn't exists"),
    E3(3, "Could not access or create backout directory"),
    E4(4, "Targetted update. No update repository entered.");

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