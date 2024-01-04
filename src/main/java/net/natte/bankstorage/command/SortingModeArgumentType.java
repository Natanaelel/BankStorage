package net.natte.bankstorage.command;


import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

public class SortingModeArgumentType extends EnumArgumentType<SortingMode> {

    private SortingModeArgumentType() {
        super(SortingMode.CODEC, SortingMode::values);
    }

    public static SortingModeArgumentType sortingMode() {
        return new SortingModeArgumentType();
    }

    
}

enum SortingMode implements StringIdentifiable {
    BANK_TIER("bank_tier"),
    PLAYER("player"),
    LAST_USED("last_used");

    public static final com.mojang.serialization.Codec<SortingMode> CODEC;
    private String name;

    SortingMode(String name){
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    static {
        CODEC = StringIdentifiable.createCodec(SortingMode::values);
    }
}