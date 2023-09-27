
package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class SortPacketC2S implements FabricPacket {

    public static final PacketType<SortPacketC2S> TYPE = PacketType
            .create(new Identifier(BankStorage.MOD_ID, "sort_c2s"), SortPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<SortPacketC2S> {

        @Override
        public void receive(SortPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
                return;
            BankItemStorage bankItemStorage = (BankItemStorage) bankScreenHandler.inventory;

            Util.sortBank(bankItemStorage);

        }
    }

    public SortPacketC2S() {
    }

    public SortPacketC2S(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}