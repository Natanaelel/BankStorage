
package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class SortPacketC2S implements CustomPayload {

    // public static final PacketType<SortPacketC2S> TYPE = PacketType
            // .create(Util.ID("sort_c2s"), SortPacketC2S::new);
    public static final CustomPayload.Id<SortPacketC2S> PACKET_ID = new CustomPayload.Id<>(Util.ID("sort_c2s"));
    public static final PacketCodec<RegistryByteBuf, SortPacketC2S> PACKET_CODEC = PacketCodec.of(SortPacketC2S::write, SortPacketC2S::new);

    public static class Receiver implements PlayPayloadHandler<SortPacketC2S> {

        @Override
        public void receive(SortPacketC2S packet, Context context) {
            ServerPlayerEntity player = context.player();
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
                return;
            ItemStack bankLikeItem = ((BankItemStorage) bankScreenHandler.inventory).getItem();
            BankOptions options = Util.getOrCreateOptions(bankLikeItem);
            options.sortMode = packet.sortMode;
            Util.setOptions(bankLikeItem, options);

            BankItemStorage bankItemStorage = (BankItemStorage) bankScreenHandler.inventory;

            Util.sortBank(bankItemStorage, player, packet.sortMode);

        }
    }

    public SortMode sortMode;

    public SortPacketC2S(SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public SortPacketC2S(PacketByteBuf buf) {
        this(SortMode.from(buf.readByte()));
    }

    // @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(sortMode.number);
    }

    // @Override
    // public PacketType<?> getType() {
    //     return TYPE;
    // }

    @Override
    public Id<? extends CustomPayload> getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }
}
