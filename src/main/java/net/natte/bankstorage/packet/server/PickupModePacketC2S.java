
package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.packet.client.OptionPacketS2C;
import net.natte.bankstorage.util.Util;

public class PickupModePacketC2S implements FabricPacket {

    public static final PacketType<PickupModePacketC2S> TYPE = PacketType
            .create(Util.ID("pickupmode_c2s"), PickupModePacketC2S::new);

    public static class Receiver implements PlayPacketHandler<PickupModePacketC2S> {

        @Override
        public void receive(PickupModePacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {

            ItemStack stackInHand = player.getStackInHand(player.getActiveHand());

            if (Util.isBank(stackInHand)) {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(stackInHand, player.getWorld());
                bankItemStorage.options.pickupMode = PickupMode
                        .from((bankItemStorage.options.pickupMode.number + 1) % 4);
                player.sendMessage(Text.translatable(
                        "popup.bankstorage.pickupmode." + bankItemStorage.options.pickupMode.toString().toLowerCase()),
                        true);

                ServerPlayNetworking.send(player,
                        new OptionPacketS2C(Util.getUUID(stackInHand), bankItemStorage.options.asNbt()));
            }

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
