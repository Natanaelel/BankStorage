package net.natte.bankstorage.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
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
import net.natte.bankstorage.access.KeyBindInfoAccess;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.world.BankStateSaverAndLoader;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;
    public static Random clientSyncedRandom;
    public static KeyBindInfo keyBindInfo = new KeyBindInfo(false, false);

    public static Consumer<PlayerEntity> onToggleBuildMode = e -> {
    };
    public static Consumer<PlayerEntity> onCycleBuildMode = e -> {
    };

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

    public static boolean canCombine(ItemStack left, ItemStack right) {
        return left.getItem() == right.getItem() && Objects.equals(left.getNbt(), right.getNbt());
    }

    public static boolean hasUUID(ItemStack itemStack) {
        if (!itemStack.hasNbt())
            return false;
        if (!itemStack.getNbt().contains(BankItem.UUID_KEY))
            return false;
        return true;
    }

    public static UUID getUUID(ItemStack itemStack) {
        return itemStack.getNbt().getUuid(BankItem.UUID_KEY);
    }

    public static boolean hasOptions(ItemStack itemStack) {
        if (!itemStack.hasNbt())
            return false;
        if (!itemStack.getNbt().contains(BankItem.OPTIONS_KEY))
            return false;
        return true;
    }

    public static BankOptions getOptions(ItemStack itemStack) {
        return BankOptions.fromNbt(itemStack.getNbt().getCompound(BankItem.OPTIONS_KEY));
    }

    public static BankOptions getOrCreateOptions(ItemStack itemStack) {
        if (hasOptions(itemStack)) {
            return getOptions(itemStack);
        } else {
            return new BankOptions();
        }
    }

    public static void setOptions(ItemStack itemStack, BankOptions options) {
        itemStack.getOrCreateNbt().put(BankItem.OPTIONS_KEY, options.asNbt());
    }

    public static NbtCompound largeStackAsNbt(ItemStack itemStack) {

        NbtCompound nbt = new NbtCompound();

        Identifier identifier = Registries.ITEM.getId(itemStack.getItem());
        nbt.putString("id", identifier == null ? "minecraft:air" : identifier.toString());
        nbt.putInt("Count", itemStack.getCount());

        if (itemStack.getNbt() != null)
            nbt.put("tag", itemStack.getNbt().copy());

        return nbt;
    }

    public static ItemStack largeStackFromNbt(NbtCompound nbt) {

        ItemStack itemStack = Registries.ITEM.get(new Identifier(nbt.getString("id"))).getDefaultStack();
        itemStack.setCount(nbt.getInt("Count"));

        if (nbt.contains("tag", NbtElement.COMPOUND_TYPE))
            itemStack.setNbt(nbt.getCompound("tag"));

        return itemStack;
    }

    public static ItemStack readLargeStack(PacketByteBuf buf) {
        return Util.largeStackFromNbt(buf.readNbt());
    }

    public static void writeLargeStack(PacketByteBuf buf, ItemStack stack) {
        buf.writeNbt(Util.largeStackAsNbt(stack));
    }

    public static void sortBank(BankItemStorage bankItemStorage, ServerPlayerEntity player, SortMode sortMode) {

        // collect unique elements with *unlimited* stack size
        // and clear bank
        List<HugeItemStack> collectedItems = new ArrayList<>();
        for (int i = 0; i < bankItemStorage.size(); ++i) {
            ItemStack itemStack = bankItemStorage.stacks.get(i);
            bankItemStorage.setStack(i, ItemStack.EMPTY);
            boolean didExist = false;
            for (HugeItemStack existing : collectedItems) {
                if (ItemStack.canCombine(itemStack, existing.stack)) {
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
                    .filter(index -> Util.canCombine(collectedItem.stack, bankItemStorage.getLockedStack(index)))
                    .sorted()
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

    public static BankItemStorage getBankItemStorage(UUID uuid, World world) {

        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerStateSaverAndLoader(world.getServer());
        BankItemStorage bankItemStorage = serverState.get(uuid);

        return bankItemStorage;
    }

    /**
     * @return null if unlinked {@link LinkItem} otherwise {@link BankItemStorage}
     */
    public static @Nullable BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        if (Util.isLink(bank)) {
            if (!Util.hasUUID(bank))
                return null;
            BankItemStorage bankItemStorage = getBankItemStorage(Util.getUUID(bank), world);
            if (bankItemStorage.type != LinkItem.getType(bank)) {
                LinkItem.setTypeName(bank, bankItemStorage.type.getName());
            }
            return bankItemStorage;
        }

        UUID uuid = hasUUID(bank) ? getUUID(bank) : UUID.randomUUID();
        if (!hasUUID(bank))
            bank.getOrCreateNbt().putUuid(BankItem.UUID_KEY, uuid);

        BankType type = ((BankItem) bank.getItem()).getType();
        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerStateSaverAndLoader(world.getServer());
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

    public static UUID getOrSetUUID(ItemStack bank) {
        if (hasUUID(bank))
            return getUUID(bank);

        UUID uuid = UUID.randomUUID();
        bank.getOrCreateNbt().putUuid(BankItem.UUID_KEY, uuid);
        return uuid;
    }

    public static boolean isBuildModeToggleKeyBound(PlayerEntity player) {
        return (player.getWorld().isClient ? Util.keyBindInfo : ((KeyBindInfoAccess) player).bankstorge$getKeyBindInfo()).hasBuildModeToggleKey();
    }

    public static boolean isBuildModeCycleKeyBound(PlayerEntity player) {
        return (player.getWorld().isClient ? Util.keyBindInfo : ((KeyBindInfoAccess) player).bankstorge$getKeyBindInfo()).hasBuildModeCycleKey();
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