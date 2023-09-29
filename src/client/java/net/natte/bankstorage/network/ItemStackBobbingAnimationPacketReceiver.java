package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.packet.ItemStackBobbingAnimationPacketS2C;

public class ItemStackBobbingAnimationPacketReceiver implements PlayPacketHandler<ItemStackBobbingAnimationPacketS2C> {

    public void receive(ItemStackBobbingAnimationPacketS2C packet, ClientPlayerEntity player, PacketSender response) {
        player
                .getInventory()
                .getStack(packet.index)
                .setBobbingAnimationTime(5);
    }
}
