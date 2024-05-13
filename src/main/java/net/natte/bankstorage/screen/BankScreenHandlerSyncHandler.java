package net.natte.bankstorage.screen;

import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;

public class BankScreenHandlerSyncHandler implements ScreenHandlerSyncHandler {
    private final ScreenHandlerSyncHandler screenHandlerSyncHandler;
    private final ServerPlayerEntity player;

    public BankScreenHandlerSyncHandler(ScreenHandlerSyncHandler original, ServerPlayerEntity player) {
        this.screenHandlerSyncHandler = original;
        this.player = player;
    }

    @Override
    public void updateState(ScreenHandler screenHandler, DefaultedList<ItemStack> items, ItemStack cursorStack,
            int[] indices) {
        screenHandlerSyncHandler.updateState(screenHandler, items, cursorStack, indices);

        syncLockedSlots(screenHandler,
                ((BankItemStorage) ((BankScreenHandler) screenHandler).inventory).getlockedSlots());

    }

    @Override
    public void updateSlot(ScreenHandler screenHandler, int slot, ItemStack stack) {
        screenHandlerSyncHandler.updateSlot(screenHandler, slot, stack);
    }

    @Override
    public void updateCursorStack(ScreenHandler screenHandler, ItemStack stack) {
        screenHandlerSyncHandler.updateCursorStack(screenHandler, stack);
    }

    @Override
    public void updateProperty(ScreenHandler screenHandler, int progerty, int stack) {
        screenHandlerSyncHandler.updateProperty(screenHandler, progerty, stack);
    }

    public void syncLockedSlots(ScreenHandler screenHandler, Map<Integer, ItemStack> lockedSlots) {
        ServerPlayNetworking.send(player, new LockedSlotsPacketS2C(screenHandler.syncId, lockedSlots));
    }
}
