package net.natte.bankstorage.storage;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.options.PickupMode;
import net.neoforged.neoforge.items.IItemHandler;

public class BankCombinedStorage implements IItemHandler {

    private List<BankSingleStackStorage> parts;
    private PickupMode pickupMode;

    public BankCombinedStorage(List<BankSingleStackStorage> parts, PickupMode pickupMode) {
        this.parts = parts;
        this.pickupMode = pickupMode;
    }

    public void setPickupMode(PickupMode pickupMode) {
        this.pickupMode = pickupMode;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {

    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return extractFromAnySlot(resource, maxAmount, transaction);
    }

    private long extractFromAnySlot(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0)
            return 0;
        long amount = 0;

        for (BankSingleStackStorage part : this.parts) {
            amount += part.extract(resource, maxAmount - amount, transaction);

            if (amount == maxAmount)
                return amount;
        }

        return amount;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return parts.stream().map(singleStackStorage -> {
            return ((StorageView<ItemVariant>) singleStackStorage);
        }).iterator();
    }

    private long insertIntoLockedSlots(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0)
            return 0;
        long amount = 0;
        for (BankSingleStackStorage part : this.parts) {
            if (part.isLocked()) {

                amount += part.insert(resource, maxAmount - amount, transaction);

                if (amount == maxAmount)
                    return amount;
            }
        }

        return amount;
    }

    private long insertIntoNonEmptySlots(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0)
            return 0;
        long amount = 0;
        for (BankSingleStackStorage part : this.parts) {
            if (part.getAmount() == 0)
                continue;

            amount += part.insert(resource, maxAmount - amount, transaction);

            if (amount == maxAmount)
                return amount;
        }

        return amount;
    }

    private long insertIntoAnySlots(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0)
            return 0;
        long amount = 0;

        for (BankSingleStackStorage part : this.parts) {
            amount += part.insert(resource, maxAmount - amount, transaction);

            if (amount == maxAmount)
                return amount;
        }

        return amount;
    }

    private boolean hasSlotWithItem(ItemVariant resource) {
        for (BankSingleStackStorage part : this.parts) {
            if (part.isLocked() && part.canInsert(resource))
                return true;
            if (!part.isLocked() && resource.matches(part.getStack()))
                return true;
        }
        return false;
    }

    @Override
    public int getSlots() {
        return this.parts.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.parts.get(slot).getStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int maxAmount = stack.getCount();
        int insertedAmount = 0;
        switch (this.pickupMode) {
            case NONE:
                break;
            case ALL:
                insertedAmount += insertIntoLockedSlots(stack, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(stack, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoAnySlots(stack, maxAmount - insertedAmount, transaction);
                break;
            case FILTERED:
                insertedAmount += insertIntoLockedSlots(resource, maxAmount - insertedAmount, transaction);
                insertedAmount += insertIntoNonEmptySlots(resource, maxAmount - insertedAmount, transaction);
                if (hasSlotWithItem(resource)) {
                    insertedAmount += insertIntoAnySlots(resource, maxAmount - insertedAmount, transaction);
                }
                break;
            case VOID:
                if (hasSlotWithItem(resource)) {
                    insertedAmount += insertIntoLockedSlots(resource, maxAmount - insertedAmount, transaction);
                    insertedAmount += insertIntoNonEmptySlots(resource, maxAmount - insertedAmount, transaction);
                    // Don't insert into new slot when voiding overflow https://github.com/Natanaelel/BankStorage/issues/20
                    // insertedAmount += insertIntoAnySlots(resource, maxAmount - insertedAmount, transaction);
                    insertedAmount = maxAmount;
                }
                break;
        }
        return insertedAmount;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.parts.get(slot).getSlotLimit();
    }

}
