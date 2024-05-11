package net.natte.bankstorage.packet.client;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public class ItemStackBobbingAnimationPacketS2C implements CustomPayload {

    // public static final PacketType<ItemStackBobbingAnimationPacketS2C> TYPE = PacketType
            // .create(Util.ID("bobbing_s2c"), ItemStackBobbingAnimationPacketS2C::new);

    public static final CustomPayload.Id<ItemStackBobbingAnimationPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("bobbing_s2c"));
    public static final PacketCodec<PacketByteBuf, ItemStackBobbingAnimationPacketS2C> PACKET_CODEC = PacketCodec.of(ItemStackBobbingAnimationPacketS2C::write, ItemStackBobbingAnimationPacketS2C::new);

    public int index;

    public ItemStackBobbingAnimationPacketS2C(int index) {
        this.index = index;
    }

    public ItemStackBobbingAnimationPacketS2C(PacketByteBuf buf) {
        this(buf.readInt());
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.index);
    }

    // @Override
    // public PacketType<?> getType() {
    //     return TYPE;
    // }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
