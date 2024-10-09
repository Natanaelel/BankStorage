package net.natte.bankstorage.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.state.BankStateManager;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;
    // used on client, player attachment used on server
    public static KeyBindInfo keybindInfo = new KeyBindInfo(false, false);
    public static Random clientSyncedRandom;

    public static final ThreadLocal<Boolean> isClient = ThreadLocal.withInitial(() -> false);

    public static boolean isBank(ItemStack itemStack) {
        return itemStack.getItem() instanceof BankItem;
    }

    public static boolean isLink(ItemStack itemStack) {
        return itemStack.getItem() instanceof LinkItem;
    }

    public static boolean isBankLike(ItemStack itemStack) {
        return isBank(itemStack) || isLink(itemStack);
    }

    public static boolean isDisallowedInBank(ItemStack itemStack) {
        return !itemStack.getItem().canFitInsideContainerItems();
    }

    public static boolean hasUUID(ItemStack itemStack) {
        return itemStack.has(BankStorage.UUIDComponentType);
    }

    public static UUID getUUID(ItemStack itemStack) {
        return itemStack.get(BankStorage.UUIDComponentType);
    }

    public static BankType getType(ItemStack stack) {
        if (stack.getItem() instanceof BankItem bankItem)
            return bankItem.getType();
        return stack.getOrDefault(BankStorage.BankTypeComponentType, BankStorage.BANK_TYPES[0]);
    }

    public static BankOptions getOrCreateOptions(ItemStack itemStack) {
        BankOptions options = itemStack.get(BankStorage.OptionsComponentType);
        if (options == null) {
            options = BankOptions.DEFAULT;
            setOptions(itemStack, options);
        }
        return options;
    }

    public static void setOptions(ItemStack itemStack, BankOptions options) {
        itemStack.set(BankStorage.OptionsComponentType, options);
    }

    public static void sortBank(BankItemStorage bankItemStorage, ServerPlayer player, SortMode sortMode) {

        // collect unique elements with *unlimited* stack size
        // and clear bank
        List<HugeItemStack> collectedItems = new ArrayList<>();
        for (int i = 0; i < bankItemStorage.size(); ++i) {
            ItemStack itemStack = bankItemStorage.getItems().get(i);
            bankItemStorage.getItems().set(i, ItemStack.EMPTY);
            boolean didExist = false;
            for (HugeItemStack existing : collectedItems) {
                if (ItemStack.isSameItemSameComponents(itemStack, existing.stack)) {
                    existing.count += itemStack.getCount();
                    didExist = true;
                    break;
                }
            }
            if (!didExist && !itemStack.isEmpty())
                collectedItems.add(new HugeItemStack(itemStack.copyWithCount(1), itemStack.getCount()));
        }

        // sort
        switch (sortMode) {
            case COUNT:
                collectedItems.sort(Comparator.comparingLong(HugeItemStack::getCount).reversed());
                break;
            case NAME:
                collectedItems.sort(Comparator.comparing(HugeItemStack::getName));
                break;
            case MOD:
                collectedItems.sort(Comparator.comparing(HugeItemStack::getModName));
                break;
        }

        int slotSize = bankItemStorage.getMaxCountPerStack();

        // first fill locked slots with their item
        for (HugeItemStack collectedItem : collectedItems) {
            bankItemStorage.getLockedSlots().keySet().stream().filter(index -> ItemStack.isSameItemSameComponents(collectedItem.stack, bankItemStorage.getLockedStack(index))).sorted()
                    .forEach(index -> bankItemStorage.getItems().set(index, collectedItem.split(slotSize)));
        }

        // fill empty bank slots one at a time
        for (HugeItemStack collectedItem : collectedItems) {

            if (collectedItem.count == 0)
                continue;
            for (int i = 0; i < bankItemStorage.size(); ++i) {
                if (bankItemStorage.getLockedStack(i) != null)
                    continue;
                ItemStack existingStack = bankItemStorage.getItems().get(i);
                if (existingStack.isEmpty()) {
                    bankItemStorage.getItems().set(i, collectedItem.split(slotSize));
                }
            }
        }

        // insert remaining items into player inventory or drop
        for (HugeItemStack collectedItem : collectedItems) {

            while (collectedItem.count > 0) {
                BankStorage.LOGGER.warn("Item does not fit in bank after sort. This *should* be impossible. item: {} count: {}", collectedItem.stack, collectedItem.count);
                player.getInventory().placeItemBackInInventory(collectedItem.split(collectedItem.stack.getMaxStackSize()));
            }
        }
    }

    /**
     * Doesn't Upgrade {@link BankType}.
     * Assumes {@link BankItemStorage} with this uuid already exists.
     */
    public static BankItemStorage getBankItemStorage(UUID uuid) {
        return BankStateManager.getState().get(uuid);
    }

    /**
     * Returns null if unlinked {@link LinkItem} otherwise {@link BankItemStorage}.
     * Creates new {@link BankItemStorage} if needed.
     * Upgrades {@link BankType} if needed
     */
    @Nullable
    public static BankItemStorage getBankItemStorage(ItemStack bank) {

        UUID uuid = Util.isBank(bank) ? getOrSetUUID(bank) : getUUID(bank);

        if (uuid == null)
            return null; // unlinked link

        BankType type = getType(bank);
        BankItemStorage bankItemStorage = BankStateManager.getState().getOrCreate(uuid, type);
        bank.set(BankStorage.BankTypeComponentType, bankItemStorage.type());
        if (bankItemStorage.type() != type && isLink(bank))
            bank.set(BankStorage.BankTypeComponentType, bankItemStorage.type());

        return bankItemStorage;
    }

    public static UUID getOrSetUUID(ItemStack bank) {
        UUID uuid = bank.get(BankStorage.UUIDComponentType);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            bank.set(BankStorage.UUIDComponentType, uuid);
        }
        return uuid;

    }

    public static ResourceLocation ID(String path) {
        return ResourceLocation.fromNamespaceAndPath(BankStorage.MOD_ID, path);
    }

    public static IItemHandler getItemHandlerFromItem(ItemStack itemStack, Void ignored) {
        if (isClient())
            return getClientItemHandlerFromItem(itemStack);
        else
            return getServerItemHandlerFromItem(itemStack);
    }

    private static IItemHandler getClientItemHandlerFromItem(ItemStack itemStack) {
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(itemStack);
        if (cachedBankStorage == null)
            return null;
        return cachedBankStorage.getReadOnlyItemHandler();
    }

    private static IItemHandler getServerItemHandlerFromItem(ItemStack itemStack) {
        BankItemStorage bankItemStorage = getBankItemStorage(itemStack);
        if (bankItemStorage == null)
            return null;
        return bankItemStorage.getItemHandler(getPickupMode(itemStack));
    }

    private static PickupMode getPickupMode(ItemStack itemStack) {
        return itemStack.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT).pickupMode();
    }

    public static boolean isClient() {
        return isClient.get();
    }

    public static boolean isBuildModeToggleKeyBound(Player player) {
        return (player.level().isClientSide() ? Util.keybindInfo : player.getData(BankStorage.KEYBIND_INFO_ATTACHMENT)).hasBuildModeToggleKey();
    }

    public static boolean isBuildModeCycleKeyBound(Player player) {
        return (player.level().isClientSide() ? Util.keybindInfo : player.getData(BankStorage.KEYBIND_INFO_ATTACHMENT)).hasBuildModeCycleKey();
    }
}

// if somehow one bank has more than Integer.MAX_VALUE total of one item
class HugeItemStack {
    public final ItemStack stack;
    public long count;

    public HugeItemStack(ItemStack stack, long count) {
        this.stack = stack;
        this.count = count;
    }

    public long getCount() {
        return this.count;
    }

    public String getName() {
        return this.stack.getHoverName().getString();
    }

    public String getModName() {
        return BuiltInRegistries.ITEM.getKey(this.stack.getItem()).getNamespace();
    }

    public ItemStack split(int count) {
        int toMove = (int) Math.min(this.count, count);
        this.count -= toMove;
        return this.stack.copyWithCount(toMove);
    }

}