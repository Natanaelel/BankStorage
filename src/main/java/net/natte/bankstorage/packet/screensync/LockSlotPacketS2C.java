package net.natte.bankstorage.packet.screensync;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class LockSlotPacketS2C implements FabricPacket {

    public static final PacketType<LockSlotPacketS2C> TYPE = PacketType
            .create(Util.ID("lock_slot_s2c"), LockSlotPacketS2C::new);

    public int syncId;
    public int slot;
    public ItemStack stack;
    public boolean shouldLock;

    public LockSlotPacketS2C(int syncId, int slot, ItemStack stack, boolean shouldLock) {
        this.syncId = syncId;
        this.slot = slot;
        this.stack = stack;
        this.shouldLock = shouldLock;
    }

    public LockSlotPacketS2C(PacketByteBuf buf) {
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
