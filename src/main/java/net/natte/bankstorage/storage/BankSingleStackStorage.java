package net.natte.bankstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.util.Util;

public class BankSingleStackStorage extends SingleStackStorage {

    private final BankItemStorage storage;
    final int slot;

    public BankSingleStackStorage(BankItemStorage storage, int slot) {
        this.storage = storage;
        this.slot = slot;
    }

    @Override
    protected ItemStack getStack() {
        return this.storage.getItem(this.slot);
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.storage.setStack(this.slot, stack);
    }

    @Override
    protected int getCapacity(ItemVariant itemVariant) {
        return storage.type.stackLimit;
    }

    @Override
    protected boolean canInsert(ItemVariant itemVariant) {
        if (!Util.isAllowedInBank(itemVariant.toStack()))
            return false;
        ItemStack lockedStack = this.storage.getLockedStack(this.slot);
        if (lockedStack != null && !itemVariant.matches(lockedStack))
            return false;

        return super.canInsert(itemVariant);
    }

    public boolean isLocked() {
        return this.storage.getLockedStack(this.slot) != null;
    }
}
