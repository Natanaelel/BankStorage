package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.packet.ItemStackBobbingAnimationPacketS2C;

public class ItemStackBobbingAnimationPacketReceiver implements PlayPacketHandler<ItemStackBobbingAnimationPacketS2C> {

    public void receive(ItemStackBobbingAnimationPacketS2C packet, ClientPlayerEntity player,
            PacketSender responseSender) {
        PlayerInventory inventory = player.getInventory();
        ItemStack stack = ItemStack.EMPTY;
        int i = packet.index;
        if (i < 9 * 4) {
            stack = inventory.main.get(i);
        } else if (i < 9 * 4 + 1) {
            stack = inventory.offHand.get(i - 9 * 4);
        } else {
            stack = inventory.armor.get(i - 9 * 4 - 1);
        }
        stack.setBobbingAnimationTime(5);

    }
}