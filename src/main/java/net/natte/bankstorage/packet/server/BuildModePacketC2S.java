package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.util.ServerEvents;
import net.natte.bankstorage.util.Util;

public class BuildModePacketC2S implements FabricPacket {

    public static final PacketType<BuildModePacketC2S> TYPE = PacketType
            .create(Util.ID("buildmode"), BuildModePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<BuildModePacketC2S> {

        @Override
        public void receive(BuildModePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            ServerEvents.onToggleBuildMode(player);
        }
    }

    public BuildModePacketC2S() {
    }

    public BuildModePacketC2S(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
