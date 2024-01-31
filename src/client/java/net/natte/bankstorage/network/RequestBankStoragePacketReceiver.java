package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.util.Util;

public class RequestBankStoragePacketReceiver implements PlayPacketHandler<RequestBankStoragePacketS2C> {

    public void receive(RequestBankStoragePacketS2C packet, ClientPlayerEntity player,
            PacketSender responseSender) {
        System.out.println("received cache update");
        CachedBankStorage bankStorage = packet.cachedBankStorage;
        CachedBankStorage.setBankStorage(bankStorage.uuid, bankStorage);
        // System.out.println("");
        if (bankStorage.uuid.equals(BankStorageClient.buildModePreviewRenderer.uuid)) {
            BankStorageClient.buildModePreviewRenderer.setBankStorage(bankStorage);

            if (packet.cachedBankStorage.optionsRevision != -1) {
                BankStorageClient.buildModePreviewRenderer.options = Util.getOptions(player.getMainHandStack());
            }
        }

    }
}
