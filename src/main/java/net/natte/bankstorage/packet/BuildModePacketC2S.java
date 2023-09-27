
package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class BuildModePacketC2S implements FabricPacket {

    public static final PacketType<SortPacketC2S> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "buildmode_c2s"), SortPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<SortPacketC2S> {

        @Override
        public void receive(SortPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            BankStorage.onChangeBuildMode(player);
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