package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class ItemStackBobbingAnimationS2C {
    public static Identifier PACKET_ID = new Identifier(BankStorage.MOD_ID, "bobbing");

    public static void send(ServerPlayerEntity player, int i){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(i);
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }
}
