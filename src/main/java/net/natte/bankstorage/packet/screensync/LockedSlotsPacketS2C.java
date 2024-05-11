package net.natte.bankstorage.packet.screensync;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public record LockedSlotsPacketS2C(int syncId, Map<Integer, ItemStack> lockedSlots) implements CustomPayload {

    public static final CustomPayload.Id<LockedSlotsPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("sync_locked_slots_s2c"));
    public static final PacketCodec<RegistryByteBuf, LockedSlotsPacketS2C> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        LockedSlotsPacketS2C::syncId,
        PacketCodecs.map(HashMap::newHashMap, PacketCodecs.INTEGER, ItemStack.OPTIONAL_PACKET_CODEC),
        LockedSlotsPacketS2C::lockedSlots,
        LockedSlotsPacketS2C::new        
    );


    // public int syncId;
    // public Map<Integer, ItemStack> lockedSlots;

    // public LockedSlotsPacketS2C(int syncId, Map<Integer, ItemStack> lockedSlots) {
    //     this.syncId = syncId;
    //     this.lockedSlots = lockedSlots;
    // }

    // public LockedSlotsPacketS2C(PacketByteBuf buf) {
    //     this(buf.readInt(), LockedSlotsPacketS2C.readLockedSlots(buf));
    // }

    // private static Map<Integer, ItemStack> readLockedSlots(PacketByteBuf buf) {
    //     Map<Integer, ItemStack> lockedSlots = new HashMap<>();
    //     int size = buf.readInt();
    //     for(int i = 0; i < size; ++i){
    //         lockedSlots.put(buf.readInt(), buf.readItemStack());
    //     }
    //     return lockedSlots;
    // }

    // @Override
    // public void write(PacketByteBuf buf) {
    //     buf.writeInt(this.syncId);
    //     buf.writeInt(this.lockedSlots.size());
    //     this.lockedSlots.forEach((slot, stack) -> {
    //         buf.writeInt(slot);
    //         buf.writeItemStack(stack);
    //     });
    // }

    // @Override
    // public PacketType<?> getType() {
    //     return TYPE;
    // }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
