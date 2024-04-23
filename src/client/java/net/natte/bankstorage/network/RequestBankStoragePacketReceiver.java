package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;

public class RequestBankStoragePacketReceiver implements PlayPacketHandler<RequestBankStoragePacketS2C> {

    public void receive(RequestBankStoragePacketS2C packet, ClientPlayerEntity player,
            PacketSender responseSender) {
        CachedBankStorage bankStorage = packet.cachedBankStorage;
        CachedBankStorage.setBankStorage(bankStorage.uuid, bankStorage);

        if (bankStorage.uuid.equals(BankStorageClient.buildModePreviewRenderer.uuid)) {
            BankStorageClient.buildModePreviewRenderer.setBankStorage(bankStorage);

            int selectedSlot = BankStorageClient.buildModePreviewRenderer.options.selectedItemSlot;
            int newSelectedSlot = MathHelper.clamp(selectedSlot, 0, bankStorage.blockItems.size() - 1);
            if (newSelectedSlot != selectedSlot) {
                BankOptions options = BankStorageClient.buildModePreviewRenderer.options;
                options.selectedItemSlot = newSelectedSlot;
                responseSender.sendPacket(new UpdateBankOptionsPacketC2S(options));
            }
        }

    }
}
