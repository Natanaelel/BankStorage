package net.natte.bankstorage.screen;

import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.screensync.SyncContainerPacketS2C;
import net.natte.bankstorage.packet.screensync.SyncLargeSlotPacketS2C;
import net.natte.bankstorage.util.Util;

/*
 * Credit Tfarcenim
 * https://github.com/Tfarcenim/DankStorageFabric/blob/1.19.x/src/main/java/tfar/dankstorage/container/CustomSync.java#L12 
 */
// TODO: is this needed anymore?
public class BankScreenSync implements ScreenHandlerSyncHandler {

    private ServerPlayerEntity player;

    public BankScreenSync(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void updateState(ScreenHandler screenHandler, DefaultedList<ItemStack> inventory, ItemStack stack,
            int[] indices) {
        SyncContainerPacketS2C.sendSyncContainer(player, screenHandler.nextRevision(),
                screenHandler.syncId, inventory, stack);
        Map<Integer, ItemStack> lockedSlots = ((BankItemStorage) ((BankScreenHandler) screenHandler).inventory)
                .getlockedSlots();

        ServerPlayNetworking.send(player, new LockedSlotsPacketS2C(screenHandler.syncId, lockedSlots));
                

        for (int i = 0; i < indices.length; ++i) {
            this.sendPropertyUpdate(screenHandler, i, indices[i]);
        }

    }

    @Override
    public void updateSlot(ScreenHandler screenHandler, int slot, ItemStack stack) {
        // BankSyncPacketHandler.sendSyncSlot(player, screenHandler.syncId, slot, stack);
        ServerPlayNetworking.send(player, new SyncLargeSlotPacketS2C(screenHandler.syncId, slot, stack));
        NetworkUtil.syncCachedBankS2C(Util.getUUIDFromScreenHandler(screenHandler), this.player);
    }

    @Override
    public void updateCursorStack(ScreenHandler screenHandler, ItemStack stack) {
        player.networkHandler
                .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, player.currentScreenHandler.nextRevision(), -1,
                        stack));
    }

    public void syncLockedSlots(ScreenHandler screenHandler, Map<Integer, ItemStack> lockedSlots) {
        ServerPlayNetworking.send(this.player, new LockedSlotsPacketS2C(screenHandler.syncId, lockedSlots));
    }

    @Override
    public void updateProperty(ScreenHandler screenHandler, int i, int j) {
        this.sendPropertyUpdate(screenHandler, i, j);
    }

    private void sendPropertyUpdate(ScreenHandler screenHandler, int i, int j) {
        player.networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(screenHandler.syncId, i, j));
    }
}
