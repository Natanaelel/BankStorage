package net.natte.bankstorage.storage;

import net.minecraft.world.item.ItemStack;

public class BankSlotData {

    private ItemStack item;
    private int count;
    private boolean isLocked;

    public BankSlotData(ItemStack item, int count, boolean isLocked) {
        this.item = item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1);
        this.count = count;
        this.isLocked = isLocked;
    }

    public BankSlotData() {
        this(ItemStack.EMPTY, 0, false);
    }

    public ItemStack getStack(){
        return item.copyWithCount(count);
    }
}
