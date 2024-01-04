
package net.natte.bankstorage.command;


import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

public class FilterArgumentType extends EnumArgumentType<Filter> {

    private FilterArgumentType() {
        super(Filter.CODEC, Filter::values);
    }

    public static FilterArgumentType sortingMode() {
        return new FilterArgumentType();
    }

    
}

enum Filter implements StringIdentifiable {
    BANK_TIER("bank_tier"),
    PLAYER("player"),
    LAST_USED("last_used");

    public static final com.mojang.serialization.Codec<Filter> CODEC;
    private String name;

    Filter(String name){
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    static {
        CODEC = StringIdentifiable.createCodec(Filter::values);
    }
}