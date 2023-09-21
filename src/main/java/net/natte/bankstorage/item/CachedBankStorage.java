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

public class CachedBankStorage {

    public static Map<UUID, CachedBankStorage> BANK_CACHE = new HashMap<>();

    // public static List<ItemStack> bankRequestQueue = new ArrayList<>();
    public static Set<UUID> bankRequestQueue = new HashSet<>();

    public List<ItemStack> items;

    // public int selectedItemSlot;
    public UUID uuid;
    public ItemStack bankItemStack;

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

    @Nullable
    public static CachedBankStorage getBankStorage(ItemStack itemStack) {
        if (!itemStack.hasNbt())
            return null;
        if (!itemStack.getNbt().contains(BankItem.UUID_KEY))
            return null;

        UUID uuid = itemStack.getNbt().getUuid(BankItem.UUID_KEY);

        CachedBankStorage bankStorage = BANK_CACHE.get(uuid);

        if (bankStorage == null) {
            bankRequestQueue.add(uuid);
        }

        return bankStorage;
    }
}
