package net.natte.bankstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.packet.OptionPacketS2C;

public class OptionPacketReceiver implements PlayPacketHandler<OptionPacketS2C> {

    public void receive(OptionPacketS2C packet, ClientPlayerEntity player,
            PacketSender responseSender) {
        CachedBankStorage cachedBankStorage = CachedBankStorage.BANK_CACHE.get(packet.uuid);
        if (cachedBankStorage != null) {
            cachedBankStorage.options = BankOptions.fromNbt(packet.nbt);
            return;
        }

    }
}