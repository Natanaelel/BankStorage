
package net.natte.bankstorage.packet.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.util.Util;

public class BuildModePacketC2S implements FabricPacket {

    public static final PacketType<BuildModePacketC2S> TYPE = PacketType.create(
            Util.ID("buildmode_c2s"), BuildModePacketC2S::new);

    public static class Receiver implements PlayPacketHandler<BuildModePacketC2S> {

        @Override
        public void receive(BuildModePacketC2S packet, ServerPlayerEntity player, PacketSender responseSender) {
            BankStorage.onChangeBuildMode(player, packet.bank);
        }
    }

    public ItemStack bank;

    public BuildModePacketC2S(ItemStack bank) {
        this.bank = bank;
    }

    public BuildModePacketC2S(PacketByteBuf buf) {
        this.bank = buf.readItemStack();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeItemStack(this.bank);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
