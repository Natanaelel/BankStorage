package net.natte.bankstorage.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Random;

public record SyncedRandomPacketS2C(long randomSeed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncedRandomPacketS2C> TYPE = new CustomPacketPayload.Type<>(Util.ID("sync_random"));
    public static final StreamCodec<ByteBuf, SyncedRandomPacketS2C> STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(
            SyncedRandomPacketS2C::new,
            SyncedRandomPacketS2C::randomSeed);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncedRandomPacketS2C packet, IPayloadContext context) {
        Util.clientSyncedRandom = new Random(packet.randomSeed());
    }
}
