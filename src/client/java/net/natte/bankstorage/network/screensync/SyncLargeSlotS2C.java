package net.natte.bankstorage.network.screensync;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.packet.screensync.SyncLargeSlotPacketS2C;
import net.natte.bankstorage.screen.BankScreenHandler;

public class SyncLargeSlotS2C implements PlayPayloadHandler<SyncLargeSlotPacketS2C> {

    public void handle(@Nullable ClientPlayerEntity player, int windowId, int slot, ItemStack stack) {
        if (player != null && player.currentScreenHandler instanceof BankScreenHandler
                && windowId == player.currentScreenHandler.syncId) {
            player.currentScreenHandler.slots.get(slot).setStack(stack);
        }
    }

    @Override
    public void receive(SyncLargeSlotPacketS2C packet, Context context) {
        int windowId = packet.id();
        int slot = packet.slot();

        ItemStack stack = packet.itemStack();
        context.client().execute(() -> handle(context.player(), windowId, slot, stack));
    }
}
