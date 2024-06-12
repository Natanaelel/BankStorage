package net.natte.bankstorage.client.network;

import java.util.Random;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;

import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.util.Util;

public class SyncedRandomPacketReceiver implements PlayPayloadHandler<SyncedRandomPacketS2C> {

    @Override
    public void receive(SyncedRandomPacketS2C packet, Context context) {
        Util.clientSyncedRandom = new Random(packet.randomSeed());
    }
}
