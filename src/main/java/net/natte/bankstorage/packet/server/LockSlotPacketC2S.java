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

public class LockSlotPacketC2S implements CustomPayload {

    // public static final PacketType<LockSlotPacketC2S> TYPE = PacketType
            // .create(Util.ID("lock_slot_c2s"), LockSlotPacketC2S::new);
    public static final CustomPayload.Id<LockSlotPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("lock_slot_c2s"));
    // public static final PacketCodec<RegistryByteBuf, LockSlotPacketC2S> PACKET_CODEC = PacketCodec.of(LockSlotPacketC2S::write, LockSlotPacketC2S::new);
    // public static final Codec CODEC = RecordCodecBuilder.create(instance -> instance.group(
    //     Codec.INT.fieldOf("syncId").forGetter(LockSlotPacketC2S::getSyncId),
    //     Codec.INT.fieldOf("slot").forGetter(LockSlotPacketC2S::getSlot)).apply
    // );
    public static final PacketCodec<RegistryByteBuf, LockSlotPacketC2S> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        LockSlotPacketC2S::getSyncId,
        PacketCodecs.INTEGER,
        LockSlotPacketC2S::getSlot,
        ItemStack.OPTIONAL_PACKET_CODEC,
        LockSlotPacketC2S::getItemStack,
        PacketCodecs.BOOL,
        LockSlotPacketC2S::getShouldLock,
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

    public int syncId;
    public int slot;
    public ItemStack stack;
    public boolean shouldLock;
    public int getSyncId(){return syncId;}
    public int getSlot(){return slot;}
    public ItemStack getItemStack(){return stack;}
    public boolean getShouldLock(){return shouldLock;}

    public LockSlotPacketC2S(int syncId, int slot, ItemStack stack, boolean shouldLock) {
        this.syncId = syncId;
        this.slot = slot;
        this.stack = stack.copyWithCount(1);
        this.shouldLock = shouldLock;
    }

    // public LockSlotPacketC2S(PacketByteBuf buf) {
    //     this(buf.readInt(), buf.readInt(), ItemStack.fromNbt(wrapperLookup, null)buf.readNbt(), buf.readBoolean());
    // }

    // // @Override
    // public void write(PacketByteBuf buf) {
    //     buf.writeInt(syncId);
    //     buf.writeInt(slot);
    //     // buf.
    //     // MinecraftClient.getInstance().world.getRegistryManager()
    //     // NbtElement nbt = stack.encode(null);
    //     // .encode(stack, null, null)
    //     // buf.writeNbt(stack.encode(this.wrapperLookup));
        
    //     // buf.writeItemStack(stack);
    //     buf.writeBoolean(shouldLock);
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
