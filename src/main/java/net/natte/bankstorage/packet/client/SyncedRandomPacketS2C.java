package net.natte.bankstorage.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public record SyncedRandomPacketS2C(long randomSeed) implements CustomPayload {

    public static final CustomPayload.Id<SyncedRandomPacketS2C> PACKET_ID = new CustomPayload.Id<>(Util.ID("sync_random"));
    public static final PacketCodec<ByteBuf, SyncedRandomPacketS2C> PACKET_CODEC = PacketCodecs.VAR_LONG.xmap(
            SyncedRandomPacketS2C::new,
            SyncedRandomPacketS2C::randomSeed);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
