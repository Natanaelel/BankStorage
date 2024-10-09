package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.access.KeyBindInfoAccess;
import net.natte.bankstorage.util.KeyBindInfo;
import net.natte.bankstorage.util.Util;

public class KeyBindUpdatePacketC2S implements FabricPacket {

    public static final PacketType<KeyBindUpdatePacketC2S> TYPE = PacketType
            .create(Util.ID("keybindupdate"), KeyBindUpdatePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<KeyBindUpdatePacketC2S> {

        @Override
        public void receive(KeyBindUpdatePacketC2S packet, ServerPlayerEntity player,
                            PacketSender responseSender) {
            ((KeyBindInfoAccess) player).bankstorge$setKeyBindInfo(packet.keyBindInfo);
        }
    }

    public KeyBindInfo keyBindInfo;

    public KeyBindUpdatePacketC2S(KeyBindInfo keyBindInfo) {
        this.keyBindInfo = keyBindInfo;
    }

    public KeyBindUpdatePacketC2S(PacketByteBuf buf) {
        this(new KeyBindInfo(buf.readBoolean(), buf.readBoolean()));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.keyBindInfo.hasBuildModeToggleKey());
        buf.writeBoolean(this.keyBindInfo.hasBuildModeCycleKey());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
