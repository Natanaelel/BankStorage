package net.natte.bankstorage.packet;

import java.util.List;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.util.Util;

public class NetworkUtil {

    public static void syncCachedBankS2C(UUID uuid, ServerPlayer player) {

        BankItemStorage bankItemStorage = Util.getBankItemStorage(uuid);
        List<ItemStack> items = bankItemStorage.getItems();
        player.connection.send(new RequestBankStoragePacketS2C(
                new CachedBankStorage(items, uuid, bankItemStorage.getRevision())));
    }

//    public static void syncCachedBankIfBuildModeS2C(UUID uuid, ServerPlayer player, ItemStack bankItem) {
//        if (Util.getOrCreateOptions(bankItem).buildMode == BuildMode.NONE)
//            return;
//        syncCachedBankS2C(uuid, player);
//    }
}
