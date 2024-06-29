package net.natte.bankstorage.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class NetworkUtil {

    public static void syncCachedBankS2C(UUID uuid, ServerPlayer player) {

        BankItemStorage bankItemStorage = Util.getBankItemStorage(uuid);
        List<ItemStack> items = bankItemStorage.getItems();
        PacketDistributor.sendToPlayer(player, new RequestBankStoragePacketS2C(
                new CachedBankStorage(items, uuid, bankItemStorage.getRevision())));
    }
}
