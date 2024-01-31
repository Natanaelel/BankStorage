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
            long millis = System.currentTimeMillis();
            while(System.currentTimeMillis() < millis + 500);  // TODO: remove obv

            // if(packet.buildModePreviewRevision == BankStorageClient)


            ItemStack stack = player.getStackInHand(packet.isRight ? Hand.MAIN_HAND : Hand.OFF_HAND);
            if (Util.isBankLike(stack) && Util.hasUUID(stack)) {
                BankItemStorage bankItemStorage = Util.getBankItemStorage(Util.getUUID(stack), player.getWorld());
                if (bankItemStorage == null)
                    return;
                BankOptions options = Util.getOrCreateOptions(stack);
                options.selectedItemSlot -= (int) Math.signum(packet.scroll);

                int size = bankItemStorage.getBlockItems().size();
                options.selectedItemSlot = Math.max(Math.min(options.selectedItemSlot, size - 1), 0);
                Util.setOptions(stack, options);
            }

        }
    }

    public boolean isRight;
    public double scroll;
    public short buildModePreviewRevision;

    public ScrollPacketC2S(boolean isRight, double scroll, short buildModePreviewRevision) {
        this.isRight = isRight;
        this.scroll = scroll;
        this.buildModePreviewRevision = buildModePreviewRevision;
    }

    public ScrollPacketC2S(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readDouble(), buf.readShort());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isRight);
        buf.writeDouble(scroll);
        buf.writeShort(buildModePreviewRevision);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
