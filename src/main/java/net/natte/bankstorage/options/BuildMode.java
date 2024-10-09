package net.natte.bankstorage.options;

public enum BuildMode {
    NONE_NORMAL,
    NONE_RANDOM,
    NORMAL,
    RANDOM;

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
