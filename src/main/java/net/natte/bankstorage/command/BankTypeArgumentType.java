package net.natte.bankstorage.command;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;

public class BankTypeArgumentType extends EnumArgumentType<BankTypeArgumentType.BankType> {

    protected BankTypeArgumentType() {
        super(BankType.CODEC, BankType::values);
    }

    public static BankTypeArgumentType bankType() {
        return new BankTypeArgumentType();
    }

    public static enum BankType implements StringIdentifiable {
        BANK_1("bank_1"),
        BANK_2("bank_2"),
        BANK_3("bank_3"),
        BANK_4("bank_4"),
        BANK_5("bank_5"),
        BANK_6("bank_6"),
        BANK_7("bank_7");

        private String name;
        public static final com.mojang.serialization.Codec<BankType> CODEC = StringIdentifiable
                .createCodec(BankType::values);

        BankType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

    }

    public static BankType getBankType(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, BankType.class);

    }
}
