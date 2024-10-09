package net.natte.bankstorage.options;

public enum BuildMode {
    NONE_NORMAL(0),
    NONE_RANDOM(1),
    NORMAL(2),
    RANDOM(3);

    public byte number;

    BuildMode(int n) {
        number = (byte) n;
    }

    public static BuildMode from(byte n) {
        return switch (n) {
            case 0 -> NONE_NORMAL;
            case 1 -> NONE_RANDOM;
            case 2 -> NORMAL;
            case 3 -> RANDOM;
            default -> NONE_NORMAL;
        };
    }

    public static BuildMode from(int n) {
        return BuildMode.from((byte) n);
    }

    public BuildMode next() {
        return switch (this) {
            case NONE_NORMAL, NONE_RANDOM -> NORMAL;
            case NORMAL -> RANDOM;
            case RANDOM -> NONE_NORMAL;
        };
    }

    public BuildMode toggle() {
        return switch (this) {
            case NONE_NORMAL -> NORMAL;
            case NONE_RANDOM -> RANDOM;
            case NORMAL -> NONE_NORMAL;
            case RANDOM -> NONE_RANDOM;
        };
    }

    public BuildMode cycle() {
        return switch (this) {
            case NONE_NORMAL -> NONE_RANDOM;
            case NONE_RANDOM -> NONE_NORMAL;
            case NORMAL -> RANDOM;
            case RANDOM -> NORMAL;
        };
    }

    public boolean isActive() {
        return this == NORMAL || this == RANDOM;
    }
}
