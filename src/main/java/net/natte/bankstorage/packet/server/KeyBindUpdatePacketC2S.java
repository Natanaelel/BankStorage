package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public record KeyBindUpdatePacketC2S(boolean isUnbound) implements CustomPayload {

    public static final CustomPayload.Id<KeyBindUpdatePacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("keybindupdate"));
    public static final PacketCodec<RegistryByteBuf, KeyBindUpdatePacketC2S> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL,
            KeyBindUpdatePacketC2S::isUnbound,
            KeyBindUpdatePacketC2S::new);

    public static class Receiver implements
            PlayPayloadHandler<KeyBindUpdatePacketC2S> {

        @Override
        public void receive(KeyBindUpdatePacketC2S packet, Context context) {
            Util.isBuildModeKeyUnBound = packet.isUnbound;
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
