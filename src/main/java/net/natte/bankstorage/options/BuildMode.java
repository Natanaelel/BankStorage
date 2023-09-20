package net.natte.bankstorage.options;

public enum BuildMode {
    NONE(0),
    NORMAL(1),
    RANDOM(2);

    public byte number;

    BuildMode(int n) {
        number = (byte) n;
    }

    public static BuildMode from(byte n) {
        return switch (n) {
            case 0 -> NONE;
            case 1 -> NORMAL;
            case 2 -> RANDOM;
            default -> NONE;
        };
    }
    public static BuildMode from(int n){
        return BuildMode.from((byte)n);
    }
}