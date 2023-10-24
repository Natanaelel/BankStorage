
package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class SortPacketC2S implements FabricPacket {

    public static final PacketType<SortPacketC2S> TYPE = PacketType
            .create(Util.ID("sort_c2s"), SortPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<SortPacketC2S> {

        @Override
        public void receive(SortPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
                return;
            ItemStack bankLikeItem = ((BankItemStorage) bankScreenHandler.inventory).getItem();
            BankOptions options = Util.getOrCreateOptions(bankLikeItem);
            options.sortMode = packet.sortMode;
            Util.setOptions(bankLikeItem, options);

            BankItemStorage bankItemStorage = (BankItemStorage) bankScreenHandler.inventory;

            Util.sortBank(bankItemStorage, player, packet.sortMode);

        }
    }

    public SortMode sortMode;

    public SortPacketC2S(SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public SortPacketC2S(PacketByteBuf buf) {
        this(SortMode.from(buf.readByte()));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(sortMode.number);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
