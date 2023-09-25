package net.natte.bankstorage.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

public class Util {

    public static boolean isBank(ItemStack itemStack) {
        return itemStack.getItem() instanceof BankItem;
    }

    public static boolean isAllowedInBank(ItemStack itemStack) {
        return !isBank(itemStack);
    }

    public static UUID getOrCreateUuid(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getOrCreateNbt();
        UUID uuid;
        if (nbt.contains(BankItem.UUID_KEY)) {
            uuid = nbt.getUuid(BankItem.UUID_KEY);
        } else {
            uuid = UUID.randomUUID();
            nbt.putUuid(BankItem.UUID_KEY, uuid);
        }
        return uuid;
    }

    public static UUID getUUID(ItemStack itemStack) {
        return itemStack.getNbt().getUuid(BankItem.UUID_KEY);
    }

    public static boolean hasUUID(ItemStack itemStack) {
        if (!itemStack.hasNbt())
            return false;
        if (!itemStack.getNbt().contains(BankItem.UUID_KEY))
            return false;
        return true;
    }

    public static NbtCompound largeStackAsNbt(ItemStack itemStack) {

        NbtCompound nbt = new NbtCompound();

        Identifier identifier = Registries.ITEM.getId(itemStack.getItem());
        nbt.putString("id", identifier == null ? "minecraft:air" : identifier.toString());
        nbt.putInt("Count", itemStack.getCount());

        if (itemStack.getNbt() != null) {
            nbt.put("tag", itemStack.getNbt().copy());
        }
        return nbt;
    }

    public static ItemStack largeStackFromNbt(NbtCompound nbt) {

        ItemStack itemStack = Registries.ITEM.get(new Identifier(nbt.getString("id"))).getDefaultStack();
        itemStack.setCount(nbt.getInt("Count"));

        if (nbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
            itemStack.setNbt(nbt.getCompound("tag"));
        }

        return itemStack;
    }

    public static void sortBank(BankItemStorage bankItemStorage) {
        List<ItemStack> collectedItems = new ArrayList<>();
        for (ItemStack itemStack : bankItemStorage.stacks) {
            boolean didExist = false;
            for (ItemStack existing : collectedItems) {
                if (ItemStack.canCombine(itemStack, existing)) {
                    existing.increment(itemStack.getCount());
                    didExist = true;
                }
            }
            if (!didExist && !itemStack.isEmpty())
                collectedItems.add(itemStack.copy());
        }
        collectedItems.sort(Comparator.comparingInt(ItemStack::getCount).reversed());
        for (int i = 0; i < bankItemStorage.size(); i++) {
            boolean isEmpty = collectedItems.isEmpty();
            if (!isEmpty) {
                ItemStack stack = collectedItems.get(0);
                int maxCount = stack.getMaxCount() * bankItemStorage.getStorageMultiplier();
                bankItemStorage.setStack(i, stack.split(maxCount));
                if (stack.isEmpty()) {
                    collectedItems.remove(0);
                }
            } else {
                bankItemStorage.setStack(i, ItemStack.EMPTY);

            }
        }
    }

    public static BankItemStorage getBankItemStorage(UUID uuid, World world) {

        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.get(uuid);

        return bankItemStorage;

    }

    public static BankItemStorage getBankItemStorage(ItemStack bank, World world) {

        UUID uuid = hasUUID(bank) ? getUUID(bank) : UUID.randomUUID();
        if (!hasUUID(bank))
            bank.getOrCreateNbt().putUuid(BankItem.UUID_KEY, uuid);

        BankType type = ((BankItem) bank.getItem()).getType();
        BankStateSaverAndLoader serverState = BankStateSaverAndLoader.getServerState(world.getServer());
        BankItemStorage bankItemStorage = serverState.getOrCreate(uuid, type, bank.getName());
        return bankItemStorage;

    }

}
