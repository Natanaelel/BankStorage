package net.natte.bankstorage.packet.server;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketC2S implements CustomPayload {

    // public static final PacketType<RequestBankStoragePacketC2S> TYPE = PacketType
            // .create(Util.ID("requestbank_c2s"), RequestBankStoragePacketC2S::new);
    public static final CustomPayload.Id<RequestBankStoragePacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("requestbank_c2s"));
    public static final PacketCodec<RegistryByteBuf, RequestBankStoragePacketC2S> PACKET_CODEC = PacketCodec.of(RequestBankStoragePacketC2S::write, RequestBankStoragePacketC2S::new);


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

    public UUID uuid;
    public short cachedRevision;

    public RequestBankStoragePacketC2S(UUID uuid, short revision) {
        this.uuid = uuid;
        this.cachedRevision = revision;
    }

    public RequestBankStoragePacketC2S(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readShort());
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.uuid);
        buf.writeShort(cachedRevision);
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
