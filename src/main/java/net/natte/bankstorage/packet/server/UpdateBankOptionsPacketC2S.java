package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

/**
 * Set the options of hold bank to values from client after validating
 * selectedSlot.
 * if selectedSlot didn't pass validation (bankstorage block items updated to
 * smaller size or smt): send cache update to client if buildmode
 */
public class UpdateBankOptionsPacketC2S implements FabricPacket {
    public static final PacketType<UpdateBankOptionsPacketC2S> TYPE = PacketType
            .create(Util.ID("update_options_c2s"), UpdateBankOptionsPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<UpdateBankOptionsPacketC2S> {

        @Override
        public void receive(UpdateBankOptionsPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {

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

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeNbt(options.asNbt());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
