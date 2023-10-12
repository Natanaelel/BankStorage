package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class LockSlotPacketC2S implements FabricPacket {

    public static final PacketType<LockSlotPacketC2S> TYPE = PacketType
            .create(Util.ID("lock_slot_c2s"), LockSlotPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<LockSlotPacketC2S> {

        @Override
        public void receive(LockSlotPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
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

    public int syncId;
    public int slot;
    public ItemStack stack;
    public boolean shouldLock;

    public LockSlotPacketC2S(int syncId, int slot, ItemStack stack, boolean shouldLock) {
        this.syncId = syncId;
        this.slot = slot;
        this.stack = stack.copyWithCount(1);
        this.shouldLock = shouldLock;
    }

    public LockSlotPacketC2S(PacketByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readItemStack(), buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(slot);
        buf.writeItemStack(stack);
        buf.writeBoolean(shouldLock);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
