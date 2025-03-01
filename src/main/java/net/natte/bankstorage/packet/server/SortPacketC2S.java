
package net.natte.bankstorage.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortPacketC2S(SortMode sortMode) implements CustomPacketPayload {

    public static final Type<SortPacketC2S> TYPE = new Type<>(Util.ID("sort_c2s"));
    public static final StreamCodec<FriendlyByteBuf, SortPacketC2S> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SortMode.class)
            .map(SortPacketC2S::new, SortPacketC2S::sortMode);

    @Override
    public Type<SortPacketC2S> type() {
        return TYPE;
    }

    public static void handle(SortPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
            return;
        ItemStack bankLikeItem = bankScreenHandler.getBankLikeItem();

        Util.setOptions(bankLikeItem, Util.getOrCreateOptions(bankLikeItem).withSortMode(packet.sortMode));

        BankItemStorage bankItemStorage = bankScreenHandler.getBankItemStorage();

        Util.sortBank(bankItemStorage, player, packet.sortMode);
    }
}
