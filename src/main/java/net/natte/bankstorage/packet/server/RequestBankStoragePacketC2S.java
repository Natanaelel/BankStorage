package net.natte.bankstorage.packet.server;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketC2S implements FabricPacket {

    public static final PacketType<RequestBankStoragePacketC2S> TYPE = PacketType
            .create(Util.ID("requestbank_c2s"), RequestBankStoragePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<RequestBankStoragePacketC2S> {

        @Override
        public void receive(RequestBankStoragePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
                    NetworkUtil.syncCachedBankS2C(packet.uuid, player);
        }
    }

    public UUID uuid;

    public RequestBankStoragePacketC2S(UUID uuid) {
        this.uuid = uuid;
    }

    public RequestBankStoragePacketC2S(PacketByteBuf buf) {
        this(buf.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.uuid);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
