package net.natte.bankstorage.packet.server;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.client.OptionPacketS2C;
import net.natte.bankstorage.util.Util;

public class ScrollPacketC2S implements FabricPacket {

    public static final PacketType<ScrollPacketC2S> TYPE = PacketType
            .create(Util.ID("scroll_c2s"), ScrollPacketC2S::new);

    public static class Receiver implements PlayPacketHandler<ScrollPacketC2S> {

        @Override
        public void receive(ScrollPacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {

            BankItemStorage bankItemStorage = Util.getBankItemStorage(packet.uuid, player.getWorld());
            int selectedItemSlot = bankItemStorage.options.selectedItemSlot;
            selectedItemSlot -= (int) Math.signum(packet.scroll);
            int size = bankItemStorage.getBlockItems().size();
            bankItemStorage.options.selectedItemSlot = size == 0 ? 0
                    : Math.min(Math.max(selectedItemSlot, 0), size - 1);

            responseSender.sendPacket(new OptionPacketS2C(packet.uuid, bankItemStorage.options.asNbt()));

        }
    }

    public UUID uuid;
    public double scroll;

    public ScrollPacketC2S(UUID uuid, double scroll) {
        this.uuid = uuid;
        this.scroll = scroll;
    }

    public ScrollPacketC2S(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readDouble());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeDouble(scroll);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
