package net.natte.bankstorage.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Set the options of bank in dock or hands to values from client after
 * validating
 * selectedSlot.
 * if selectedSlot didn't pass validation (bankstorage block items updated to
 * smaller size or smt): send cache update to client if buildmode
 */
public record UpdateBankOptionsPacketC2S(InteractionHand hand, boolean dock,
                                         BankOptions options) implements CustomPacketPayload {

    public static final Type<UpdateBankOptionsPacketC2S> TYPE = new Type<>(Util.ID("update_options_c2s"));
    public static final StreamCodec<FriendlyByteBuf, UpdateBankOptionsPacketC2S> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class),
            UpdateBankOptionsPacketC2S::hand,
            ByteBufCodecs.BOOL,
            UpdateBankOptionsPacketC2S::dock,
            BankOptions.STREAM_CODEC,
            UpdateBankOptionsPacketC2S::options,
            UpdateBankOptionsPacketC2S::new);

    @Override
    public Type<UpdateBankOptionsPacketC2S> type() {
        return TYPE;
    }


    public static void handle(UpdateBankOptionsPacketC2S packet, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (packet.dock) {
            if (player.containerMenu instanceof BankScreenHandler bankScreenHandler) {
                bankScreenHandler.getContext().execute(
                        (world, blockPos) -> world
                                .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY.get())
                                .ifPresent(dock -> {
                                    if (dock.hasBank()) {
                                        Util.setOptions(dock.getBank(), packet.options);
                                        dock.setChanged();
                                    }
                                }));
            }
        } else {


            ItemStack bankItem = player.getItemInHand(packet.hand);

            if (!Util.isBankLike(bankItem))
                return;

            BankOptions options = packet.options;

            BankItemStorage bankItemStorage = Util.getBankItemStorage(bankItem);
            
            bankItem.set(BankStorage.OptionsComponentType, options);

            if (bankItemStorage != null)
                NetworkUtil.syncCachedBankS2C(bankItemStorage.uuid, player);
        }
    }
}
