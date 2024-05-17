package net.natte.bankstorage.command;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;

public class SortingModeArgumentType extends EnumArgumentType<SortingModeArgumentType.SortingMode> {

    protected SortingModeArgumentType() {
        super(SortingMode.CODEC, SortingMode::values);
    }

    public static SortingModeArgumentType sortingMode() {
        return new SortingModeArgumentType();
    }

    public static enum SortingMode implements StringIdentifiable {
        DATE("date"),
        TYPE("type"),
        PLAYER("player");

        private String name;
        public static final com.mojang.serialization.Codec<SortingMode> CODEC = StringIdentifiable
                .createCodec(SortingMode::values);

        SortingMode(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

    }

    public static SortingMode getSortingMode(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, SortingMode.class);

    }
}
