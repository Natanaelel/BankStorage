package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdatePacketC2S implements CustomPayload {

    // public static final PacketType<KeyBindUpdatePacketC2S> TYPE = PacketType
            // .create(Util.ID("keybindupdate"), KeyBindUpdatePacketC2S::new);
    public static final CustomPayload.Id<KeyBindUpdatePacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("keybindupdate"));
    public static final PacketCodec<RegistryByteBuf, KeyBindUpdatePacketC2S> PACKET_CODEC = PacketCodec.of(KeyBindUpdatePacketC2S::write, KeyBindUpdatePacketC2S::new);
   
    public static class Receiver implements
            PlayPayloadHandler<KeyBindUpdatePacketC2S> {

        @Override
        public void receive(KeyBindUpdatePacketC2S packet, Context context) {
            Util.isBuildModeKeyUnBound = packet.isUnbound;
        }
    }

    public boolean isUnbound;

    public KeyBindUpdatePacketC2S(boolean isUnbound) {
        this.isUnbound = isUnbound;
    }

    public KeyBindUpdatePacketC2S(PacketByteBuf buf) {
        this(buf.readBoolean());
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.isUnbound);
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
