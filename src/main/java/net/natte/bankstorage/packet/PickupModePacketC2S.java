
package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;

public class PickupModePacketC2S implements FabricPacket {

    public static final PacketType<PickupModePacketC2S> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "pickupmode_c2s"), PickupModePacketC2S::new);

    public static class Receiver implements PlayPacketHandler<PickupModePacketC2S> {

        @Override
        public void receive(PickupModePacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            BankStorage.onChangePickupMode(player);
        }
    }

    public PickupModePacketC2S() {
    }

    public PickupModePacketC2S(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}