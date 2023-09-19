package net.natte.bankstorage.util;

import net.minecraft.item.ItemStack;
import net.natte.bankstorage.item.BankItem;

public class Util {
    public static boolean isBank(ItemStack itemStack) {
        return itemStack.getItem() instanceof BankItem;
    }

    public static boolean isAllowedInBank(ItemStack itemStack) {
        return !isBank(itemStack);
    }
}
