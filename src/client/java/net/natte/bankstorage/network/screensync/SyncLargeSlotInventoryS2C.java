package net.natte.bankstorage.network.screensync;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.packet.screensync.SyncContainerPacketS2C;
import net.natte.bankstorage.screen.BankScreenHandler;

public class SyncLargeSlotInventoryS2C implements PlayPayloadHandler<SyncContainerPacketS2C> {

    public void handle(@Nullable ClientPlayerEntity player, int stateID, int windowId, List<ItemStack> stacks,
            ItemStack carried) {
        if (player != null && player.currentScreenHandler instanceof BankScreenHandler
                && windowId == player.currentScreenHandler.syncId) {
            player.currentScreenHandler.updateSlotStacks(stateID, stacks, carried);

        }
    }

    @Override
    public void receive(SyncContainerPacketS2C packet, Context context) {
        int stateID = packet.stateId();
        int containerID = packet.containerId();
        ItemStack carried = packet.carried();

        DefaultedList<ItemStack> stacks = (DefaultedList<ItemStack>) packet.stacks();

        context.client().execute(() -> handle(context.player(), stateID, containerID, stacks, carried));
    }

}
