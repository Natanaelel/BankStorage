package net.natte.bankstorage.options;

public enum BuildMode {
    NONE,
    NORMAL,
    RANDOM;

    public BuildMode next() {
        return switch (this) {
            case NONE -> NORMAL;
            case NORMAL -> RANDOM;
            case RANDOM -> NONE;
        };
    }
}
