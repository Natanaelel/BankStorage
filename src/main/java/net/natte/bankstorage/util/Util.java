package net.natte.bankstorage.util;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.item.BankItem;

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

}
