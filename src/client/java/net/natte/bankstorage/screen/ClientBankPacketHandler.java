package net.natte.bankstorage.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.natte.bankstorage.network.screensync.S2CSyncContainerContents;
import net.natte.bankstorage.network.screensync.S2CSyncExtendedSlotContents;
import net.natte.bankstorage.network.screensync.S2CSyncInventory;
import net.natte.bankstorage.network.screensync.S2CSyncSelected;
import net.natte.bankstorage.packet.screensync.BankPacketHandler;

public class ClientBankPacketHandler {

    public static void registerClientMessages() {
        ClientPlayNetworking.registerGlobalReceiver(BankPacketHandler.sync_slot, new S2CSyncExtendedSlotContents());
        // ClientPlayNetworking.registerGlobalReceiver(BankPacketHandler.sync_ghost, new S2CSyncGhostItemPacket());
        ClientPlayNetworking.registerGlobalReceiver(BankPacketHandler.sync_data, new S2CSyncSelected());
        ClientPlayNetworking.registerGlobalReceiver(BankPacketHandler.sync_container, new S2CSyncContainerContents());
        ClientPlayNetworking.registerGlobalReceiver(BankPacketHandler.sync_inventory, new S2CSyncInventory());
    }
}