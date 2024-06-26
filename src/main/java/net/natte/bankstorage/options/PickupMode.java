package net.natte.bankstorage.options;

public enum PickupMode {
    NONE,
    ALL,
    FILTERED,
    VOID;

    public PickupMode next() {
        return switch (this) {
            case NONE -> ALL;
            case ALL -> FILTERED;
            case FILTERED -> VOID;
            case VOID -> NONE;
        };
    }
}
