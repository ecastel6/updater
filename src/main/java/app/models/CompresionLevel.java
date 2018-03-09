package app.models;

public enum CompresionLevel {
    UNCOMPRESSED(0),
    STANDARD(4),
    FULL(9);

    private int level;

    CompresionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
