
package net.natte.bankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public record SortPacketC2S(SortMode sortMode) implements CustomPayload {

    public static final CustomPayload.Id<SortPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("sort_c2s"));
    public static final PacketCodec<ByteBuf, SortPacketC2S> PACKET_CODEC = PacketCodecs.BYTE.xmap(
            b -> new SortPacketC2S(SortMode.from(b)),
            p -> p.sortMode().number);

    public static class Receiver implements PlayPayloadHandler<SortPacketC2S> {

        @Override
        public void receive(SortPacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
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

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
