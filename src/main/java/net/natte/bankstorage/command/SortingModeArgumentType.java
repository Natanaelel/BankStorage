package net.natte.bankstorage.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;

public class SortingModeArgumentType extends StringRepresentableArgument<SortingModeArgumentType.SortingMode> {

    protected SortingModeArgumentType() {
        super(SortingMode.CODEC, SortingMode::values);
    }

    public static SortingModeArgumentType sortingMode() {
        return new SortingModeArgumentType();
    }

    public static enum SortingMode implements StringRepresentable {
        DATE("date"),
        TYPE("type"),
        PLAYER("player");

        private final String name;
        public static final com.mojang.serialization.Codec<SortingMode> CODEC = StringRepresentable
                .fromEnum(SortingMode::values);

        SortingMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

    }

    public static SortingMode getSortingMode(CommandContext<CommandSourceStack> context, String id) {
        return context.getArgument(id, SortingMode.class);

    }
}
