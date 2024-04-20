package net.natte.bankstorage.packet.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.util.Util;

public class OpenBankFromKeyBindPacketC2S implements FabricPacket {

    public static final PacketType<OpenBankFromKeyBindPacketC2S> TYPE = PacketType
            .create(Util.ID("open_bank_from_keybind_c2s"), OpenBankFromKeyBindPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<OpenBankFromKeyBindPacketC2S> {

        @Override
        public void receive(OpenBankFromKeyBindPacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            ItemStack bank = findBank(player);
            if (bank == null)
                return;
            BankItemStorage bankItemStorage = Util.getBankItemStorage(bank, player.getWorld());
            if (bankItemStorage == null)
                return;
            player.openHandledScreen(bankItemStorage.withItem(bank));
        }

        private @Nullable ItemStack findBank(ServerPlayerEntity player) {
            PlayerInventory inventory = player.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (Util.isBank(stack))
                    return stack;
                if (Util.isLink(stack) && Util.hasUUID(stack))
                    return stack;
            }
            return null;
        }
    }

    public OpenBankFromKeyBindPacketC2S() {
    }

    public OpenBankFromKeyBindPacketC2S(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
