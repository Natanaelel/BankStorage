
package net.natte.bankstorage.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortPacketC2S(SortMode sortMode) implements CustomPacketPayload {

    public static final Type<SortPacketC2S> TYPE = new Type<>(Util.ID("sort_c2s"));
    public static final StreamCodec<ByteBuf, SortPacketC2S> STREAM_CODEC = ByteBufCodecs.BYTE.map(
            b -> new SortPacketC2S(SortMode.values()[b]),
            p -> (byte) p.sortMode().ordinal());

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
        BankOptions options = Util.getOrCreateOptions(bankLikeItem);
        options.sortMode = packet.sortMode;
        Util.setOptions(bankLikeItem, options);

        BankItemStorage bankItemStorage = bankScreenHandler.getBankItemStorage();

        Util.sortBank(bankItemStorage, player, packet.sortMode);
    }
}
