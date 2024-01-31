package net.natte.bankstorage.network;

import java.util.Random;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.util.Util;

public class SyncedRandomPacketReceiver implements PlayPacketHandler<SyncedRandomPacketS2C> {

    public void receive(SyncedRandomPacketS2C packet, ClientPlayerEntity player,
            PacketSender responseSender) {

        Util.clientSyncedRandom = new Random(packet.randomSeed);
    }
}
