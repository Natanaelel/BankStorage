package net.natte.bankstorage.network;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;

public class OptionPackets {
    public static final Identifier S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "option_s2c");
    public static final Identifier SCROLL_C2S_PACKET_ID = new Identifier(BankStorage.MOD_ID, "scroll_c2s");
    public static final Identifier SCROLL_S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "scroll_s2c");

    public static final Identifier OPTIONS_S2C_PACKET_ID = new Identifier(BankStorage.MOD_ID, "options_s2c");

    public static void sendOptions(ServerPlayerEntity player, BankOptions options, UUID uuid) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeUuid(uuid);
        buf.writeNbt(options.asNbt());

        ServerPlayNetworking.send(player, OPTIONS_S2C_PACKET_ID, buf);
    }

    // public static void sendScrollPacketC2S(PlayerEntity player, UUID uuid, double scroll){
    //     PacketByteBuf buf = PacketByteBufs.create();
    //     buf.writeUuid(uuid);
    //     buf.writeDouble(scroll);
    //     ClientPlayNetworking.send(SCROLL_C2S_PACKET_ID, buf);

    // }

    // public static BankOptions readOptions(PacketByteBuf buf){

    // return BankOptions.fromNbt(buf.readNbt());
    // }

}
