package net.natte.bankstorage.packet.server;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public record RequestBankStoragePacketC2S(UUID uuid, short cachedRevision) implements CustomPayload {

    public static final CustomPayload.Id<RequestBankStoragePacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("requestbank_c2s"));
    public static final PacketCodec<RegistryByteBuf, RequestBankStoragePacketC2S> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            RequestBankStoragePacketC2S::uuid,
            PacketCodecs.SHORT,
            RequestBankStoragePacketC2S::cachedRevision,
            RequestBankStoragePacketC2S::new);

    public static class Receiver implements
            PlayPayloadHandler<RequestBankStoragePacketC2S> {

        @Override
        public void receive(RequestBankStoragePacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
            BankItemStorage bankItemStorage = Util.getBankItemStorage(packet.uuid, player.getWorld());
            // only send update if client doesn't already have latest version (revision)
            if (packet.cachedRevision != bankItemStorage.getRevision()) {
                NetworkUtil.syncCachedBankS2C(packet.uuid, player);
            }
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
