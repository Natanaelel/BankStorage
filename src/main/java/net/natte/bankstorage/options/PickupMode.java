package net.natte.bankstorage.options;

public enum PickupMode {
    NONE(0),
    ALL(1),
    FILTERED(2),
    VOID(3);

    public byte number;

    PickupMode(int n) {
        number = (byte) n;
    }

    public static PickupMode from(byte n) {
        return switch (n) {
            case 0 -> NONE;
            case 1 -> ALL;
            case 2 -> FILTERED;
            case 3 -> VOID;
            default -> NONE;
        };
    }

    public static PickupMode from(int n) {
        return from((byte) n);
    }
}