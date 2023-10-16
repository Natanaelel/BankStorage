package net.natte.bankstorage.storage;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.natte.bankstorage.options.PickupMode;

public class BankCombinedStorage implements Storage<ItemVariant> {

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
        long amount = 0;
        switch (this.pickupMode) {
            case NONE:
                break;
            case ALL:
                amount += insertIntoLockedSlots(resource, maxAmount - amount, transaction);
                amount += insertIntoNonEmptySlots(resource, maxAmount - amount, transaction);
                amount += insertIntoAnySlots(resource, maxAmount - amount, transaction);
                break;
            case FILTERED:
                amount += insertIntoLockedSlots(resource, maxAmount - amount, transaction);
                amount += insertIntoNonEmptySlots(resource, maxAmount - amount, transaction);
                // if(h)
                if (hasSlotWithItem(resource)) {
                    amount += insertIntoAnySlots(resource, maxAmount - amount, transaction);
                }
                break;
            case VOID:
                amount += insertIntoLockedSlots(resource, maxAmount - amount, transaction);
                amount += insertIntoNonEmptySlots(resource, maxAmount - amount, transaction);
                amount += insertIntoAnySlots(resource, maxAmount - amount, transaction);
                return maxAmount;
        }
        return amount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {

        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return null;
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

}
