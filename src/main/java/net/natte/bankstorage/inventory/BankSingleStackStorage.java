package net.natte.bankstorage.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;

public class BankSingleStackStorage extends SingleStackStorage {

    private final BankItemStorage storage;
	final int slot;

    public BankSingleStackStorage(BankItemStorage storage, int slot) {
		this.storage = storage;
		this.slot = slot;
	}

    @Override
    protected ItemStack getStack() {
        return this.storage.getStack(this.slot);
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.storage.setStack(this.slot, stack);
    }

    @Override
    protected int getCapacity(ItemVariant itemVariant) {
        return storage.getStorageMultiplier() * itemVariant.getItem().getMaxCount();
    }
    
    
}
