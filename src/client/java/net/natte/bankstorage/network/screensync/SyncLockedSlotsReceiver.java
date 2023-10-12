package net.natte.bankstorage.network.screensync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.screen.BankScreenHandler;

public class SyncLockedSlotsReceiver implements PlayPacketHandler<LockedSlotsPacketS2C> {

    public void receive(LockedSlotsPacketS2C packet, ClientPlayerEntity player, PacketSender response) {
        if (player.currentScreenHandler.syncId != packet.syncId)
            return;
        if (!(player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler))
            return;
        if (!(bankScreenHandler.inventory instanceof BankItemStorage bankItemStorage))
            return;
        bankScreenHandler.setLockedSlotsNoSync(packet.lockedSlots);
    }
}
