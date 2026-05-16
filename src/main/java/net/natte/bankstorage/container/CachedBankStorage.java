package net.natte.bankstorage.container;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.item.BankStashResult;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * a client-side only representation of a bank storage containing:
 * <p>
 * - {@link #blockItems} stores all placeable items
 * <p>
 * - {@link #uuid} uuid of the bank
 * <p>
 * - {@link #revision} revision of the bank items
 * <p>
 */
public class CachedBankStorage {

    public static final StreamCodec<RegistryFriendlyByteBuf, CachedBankStorage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            o -> o.items,
            ByteBufCodecs.map(HashMap::newHashMap, ByteBufCodecs.INT, ItemStack.OPTIONAL_STREAM_CODEC),
            o -> o.lockedSlots,
            UUIDUtil.STREAM_CODEC,
            o -> o.uuid,
            ByteBufCodecs.SHORT,
            o -> o.revision,
            CachedBankStorage::new);

    private static final Map<UUID, CachedBankStorage> BANK_CACHE = new HashMap<>();

    public static final Set<UUID> bankRequestQueue = new HashSet<>();

    private static final Map<UUID, Integer> throddledQueue = new HashMap<>();


    private static Consumer<UUID> requestCacheUpdate = uuid -> {
    };
    public static boolean markDirtyForPreview = false;

    private final List<ItemStack> items;
    private final Map<Integer, ItemStack> lockedSlots;
    private final List<ItemStack> nonEmptyItems;
    private final List<ItemStack> blockItems;
    public final UUID uuid;
    public short revision;

    public CachedBankStorage(List<ItemStack> items, Map<Integer, ItemStack> lockedSlots, UUID uuid, short revision) {
        this.items = items;
        this.lockedSlots = lockedSlots;
        this.uuid = uuid;
        this.revision = revision;

        this.blockItems = items.stream().filter(stack -> stack.getItem() instanceof BlockItem).toList();
        this.nonEmptyItems = items.stream().filter(stack -> !stack.isEmpty()).toList();
    }

    public static void requestCacheUpdate(UUID uuid) {
        requestCacheUpdate.accept(uuid);
    }

    public static void setCacheUpdater(Consumer<UUID> consumer) {
        requestCacheUpdate = consumer;
    }

    @Nullable
    public static CachedBankStorage getAndThrottleUpdate(ItemStack stack, int ticks) {
        if (!Util.hasUUID(stack))
            return null;
        UUID uuid = Util.getUUID(stack);

        if (!throddledQueue.containsKey(uuid)) {
            throddledQueue.put(uuid, ticks);
            requestCacheUpdate(uuid);
        }
        return getBankStorage(uuid);
    }

    public static void advanceThrottledQueue() {
        throddledQueue.entrySet().removeIf(entity -> entity.getValue() <= 0);
        throddledQueue.replaceAll((uuid, ticksLeft) -> ticksLeft - 1);
    }

    public ItemStack getSelectedItem(int selectedItemSlot) {
        if (getBlockItems().isEmpty())
            return ItemStack.EMPTY;
        return getBlockItems().get(Mth.clamp(selectedItemSlot, 0, getBlockItems().size() - 1));
    }

    public ItemStack getRandomItem(Random random) {
        if (getBlockItems().isEmpty())
            return ItemStack.EMPTY;
        return getBlockItems().get(random.nextInt(getBlockItems().size()));
    }

    public ItemStack chooseItemToPlace(BankOptions options, Random random, int selectedSlot) {
        return switch (options.buildMode()) {
            case NONE_NORMAL, NONE_RANDOM -> ItemStack.EMPTY;
            case NORMAL -> getSelectedItem(selectedSlot);
            case RANDOM -> getRandomItem(random);
        };
    }

    @Nullable
    public static CachedBankStorage getBankStorage(ItemStack itemStack) {
        if (!Util.hasUUID(itemStack))
            return null;

        UUID uuid = Util.getUUID(itemStack);

        return CachedBankStorage.getBankStorage(uuid);
    }

    @Nullable
    public static CachedBankStorage getBankStorage(UUID uuid) {

        CachedBankStorage bankStorage = BANK_CACHE.get(uuid);

        if (bankStorage == null)
            requestCacheUpdate(uuid);

        return bankStorage;
    }

    public static void setBankStorage(UUID uuid, CachedBankStorage bankStorage) {
        BANK_CACHE.put(uuid, bankStorage);
        markDirtyForPreview = true;
    }


    public List<ItemStack> getBlockItems() {
        return this.blockItems;
    }

    public List<ItemStack> getNonEmptyItems() {
        return this.nonEmptyItems;
    }

    public IItemHandler getReadOnlyItemHandler() {
        return new ClientReadOnlyItemHandler(this);
    }

    /**
     * Checks whether the given item stack can be stashed into this bank.
     * Used on the client side to render inventory interaction indicators.
     *
     * @param stackToInsert the item being held on the cursor
     * @param stackLimit the maximum items per slot in this bank type
     * @return the stash result indicating match, space, or no space
     */
    public BankStashResult getItemStashable(ItemStack stackToInsert, int stackLimit) {
        ItemStack remainder = getStashRemainder(stackToInsert, stackLimit);
        if (remainder.getCount() == stackToInsert.getCount()) {
            return BankStashResult.NO_SPACE;
        }

        for (ItemStack slotStack : items) {
            if (ItemStack.isSameItemSameComponents(slotStack, stackToInsert)
                    && slotStack.getCount() < stackLimit) {
                return BankStashResult.MATCH_AND_SPACE;
            }
        }

        for (Map.Entry<Integer, ItemStack> lockedSlot : lockedSlots.entrySet()) {
            ItemStack lockedStack = lockedSlot.getValue();
            if (ItemStack.isSameItemSameComponents(lockedStack, stackToInsert)
                    && getSlotCount(lockedSlot.getKey()) < stackLimit) {
                return BankStashResult.MATCH_AND_SPACE;
            }
        }

        return BankStashResult.SPACE;
    }

    public ItemStack getStashRemainder(ItemStack stackToInsert, int stackLimit) {
        if (stackToInsert.isEmpty() || Util.isDisallowedInBank(stackToInsert)) {
            return stackToInsert;
        }

        int originalCount = stackToInsert.getCount();
        int remaining = originalCount;
        int[] simulatedCounts = items.stream().mapToInt(ItemStack::getCount).toArray();

        remaining = simulateInsertIntoLockedSlots(stackToInsert, remaining, stackLimit, simulatedCounts);
        remaining = simulateInsertIntoNonEmptySlots(stackToInsert, remaining, stackLimit, simulatedCounts);
        remaining = simulateInsertIntoAnySlots(stackToInsert, remaining, stackLimit, simulatedCounts);

        if (remaining == originalCount) {
            return stackToInsert;
        }
        return remaining == 0 ? ItemStack.EMPTY : stackToInsert.copyWithCount(remaining);
    }

    private int simulateInsertIntoLockedSlots(ItemStack stackToInsert, int remaining, int stackLimit,
                                              int[] simulatedCounts) {
        for (Map.Entry<Integer, ItemStack> lockedSlot : lockedSlots.entrySet()) {
            if (remaining == 0) {
                break;
            }
            if (ItemStack.isSameItemSameComponents(lockedSlot.getValue(), stackToInsert)) {
                remaining = simulateInsertIntoSlot(lockedSlot.getKey(), remaining, stackLimit, simulatedCounts);
            }
        }
        return remaining;
    }

    private int simulateInsertIntoNonEmptySlots(ItemStack stackToInsert, int remaining, int stackLimit,
                                                int[] simulatedCounts) {
        for (int slot = 0; slot < items.size(); ++slot) {
            if (remaining == 0) {
                break;
            }
            ItemStack slotStack = items.get(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stackToInsert)) {
                remaining = simulateInsertIntoSlot(slot, remaining, stackLimit, simulatedCounts);
            }
        }
        return remaining;
    }

    private int simulateInsertIntoAnySlots(ItemStack stackToInsert, int remaining, int stackLimit,
                                           int[] simulatedCounts) {
        for (int slot = 0; slot < items.size(); ++slot) {
            if (remaining == 0) {
                break;
            }
            ItemStack lockedStack = lockedSlots.get(slot);
            ItemStack slotStack = items.get(slot);
            if (lockedStack != null
                    ? ItemStack.isSameItemSameComponents(lockedStack, stackToInsert)
                    : slotStack.isEmpty() || ItemStack.isSameItemSameComponents(slotStack, stackToInsert)) {
                remaining = simulateInsertIntoSlot(slot, remaining, stackLimit, simulatedCounts);
            }
        }
        return remaining;
    }

    private int simulateInsertIntoSlot(int slot, int remaining, int stackLimit, int[] simulatedCounts) {
        if (slot < 0 || slot >= simulatedCounts.length) {
            return remaining;
        }
        int inserted = Math.min(Math.max(0, stackLimit - simulatedCounts[slot]), remaining);
        simulatedCounts[slot] += inserted;
        return remaining - inserted;
    }

    private int getSlotCount(int slot) {
        if (slot < 0 || slot >= items.size()) {
            return 0;
        }
        return items.get(slot).getCount();
    }
}

// used for building gadgets preview etc
class ClientReadOnlyItemHandler implements IItemHandler {

    private final CachedBankStorage cachedBankStorage;

    public ClientReadOnlyItemHandler(CachedBankStorage cachedBankStorage) {
        this.cachedBankStorage = cachedBankStorage;
    }

    @Override
    public int getSlots() {
        return cachedBankStorage.getBlockItems().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return cachedBankStorage.getBlockItems().get(slot);
    }

    // can't insert: remainder = input
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    // can't extract: extracted = empty
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0; // Hello this maybe works :)
    }

    // nothing is valid lol
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}
