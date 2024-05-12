package net.natte.bankstorage.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public record ItemStackBobbingAnimationPacketS2C(int index) implements CustomPayload {

    public static final CustomPayload.Id<ItemStackBobbingAnimationPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("bobbing_s2c"));
    public static final PacketCodec<ByteBuf, ItemStackBobbingAnimationPacketS2C> PACKET_CODEC = PacketCodecs.INTEGER
            .xmap(
                    ItemStackBobbingAnimationPacketS2C::new,
                    ItemStackBobbingAnimationPacketS2C::index);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
