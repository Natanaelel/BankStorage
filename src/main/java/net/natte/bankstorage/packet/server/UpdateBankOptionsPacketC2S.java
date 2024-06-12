package net.natte.bankstorage.packet.server;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Set the options of bank in dock or hands to values from client after
 * validating
 * selectedSlot.
 * if selectedSlot didn't pass validation (bankstorage block items updated to
 * smaller size or smt): send cache update to client if buildmode
 */
public record UpdateBankOptionsPacketC2S(BankOptions options) implements CustomPacketPayload {

    public static final Type<UpdateBankOptionsPacketC2S> TYPE = new Type<>(Util.ID("update_options_c2s"));
    public static final StreamCodec<ByteBuf, UpdateBankOptionsPacketC2S> STREAM_CODEC = BankOptions.STREAM_CODEC
            .map(
                    UpdateBankOptionsPacketC2S::new,
                    UpdateBankOptionsPacketC2S::options);

    @Override
    public Type<UpdateBankOptionsPacketC2S> type() {
        return TYPE;
    }


    public static void handle(UpdateBankOptionsPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.containerMenu instanceof BankScreenHandler bankScreenHandler) {

            AtomicBoolean hasOpenedBankDock = new AtomicBoolean(false);

            bankScreenHandler.getContext().execute(
                    (world, blockPos) -> world
                            .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY)
                            .ifPresent(dock -> {
                                if (dock.hasBank()) {
                                    Util.setOptions(dock.getBank(), packet.options);
                                    dock.setChanged();
                                    hasOpenedBankDock.set(true);
                                }
                            }));

            if (hasOpenedBankDock.get())
                return;
        }

        ItemStack bankItem;

        if (Util.isBankLike(player.getMainHandItem()))
            bankItem = player.getMainHandItem();
        else if (Util.isBankLike(player.getOffhandItem()))
            bankItem = player.getOffhandItem();
        else
            return;

        BankItemStorage bankItemStorage = Util.getBankItemStorage(bankItem, player.level());
        int clampedSelectedItemSlot = 0;
        if (bankItemStorage != null) {
            clampedSelectedItemSlot = Mth.clamp(packet.options.selectedItemSlot, 0,
                    bankItemStorage.getBlockItems().size() - 1);
        }

        BankOptions options = packet.options;
        if (clampedSelectedItemSlot != packet.options.selectedItemSlot) {
            options = packet.options.copy(); // copy to not modify packet.options
            options.selectedItemSlot = clampedSelectedItemSlot;

            // completely useless?
            NetworkUtil.syncCachedBankIfBuildModeS2C(bankItemStorage.uuid, player, bankItem);

        }
        Util.setOptions(bankItem, options);
    }
}
