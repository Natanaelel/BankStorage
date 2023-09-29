package net.natte.bankstorage.network.screensync;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class SyncLargeSlotInventoryS2C implements PlayChannelHandler {

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
            PacketSender responseSender) {

        int stateID = buf.readInt();
        int containerID = buf.readInt();
        ItemStack carried = buf.readItemStack();

        int i = buf.readShort();
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(i, ItemStack.EMPTY);

        for (int j = 0; j < i; ++j) {
            stacks.set(j, Util.readLargeStack(buf));
        }
        client.execute(() -> handle(client.player, stateID, containerID, stacks, carried));
    }

    public void handle(@Nullable ClientPlayerEntity player, int stateID, int windowId, DefaultedList<ItemStack> stacks,
            ItemStack carried) {
        if (player != null && player.currentScreenHandler instanceof BankScreenHandler
                && windowId == player.currentScreenHandler.syncId) {
            player.currentScreenHandler.updateSlotStacks(stateID, stacks, carried);

        }
    }

}
