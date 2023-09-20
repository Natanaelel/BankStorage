package net.natte.bankstorage.network;

import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class OptionPacket {
    public static final Identifier S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "option_s2c");
    // public static final Identifier C2S_PACKET_ID = new Identifier(BankStorage.MOD_ID, "option_c2s");
}
