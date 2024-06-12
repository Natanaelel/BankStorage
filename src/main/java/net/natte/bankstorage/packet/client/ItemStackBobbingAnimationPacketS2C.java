package net.natte.bankstorage.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ItemStackBobbingAnimationPacketS2C(int index) implements CustomPacketPayload {

    public static final Type<ItemStackBobbingAnimationPacketS2C> TYPE = new Type<>(Util.ID("bobbing_s2c"));
    public static final StreamCodec<ByteBuf, ItemStackBobbingAnimationPacketS2C> STREAM_CODEC = ByteBufCodecs.INT
            .map(
                    ItemStackBobbingAnimationPacketS2C::new,
                    ItemStackBobbingAnimationPacketS2C::index);

    @Override
    public Type<ItemStackBobbingAnimationPacketS2C> type() {
        return TYPE;
    }

    public static void handle(ItemStackBobbingAnimationPacketS2C packet, IPayloadContext context) {
        context.player()
                .getInventory()
                .getItem(packet.index())
                .setPopTime(5);
    }
}
