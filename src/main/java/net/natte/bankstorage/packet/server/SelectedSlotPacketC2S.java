package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class SelectedSlotPacketC2S implements FabricPacket {

    public static final PacketType<SelectedSlotPacketC2S> TYPE = PacketType
            .create(Util.ID("selected_slot"), SelectedSlotPacketC2S::new);

    public static class Receiver implements
            PlayPacketHandler<SelectedSlotPacketC2S> {

        @Override
        public void receive(SelectedSlotPacketC2S packet, ServerPlayerEntity player,
                PacketSender responseSender) {
            ItemStack stack = player.getStackInHand(packet.isRight ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(Util.getUUID(stack), player.getWorld());
                if (bankItemStorage == null)
                    return;
                BankOptions options = Util.getOrCreateOptions(stack);
                options.selectedItemSlot = packet.slot;
                int size = bankItemStorage.getBlockItems().size();
                options.selectedItemSlot = size == 0 ? 0
                        : Math.min(Math.max(options.selectedItemSlot, 0), size - 1);
                Util.setOptions(stack, options);

            }
        }
    }

    public boolean isRight;
    public int slot;

    public SelectedSlotPacketC2S(boolean isRight, int slot) {
        this.isRight = isRight;
        this.slot = slot;
    }

    public SelectedSlotPacketC2S(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.isRight);
        buf.writeInt(this.slot);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
