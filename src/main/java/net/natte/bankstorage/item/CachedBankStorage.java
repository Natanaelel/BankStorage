package net.natte.bankstorage.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class CachedBankStorage {

    public static Map<UUID, CachedBankStorage> BANK_CACHE = new HashMap<>();

    public static Set<UUID> bankRequestQueue = new HashSet<>();

    public List<ItemStack> items;

    public UUID uuid;

    public BankOptions options;

    private Random random;

    public CachedBankStorage(List<ItemStack> items, UUID uuid, BankOptions options, long randomSeed) {
        this.items = items;
        this.uuid = uuid;
        this.options = options;
        this.random = new Random(randomSeed);
    }

    public ItemStack getSelectedItem() {
        if (this.items.isEmpty())
            return ItemStack.EMPTY;
        return this.items.get(this.options.selectedItemSlot % this.items.size());
    }

    public ItemStack getRandomItem() {
        if (this.items.isEmpty())
            return ItemStack.EMPTY;
        return this.items.get(this.random.nextInt(this.items.size()));
    }

    public ItemStack chooseItemToPlace() {
        return switch (this.options.buildMode) {
            case NONE -> ItemStack.EMPTY;
            case NORMAL -> getSelectedItem();
            case RANDOM -> getRandomItem();
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
