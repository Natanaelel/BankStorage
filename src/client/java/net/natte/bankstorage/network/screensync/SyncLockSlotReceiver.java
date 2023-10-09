package net.natte.bankstorage.network.screensync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.packet.screensync.LockSlotPacketS2C;
import net.natte.bankstorage.screen.BankScreenHandler;

public class SyncLockSlotReceiver implements PlayPacketHandler<LockSlotPacketS2C> {

    public void receive(LockSlotPacketS2C packet, ClientPlayerEntity player, PacketSender response) {
        if (player.currentScreenHandler.syncId != packet.syncId)
            return;
        if (!(player.currentScreenHandler instanceof BankScreenHandler bankScreenHandler))
            return;
        if (!(bankScreenHandler.slots.get(packet.slot) instanceof BankSlot bankSlot))
            return;
        if(packet.shouldLock){
            bankSlot.lock(packet.stack);

        }else{
            bankSlot.unlock();
        }
    }
}
