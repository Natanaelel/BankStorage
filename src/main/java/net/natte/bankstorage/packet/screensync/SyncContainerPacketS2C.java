package net.natte.bankstorage.packet.screensync;

import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.util.Util;

public record SyncContainerPacketS2C(int stateId, int containerId, List<ItemStack> stacks, ItemStack carried) implements CustomPayload {

    public static final CustomPayload.Id<SyncContainerPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("sync_container"));
    public static final PacketCodec<RegistryByteBuf, SyncContainerPacketS2C> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        SyncContainerPacketS2C::stateId,
        PacketCodecs.INTEGER,
        SyncContainerPacketS2C::containerId,
        ItemStack.OPTIONAL_LIST_PACKET_CODEC,
        SyncContainerPacketS2C::stacks,
        ItemStack.OPTIONAL_PACKET_CODEC,
        SyncContainerPacketS2C::carried,
        SyncContainerPacketS2C::new
    );

    public static void sendSyncContainer(ServerPlayerEntity player, int stateID, int containerID,
            List<ItemStack> stacks, ItemStack carried) {
        ServerPlayNetworking.send(player, new SyncContainerPacketS2C(stateID, containerID, stacks, carried));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
    
}
