package net.natte.bankstorage.packet.server;

import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.packet.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Set the options of bank in dock or hands to values from client after
 * validating
 * selectedSlot.
 * if selectedSlot didn't pass validation (bankstorage block items updated to
 * smaller size or smt): send cache update to client if buildmode
 */
public record UpdateBankOptionsPacketC2S(BankOptions options) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateBankOptionsPacketC2S> PACKET_ID = new CustomPacketPayload.Type<>(Util.ID("update_options_c2s"));
    public static final StreamCodec<ByteBuf, UpdateBankOptionsPacketC2S> PACKET_CODEC = BankOptions.STREAM_CODEC
            .map(
                    UpdateBankOptionsPacketC2S::new,
                    UpdateBankOptionsPacketC2S::options);

    public static class Receiver implements IPayloadHandler<UpdateBankOptionsPacketC2S> {

        @Override
        public void handle(UpdateBankOptionsPacketC2S packet, IPayloadContext context) {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.containerMenu instanceof BankScreenHandler bankScreenHandler) {

                AtomicBoolean hasOpenedBankDock = new AtomicBoolean(false);

                bankScreenHandler.getContext().run(
                        (world, blockPos) -> world
                                .getBlockEntity(blockPos, BankStorage.BANK_DOCK_BLOCK_ENTITY)
                                .ifPresent(dock -> {
                                    if (dock.hasBank()) {
                                        Util.setOptions(dock.getBank(), packet.options);
                                        dock.markDirty();
                                        hasOpenedBankDock.set(true);
                                    }
                                }));

                if (hasOpenedBankDock.get())
                    return;
            }

            ItemStack bankItem;

            if (Util.isBankLike(player.getMainHandStack()))
                bankItem = player.getMainHandStack();
            else if (Util.isBankLike(player.getOffHandStack()))
                bankItem = player.getOffHandStack();
            else
                return;

            BankItemStorage bankItemStorage = Util.getBankItemStorage(bankItem, player.getWorld());
            int clampedSelectedItemSlot = 0;
            if (bankItemStorage != null) {
                clampedSelectedItemSlot = MathHelper.clamp(packet.options.selectedItemSlot, 0,
                        bankItemStorage.getBlockItems().size() - 1);
            }

            BankOptions options = packet.options;
            if (clampedSelectedItemSlot != packet.options.selectedItemSlot) {
                options = packet.options.copy(); // copy to not modify packet.options
                options.selectedItemSlot = clampedSelectedItemSlot;
                NetworkUtil.syncCachedBankIfBuildModeS2C(bankItemStorage.uuid, player, bankItem);

            }
            Util.setOptions(bankItem, options);



        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
