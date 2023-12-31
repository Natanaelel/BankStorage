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

public class ScrollPacketC2S implements FabricPacket {

    public static final PacketType<ScrollPacketC2S> TYPE = PacketType
            .create(Util.ID("scroll_c2s"), ScrollPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<ScrollPacketC2S> {

        @Override
        public void receive(ScrollPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {

            ItemStack stack = player.getStackInHand(packet.isRight ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(Util.getUUID(stack), player.getWorld());
                if (bankItemStorage == null)
                    return;
                BankOptions options = Util.getOrCreateOptions(stack);
                options.selectedItemSlot -= (int) Math.signum(packet.scroll);
                // int size = bankItemStorage.getBlockItems().size();
                int size = bankItemStorage.getBlockItems().size();
                options.selectedItemSlot = size == 0 ? 0
                        : Math.min(Math.max(options.selectedItemSlot, 0), size - 1);
                Util.setOptions(stack, options);

            }

        }
    }

    public boolean isRight;
    public double scroll;

    public ScrollPacketC2S(boolean isRight, double scroll) {
        this.isRight = isRight;
        this.scroll = scroll;
    }

    public ScrollPacketC2S(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readDouble());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isRight);
        buf.writeDouble(scroll);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
