package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;

public class ItemStackBobbingAnimationPacketReceiver implements PlayPayloadHandler<ItemStackBobbingAnimationPacketS2C> {

    @Override
    public void receive(ItemStackBobbingAnimationPacketS2C packet, Context context) {
        context.player()
                .getInventory()
                .getStack(packet.index())
                .setBobbingAnimationTime(5);
    }
}
