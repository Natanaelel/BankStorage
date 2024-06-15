package net.natte.bankstorage.packet.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LockSlotPacketC2S(int syncId, int slot, ItemStack stack,
                                boolean shouldLock) implements CustomPacketPayload {

    public static final Type<LockSlotPacketC2S> TYPE = new Type<>(Util.ID("lock_slot_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LockSlotPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            LockSlotPacketC2S::syncId,
            ByteBufCodecs.INT,
            LockSlotPacketC2S::slot,
            ItemStack.OPTIONAL_STREAM_CODEC,
            LockSlotPacketC2S::stack,
            ByteBufCodecs.BOOL,
            LockSlotPacketC2S::shouldLock,
            LockSlotPacketC2S::new);

    @Override
    public Type<LockSlotPacketC2S> type() {
        return TYPE;
    }

    public static void handle(LockSlotPacketC2S packet, IPayloadContext context) {
        System.out.println("handle LockSlotPacketC2S");
        ServerPlayer player = (ServerPlayer) context.player();
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (!(screenHandler instanceof BankScreenHandler bankScreenHandler)) return;

        if (packet.shouldLock) {
            bankScreenHandler.lockSlot(packet.slot, packet.stack);
        } else {
            bankScreenHandler.unlockSlot(packet.slot);
        }
        bankScreenHandler.lockedSlotsMarkDirty();
    }
}
