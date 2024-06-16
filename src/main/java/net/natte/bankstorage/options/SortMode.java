package net.natte.bankstorage.options;

public enum SortMode {
    COUNT,
    NAME,
    MOD;

    public SortMode next() {
        return switch (this) {
            case COUNT -> NAME;
            case NAME -> MOD;
            case MOD -> COUNT;
        };
    }
}
