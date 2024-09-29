package net.natte.bankstorage.packet.server;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestBankStoragePacketC2S(UUID uuid, short cachedRevision) implements CustomPacketPayload {

    public static final Type<RequestBankStoragePacketC2S> TYPE = new Type<>(Util.ID("requestbank_c2s"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBankStoragePacketC2S> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RequestBankStoragePacketC2S::uuid,
            ByteBufCodecs.SHORT,
            RequestBankStoragePacketC2S::cachedRevision,
            RequestBankStoragePacketC2S::new);

    @Override
    public Type<RequestBankStoragePacketC2S> type() {
        return TYPE;
    }

    public static void handle(RequestBankStoragePacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        BankItemStorage bankItemStorage = Util.getBankItemStorage(packet.uuid);
        if (bankItemStorage == null) {
            BankStorage.LOGGER.info("{} at {} {} requested cache update for {} revision {} which doesn't exist. This is *impossible*. ignoring.", context.player().getName().getString(), context.player().blockPosition(), context.player().level(), packet.uuid, packet.cachedRevision);
            return;
        }
        // only send update if client doesn't already have latest version (revision)
        if (packet.cachedRevision != bankItemStorage.getRevision()) {
            NetworkUtil.syncCachedBankS2C(packet.uuid, player);
        }
    }
}
