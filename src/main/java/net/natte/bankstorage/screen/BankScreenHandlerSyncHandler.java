package net.natte.bankstorage.screen;

import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;

public class BankScreenHandlerSyncHandler implements ContainerSynchronizer {
    private final ContainerSynchronizer screenHandlerSyncHandler;
    private final ServerPlayer player;

    public BankScreenHandlerSyncHandler(ContainerSynchronizer original, ServerPlayer player) {
        this.screenHandlerSyncHandler = original;
        this.player = player;
    }


    public void syncLockedSlots(AbstractContainerMenu screenHandler, Map<Integer, ItemStack> lockedSlots) {
        player.connection.send(new LockedSlotsPacketS2C(screenHandler.containerId, lockedSlots));
    }

    @Override
    public void sendInitialData(AbstractContainerMenu screenHandler, NonNullList<ItemStack> items, ItemStack carriedItem, int[] initialData) {
        screenHandlerSyncHandler.sendInitialData(screenHandler, items, carriedItem, initialData);
        syncLockedSlots(screenHandler,  ((BankScreenHandler) screenHandler).getLockedSlots());
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu screenHandler, int slot, ItemStack itemStack) {
        screenHandlerSyncHandler.sendSlotChange(screenHandler, slot, itemStack);
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu screenHandlerMenu, ItemStack itemStack) {
        screenHandlerSyncHandler.sendCarriedChange(screenHandlerMenu, itemStack);
    }

    @Override
    public void sendDataChange(AbstractContainerMenu screenHandler, int property, int value) {
        screenHandlerSyncHandler.sendDataChange(screenHandler, property, value);
    }
}
