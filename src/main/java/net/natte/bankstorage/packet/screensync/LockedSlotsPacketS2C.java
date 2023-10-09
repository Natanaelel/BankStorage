package net.natte.bankstorage.packet.screensync;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.natte.bankstorage.util.Util;

public class LockedSlotsPacketS2C implements FabricPacket {

    public static final PacketType<LockedSlotsPacketS2C> TYPE = PacketType
            .create(Util.ID("sync_locked_slots_s2c"), LockedSlotsPacketS2C::new);

    public int syncId;
    public Map<Integer, ItemStack> lockedSlots;

    public LockedSlotsPacketS2C(int syncId, Map<Integer, ItemStack> lockedSlots) {
        this.syncId = syncId;
        this.lockedSlots = lockedSlots;
    }

    public LockedSlotsPacketS2C(PacketByteBuf buf) {
        this(buf.readInt(), LockedSlotsPacketS2C.readLockedSlots(buf));
    }

    private static Map<Integer, ItemStack> readLockedSlots(PacketByteBuf buf) {
        Map<Integer, ItemStack> lockedSlots = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0; i < size; ++i){
            lockedSlots.put(buf.readInt(), buf.readItemStack());
        }
        return lockedSlots;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.syncId);
        buf.writeInt(this.lockedSlots.size());
        this.lockedSlots.forEach((slot, stack) -> {
            buf.writeInt(slot);
            buf.writeItemStack(stack);
        });
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
