package net.natte.bankstorage.packet.server;

import java.util.concurrent.atomic.AtomicBoolean;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

/**
 * Set the options of bank in dock or hands to values from client after
 * validating
 * selectedSlot.
 * if selectedSlot didn't pass validation (bankstorage block items updated to
 * smaller size or smt): send cache update to client if buildmode
 */
public class UpdateBankOptionsPacketC2S implements CustomPayload {
    // public static final PacketType<UpdateBankOptionsPacketC2S> TYPE = PacketType
            // .create(Util.ID("update_options_c2s"), UpdateBankOptionsPacketC2S::new);

    public static final CustomPayload.Id<UpdateBankOptionsPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("update_options_c2s"));
    public static final PacketCodec<RegistryByteBuf, UpdateBankOptionsPacketC2S> PACKET_CODEC = PacketCodec.of(UpdateBankOptionsPacketC2S::write, UpdateBankOptionsPacketC2S::new);

    // public static class Receiver implements PlayPacketHandler<UpdateBankOptionsPacketC2S> {
    public static class Receiver implements PlayPayloadHandler<UpdateBankOptionsPacketC2S> {

        @Override
        public void receive(UpdateBankOptionsPacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler) {

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
                options = BankOptions.fromNbt(packet.options.asNbt()); // copy to not modify packet.options
                options.selectedItemSlot = clampedSelectedItemSlot;
                NetworkUtil.syncCachedBankIfBuildModeS2C(bankItemStorage.uuid, player, bankItem);

            }
            Util.setOptions(bankItem, options);

        }

    }

    public BankOptions options;

    public UpdateBankOptionsPacketC2S(BankOptions options) {
        this.options = options;
    }

    public UpdateBankOptionsPacketC2S(PacketByteBuf buf) {
        this(BankOptions.fromNbt(buf.readNbt()));
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeNbt(options.asNbt());
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
