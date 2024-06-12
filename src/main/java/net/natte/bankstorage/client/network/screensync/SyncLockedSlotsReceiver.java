package net.natte.bankstorage.client.network.screensync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.screen.BankScreenHandler;

public class SyncLockedSlotsReceiver implements PlayPayloadHandler<LockedSlotsPacketS2C> {

    @Override
    public void receive(LockedSlotsPacketS2C packet, Context context) {
        ClientPlayerEntity player = context.player();
        if (player.currentScreenHandler.syncId != packet.syncId())
            return;
        if (!(player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler))
            return;
        if (!(bankScreenHandler.inventory instanceof BankItemStorage))
            return;
        bankScreenHandler.setLockedSlotsNoSync(packet.lockedSlots());
    }
}
