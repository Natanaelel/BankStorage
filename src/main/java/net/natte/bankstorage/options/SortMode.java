package net.natte.bankstorage.options;

public enum SortMode {
    COUNT(0),
    NAME(1),
    MOD(2);

    public byte number;

    private SortMode(int number) {
        this.number = (byte) number;
    }

    public static SortMode from(byte number) {
        return switch (number) {
            case 0 -> COUNT;
            case 1 -> NAME;
            case 2 -> MOD;
            default -> COUNT;
        };
    }

}
