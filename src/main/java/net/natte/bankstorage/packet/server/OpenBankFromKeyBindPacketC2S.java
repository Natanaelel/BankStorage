package net.natte.bankstorage.packet.server;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.util.Util;

public class OpenBankFromKeyBindPacketC2S implements CustomPayload {

    // public static final PacketType<OpenBankFromKeyBindPacketC2S> TYPE = PacketType
            // .create(Util.ID("open_bank_from_keybind_c2s"), OpenBankFromKeyBindPacketC2S::new);
        
    public static final CustomPayload.Id<OpenBankFromKeyBindPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("open_bank_from_keybind_c2s"));
    public static final PacketCodec<RegistryByteBuf, OpenBankFromKeyBindPacketC2S> PACKET_CODEC = PacketCodec.of(OpenBankFromKeyBindPacketC2S::write, OpenBankFromKeyBindPacketC2S::new);


    public static class Receiver implements PlayPayloadHandler<OpenBankFromKeyBindPacketC2S> {

        @Override
        public void receive(OpenBankFromKeyBindPacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
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

    // @Override
    public void write(PacketByteBuf buf) {
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
