package net.natte.bankstorage.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.util.Util;

import java.util.List;
import java.util.UUID;

public class NetworkUtil {
    public static void syncCachedBankS2C(UUID uuid, ServerPlayerEntity player) {
        syncCachedBankS2C(uuid, player, (short) -1);
    }

    public static void syncCachedBankS2C(UUID uuid, ServerPlayerEntity player, short optionsRevision) {

        BankItemStorage bankItemStorage = Util.getBankItemStorage(uuid, player.getWorld());
        List<ItemStack> items = bankItemStorage.getItems();
        ServerPlayNetworking.send(player, new RequestBankStoragePacketS2C(
                new CachedBankStorage(items, uuid, bankItemStorage.getRevision(), optionsRevision)));
    }

    public static void syncCachedBankIfBuildModeS2C(UUID uuid, ServerPlayerEntity player, ItemStack bankItem) {
        syncCachedBankIfBuildModeS2C(uuid, player, bankItem, (short) -1);
    }

    public static void syncCachedBankIfBuildModeS2C(UUID uuid, ServerPlayerEntity player, ItemStack bankItem,
            short optionsRevision) {
        if (!Util.getOrCreateOptions(bankItem).buildMode.isActive())
            return;
        syncCachedBankS2C(uuid, player, optionsRevision);
    }
}
