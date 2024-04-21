package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class PickupModePacketC2S implements FabricPacket {

    public static final PacketType<PickupModePacketC2S> TYPE = PacketType
            .create(Util.ID("pickupmode"), PickupModePacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<PickupModePacketC2S> {

        @Override
        public void receive(PickupModePacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            if (player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler)
                togglePickupModeOfScreenHandler(player, bankScreenHandler);
            else
                togglePickupModeOfHeldBank(player);

        }

        private void togglePickupModeOfScreenHandler(ServerPlayerEntity player, BankScreenHandler bankScreenHandler) {
            ItemStack bank = bankScreenHandler.getBankLikeItem();
            BankOptions options = Util.getOrCreateOptions(bank);
            options.pickupMode = options.pickupMode.next();
            Util.setOptions(bank, options);

            // dock.markDirty if has dock pos
            bankScreenHandler.getContext().run(
                    (world, blockPos) -> world
                            .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY)
                            .ifPresent(dock -> {
                                if (dock.hasBank()) {
                                    Util.setOptions(dock.getBank(), options);
                                    dock.markDirty();
                                }
                            }));
        }

        private void togglePickupModeOfHeldBank(ServerPlayerEntity player) {
            ItemStack stack;
            if (Util.isBankLike(player.getMainHandStack()))
                stack = player.getMainHandStack();
            else if (Util.isBankLike(player.getOffHandStack()))
                stack = player.getOffHandStack();
            else
                return;

            BankOptions options = Util.getOrCreateOptions(stack);
            options.pickupMode = options.pickupMode.next();
            Util.setOptions(stack, options);
            player.sendMessage(Text.translatable("popup.bankstorage.pickupmode."
                    + options.pickupMode.toString().toLowerCase()), true);
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
