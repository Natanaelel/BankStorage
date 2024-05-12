package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public record LockSlotPacketC2S(int syncId, int slot, ItemStack stack, boolean shouldLock) implements CustomPayload {

    public static final CustomPayload.Id<LockSlotPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("lock_slot_c2s"));
    public static final PacketCodec<RegistryByteBuf, LockSlotPacketC2S> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            LockSlotPacketC2S::syncId,
            PacketCodecs.INTEGER,
            LockSlotPacketC2S::slot,
            ItemStack.OPTIONAL_PACKET_CODEC,
            LockSlotPacketC2S::stack,
            PacketCodecs.BOOL,
            LockSlotPacketC2S::shouldLock,
            LockSlotPacketC2S::new);

    public static class Receiver implements PlayPayloadHandler<LockSlotPacketC2S> {

        @Override
        public void receive(LockSlotPacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
                return;

            if (packet.shouldLock) {
                bankScreenHandler.lockSlot(packet.slot, packet.stack);
            } else {
                bankScreenHandler.unlockSlot(packet.slot);
            }
            bankScreenHandler.lockedSlotsMarkDirty();
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
