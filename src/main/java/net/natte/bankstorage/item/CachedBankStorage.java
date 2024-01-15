package net.natte.bankstorage.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class CachedBankStorage {

    public static Map<UUID, CachedBankStorage> BANK_CACHE = new HashMap<>();

    public static Set<UUID> bankRequestQueue = new HashSet<>();

    private static Consumer<UUID> requestCacheUpdate = uuid -> {};

    public List<ItemStack> items;
    public UUID uuid;
    public short revision;


    public CachedBankStorage(List<ItemStack> items, UUID uuid, short revision) {
        this.items = items;
        this.uuid = uuid;
        this.revision = revision;
    }

    public static void requestCacheUpdate(UUID uuid){
        System.out.println("requesting cache update for " + uuid);
        requestCacheUpdate.accept(uuid);
    }
    
    public static void setCacheUpdater(Consumer<UUID> consumer){
        requestCacheUpdate = consumer;
    }

    public ItemStack getSelectedItem(int selectedItemSlot) {
        if (this.items.isEmpty())
            return ItemStack.EMPTY;
        return this.items.get(selectedItemSlot % this.items.size());
    }

    public ItemStack getRandomItem(Random random) {
        if (this.items.isEmpty())
            return ItemStack.EMPTY;
        return this.items.get(random.nextInt(this.items.size()));
    }

    public ItemStack chooseItemToPlace(BankOptions options, Random random) {
        return switch (options.buildMode) {
            case NONE -> ItemStack.EMPTY;
            case NORMAL -> getSelectedItem(options.selectedItemSlot);
            case RANDOM -> getRandomItem(random);
        };
    }

    @Nullable
    public static CachedBankStorage getBankStorage(ItemStack itemStack) {
        if (!Util.hasUUID(itemStack))
            return null;

        UUID uuid = Util.getUUID(itemStack);
        
        CachedBankStorage bankStorage = BANK_CACHE.get(uuid);

        if (bankStorage == null) {
            bankRequestQueue.add(uuid);
        }

        return bankStorage;
    }
}
