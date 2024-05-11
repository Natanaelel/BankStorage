package net.natte.bankstorage.packet.screensync;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.util.Util;

public record SyncLargeSlotPacketS2C(int id, int slot, ItemStack itemStack) implements CustomPayload {

    public static final CustomPayload.Id<SyncLargeSlotPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("sync_slot"));
    public static final PacketCodec<RegistryByteBuf, SyncLargeSlotPacketS2C> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        SyncLargeSlotPacketS2C::id,
        PacketCodecs.INTEGER,
        SyncLargeSlotPacketS2C::slot,
        ItemStack.OPTIONAL_PACKET_CODEC,
        SyncLargeSlotPacketS2C::itemStack,
        SyncLargeSlotPacketS2C::new);
    
    
    public static void sendSyncSlot(ServerPlayerEntity player, int id, int slot, ItemStack stack) {
        ServerPlayNetworking.send(player, new SyncLargeSlotPacketS2C(id, slot, stack));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
    
}
