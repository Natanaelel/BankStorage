package net.natte.bankstorage.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.state.BankPersistentState;
import net.natte.bankstorage.state.BankStateManager;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;
    public static boolean isBuildModeKeyUnBound = true;
    public static Random clientSyncedRandom;

    public static boolean isDebugMode = false;
    public static Consumer<PlayerEntity> onToggleBuildMode = e -> {};

    public static boolean isBank(ItemStack itemStack) {
        return itemStack.getItem() instanceof BankItem;
    }

    public static boolean isLink(ItemStack itemStack) {
        return itemStack.getItem() instanceof LinkItem;
    }

    public static boolean isBankLike(ItemStack itemStack) {
        return isBank(itemStack) || isLink(itemStack);
    }

    public static boolean isAllowedInBank(ItemStack itemStack) {
        return !isBankLike(itemStack) && itemStack.getItem().canBeNested();
    }

    public static boolean hasUUID(ItemStack itemStack) {
        return itemStack.contains(BankStorage.UUIDComponentType);
    }

    public static UUID getUUID(ItemStack itemStack) {
        return itemStack.get(BankStorage.UUIDComponentType);
    }

    public static BankOptions getOrCreateOptions(ItemStack itemStack) {
        BankOptions options = itemStack.get(BankStorage.OptionsComponentType);
        if (options == null) {
            options = new BankOptions();
            setOptions(itemStack, options);
        }
        return options;

    }

    public static void setOptions(ItemStack itemStack, BankOptions options) {
        itemStack.set(BankStorage.OptionsComponentType, options);
    }

    public static void sortBank(BankItemStorage bankItemStorage, ServerPlayerEntity player, SortMode sortMode) {

        // collect unique elements with *unlimited* stack size
        // and clear bank
        List<HugeItemStack> collectedItems = new ArrayList<>();
        for (int i = 0; i < bankItemStorage.size(); ++i) {
            ItemStack itemStack = bankItemStorage.heldStacks.get(i);
            bankItemStorage.setStack(i, ItemStack.EMPTY);
            boolean didExist = false;
            for (HugeItemStack existing : collectedItems) {
                if (ItemStack.areItemsAndComponentsEqual(itemStack, existing.stack)) {
                    existing.count += itemStack.getCount();
                    didExist = true;
                    break;
                }
            }
            if (!didExist && !itemStack.isEmpty())
                collectedItems.add(new HugeItemStack(itemStack.copyWithCount(1), (long) itemStack.getCount()));
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
            bankItemStorage
                    .getlockedSlots()
                    .keySet()
                    .stream()
                    .filter(index -> ItemStack.areItemsAndComponentsEqual(collectedItem.stack, bankItemStorage.getLockedStack(index)))
                    .sorted()                       // SETSTACK
                    .forEach(index -> bankItemStorage.setStack(index, collectedItem.split(slotSize)));
        }

        // fill empty bank slots one at a time
        for (HugeItemStack collectedItem : collectedItems) {

            if (collectedItem.count == 0)
                continue;
            for (int i = 0; i < bankItemStorage.size(); ++i) {
                if (bankItemStorage.getLockedStack(i) != null)
                    continue;
                ItemStack existingStack = bankItemStorage.getStack(i);
                if (existingStack.isEmpty()) {
                    // SETSTACK
                    bankItemStorage.setStack(i, collectedItem.split(slotSize));
                }
            }
        }

        // insert remaining items into player inventory or drop
        for (HugeItemStack collectedItem : collectedItems) {

            while (collectedItem.count > 0) {
                BankStorage.LOGGER.warn("Item does not fit in bank after sort. This *should* be impossible. item: "
                        + collectedItem.stack + " count: " + collectedItem.count);
                player.getInventory().offerOrDrop(collectedItem.split(collectedItem.stack.getMaxCount()));
            }
        }
    }

    /**
     * Doesn't Upgrade {@link BankType}.
     * Assumes {@link BankItemStorage} with this uuid already exists.
     */
    public static BankItemStorage getBankItemStorage(UUID uuid, World world) {

        BankPersistentState serverState = BankStateManager.getState(world.getServer());
        BankItemStorage bankItemStorage = serverState.get(uuid);

        return bankItemStorage;
    }

    /**
     * Returns null if unlinked {@link LinkItem} otherwise {@link BankItemStorage}.
     * Creates new {@link BankItemStorage} if stack has no uuid.
     * Upgrades {@link BankType} if needed
     */
    public static @Nullable BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        if (Util.isLink(bank)) {
            if (!Util.hasUUID(bank))
                return null;
            BankItemStorage bankItemStorage = getBankItemStorage(Util.getUUID(bank), world);
            if (bankItemStorage.type != LinkItem.getType(bank)) {
                LinkItem.setType(bank, bankItemStorage.type);
            }
            return bankItemStorage;
        }

        UUID uuid = hasUUID(bank) ? getUUID(bank) : UUID.randomUUID();
        if (!hasUUID(bank))
            bank.set(BankStorage.UUIDComponentType, uuid);

        BankType type = ((BankItem) bank.getItem()).getType();
        BankPersistentState serverState = BankStateManager.getState(world.getServer());
        BankItemStorage bankItemStorage = serverState.getOrCreate(uuid, type);
        return bankItemStorage;
    }

    public static Identifier ID(String path) {
        return new Identifier(BankStorage.MOD_ID, path);
    }

    public static @Nullable UUID getUUIDFromScreenHandler(ScreenHandler screenHandler) {
        if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
            return null;
        if (!(bankScreenHandler.inventory instanceof BankItemStorage bankItemStorage))
            return null;
        return bankItemStorage.uuid;
    }

    public static void invalid(PlayerEntity playerEntity) {
        playerEntity.sendMessage(invalid());
    }

    public static Text invalid() {
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL,
                Text.translatable("github_url.bankstorage").getString());

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                Text.translatable("open_github_url.bankstorage"));

        return Text.translatable("invalid.bankstorage")
                .append(Text.literal("\n§r"))
                .append(Text.translatable("github_url.bankstorage").styled(style -> style
                        .withHoverEvent(hoverEvent)
                        .withClickEvent(clickEvent)
                        .withUnderline(true)
                        .withColor(Formatting.BLUE)));
    }

    public static Text invalid(String context) {
        return invalid().copy().append(Text.literal(" context: " + context));
    }
}

// if somehow one bank has more than Integer.MAX_VALUE total of one item
class HugeItemStack {
    public ItemStack stack;
    public long count;

    public HugeItemStack(ItemStack stack, long count) {
        this.stack = stack;
        this.count = count;
    }

    public long getCount() {
        return this.count;
    }

    public String getName() {
        return this.stack.getName().getString();
    }

    public String getModName() {
        Identifier id = Registries.ITEM.getId(this.stack.getItem());
        if (id == null)
            return "";
        return id.getNamespace();
    }

    public ItemStack split(int count) {
        int toMove = (int) Math.min(this.count, count);
        this.count -= toMove;
        return this.stack.copyWithCount(toMove);
    }

}