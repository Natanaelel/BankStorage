package net.natte.bankstorage.screen;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.screensync.BankSyncPacketHandler;

/*
 * Credit Tfarcenim
 * https://github.com/Tfarcenim/DankStorageFabric/blob/1.19.x/src/main/java/tfar/dankstorage/container/CustomSync.java#L12 
 */
public class BankScreenSync implements ScreenHandlerSyncHandler {

    private ServerPlayerEntity player;

    public BankScreenSync(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void updateState(ScreenHandler screenHandler, DefaultedList<ItemStack> inventory, ItemStack stack,
            int[] indices) {
        BankSyncPacketHandler.sendSyncContainer(player, screenHandler.nextRevision(),
                screenHandler.syncId, inventory, stack);
                Map<Integer, ItemStack> lockedSlots = ((BankItemStorage)((BankScreenHandler) screenHandler).inventory).getlockedSlots();
        BankSyncPacketHandler.sendSyncLockedSlots(player, screenHandler.syncId, lockedSlots);

        
        for (int i = 0; i < indices.length; ++i) {
            this.broadcastDataValue(screenHandler, i, indices[i]);
        }

    }

    @Override
    public void updateSlot(ScreenHandler screenHandler, int slot, ItemStack stack) {
        BankSyncPacketHandler.sendSyncSlot(player, screenHandler.syncId, slot, stack);

    }

    @Override
    public void updateCursorStack(ScreenHandler screenHandler, ItemStack stack) {
        player.networkHandler
                .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, player.currentScreenHandler.nextRevision(), -1,
                        stack));
    }

    @Override
    public void updateProperty(ScreenHandler screenHandler, int i, int j) {
        this.broadcastDataValue(screenHandler, i, j);
    }

    private void broadcastDataValue(ScreenHandler screenHandler, int i, int j) {
        player.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(screenHandler.syncId, i, j));
    }
}
