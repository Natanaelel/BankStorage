package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public class BuildModePacketC2S implements FabricPacket {

    public static final PacketType<BuildModePacketC2S> TYPE = PacketType
            .create(Util.ID("buildmode"), BuildModePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<BuildModePacketC2S> {

        @Override
        public void receive(BuildModePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            if (Util.isBankLike(player.getMainHandStack())) {
                BankOptions options = Util.getOrCreateOptions(player.getMainHandStack());
                options.buildMode = packet.buildMode;
                Util.setOptions(player.getMainHandStack(), options);
            } else if (Util.isBankLike(player.getOffHandStack())) {
                BankOptions options = Util.getOrCreateOptions(player.getOffHandStack());
                options.buildMode = packet.buildMode;
                Util.setOptions(player.getOffHandStack(), options);
            }
        }
    }

    public BuildMode buildMode;

    public BuildModePacketC2S(BuildMode buildMode) {
        this.buildMode = buildMode;
    }

    public BuildModePacketC2S(PacketByteBuf buf) {
        this(BuildMode.from(buf.readByte()));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(buildMode.number);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
