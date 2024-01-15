package net.natte.bankstorage.packet;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.util.Util;

public class NetworkUtil {
    public static void syncCachedBankS2C(UUID uuid, ServerPlayerEntity player) {

        BankItemStorage bankItemStorage = Util.getBankItemStorage(uuid, player.getWorld());
        List<ItemStack> items = bankItemStorage.getBlockItems();

        ServerPlayNetworking.send(player, new RequestBankStoragePacketS2C(
                new CachedBankStorage(items, uuid, bankItemStorage.getRevision())));
    }
}
