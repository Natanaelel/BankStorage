package net.natte.bankstorage.util;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.natte.bankstorage.item.BankItem;

public class Util {

    public static boolean isBank(ItemStack itemStack) {
        return itemStack.getItem() instanceof BankItem;
    }

    public static boolean isAllowedInBank(ItemStack itemStack) {
        return !isBank(itemStack);
    }

    public static UUID getOrCreateUuid(ItemStack itemStack){
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

    public static UUID getUUID(ItemStack itemStack){
        return itemStack.getNbt().getUuid(BankItem.UUID_KEY);
    }

}
