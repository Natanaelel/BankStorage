package net.natte.bankstorage.storage;

import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.options.PickupMode;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Map;

public class BankItemHandler implements IItemHandler {

    private final List<ItemStack> items;
    private final Map<Integer, ItemStack> lockedSlots;
    private final PickupMode pickupMode;
    private final BankType type;
    private final int slotCapacity;
    private final Runnable setChanged;


    public BankItemHandler(List<ItemStack> items, Map<Integer, ItemStack> lockedSlots, BankType type, PickupMode pickupMode, Runnable setChanged) {
        this.items = items;
        this.lockedSlots = lockedSlots;
        this.pickupMode = pickupMode;
        this.type = type;
        this.slotCapacity = type.stackLimit;
        this.setChanged = setChanged;
    }

    private void setChanged() {
        this.setChanged.run();
    }

    @Override
    public int getSlots() {
        return type.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items.get(slot);
    }

    public ItemStack insertItem(ItemStack stack) {
        return insertItem(0, stack, false);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // ignores slot. hacky? yes. NeoForge needs better transfer api? yes.

        if (stack.isEmpty())
            return ItemStack.EMPTY;
        int maxAmount = stack.getCount();
        int inserted = 0;

        switch (pickupMode) {
            case NONE -> {
                return stack;
            }
            case ALL -> {
                inserted += insertIntoLockedSlots(stack, maxAmount - inserted, simulate);
                inserted += insertIntoNonEmptySlots(stack, maxAmount - inserted, simulate);
                inserted += insertIntoAnySlots(stack, maxAmount - inserted, simulate);
            }
            case FILTERED -> {
                inserted += insertIntoLockedSlots(stack, maxAmount - inserted, simulate);
                inserted += insertIntoNonEmptySlots(stack, maxAmount - inserted, simulate);
                if (hasSlotWithItem(stack)) {
                    inserted += insertIntoAnySlots(stack, maxAmount - inserted, simulate);
                }
            }
            case VOID -> {
                if (hasSlotWithItem(stack)) {
                    inserted += insertIntoLockedSlots(stack, maxAmount - inserted, simulate);
                    inserted += insertIntoNonEmptySlots(stack, maxAmount - inserted, simulate);
                    inserted = maxAmount;
                }
            }
        }
        if (inserted > 0)
            setChanged();
        return inserted == 0 ? stack : inserted == maxAmount ? ItemStack.EMPTY : stack.copyWithCount(maxAmount - inserted);
    }


    private int insertIntoLockedSlots(ItemStack stack, int amount, boolean simulate) {
        if (amount == 0)
            return 0;

        int inserted = 0;
        for (int slot : lockedSlots.keySet()) {
            ItemStack lockedStack = lockedSlots.get(slot);
            if (ItemStack.isSameItemSameComponents(lockedStack, stack)) {
                ItemStack stackInSlot = items.get(slot);
                int count = stackInSlot.getCount();
                int spaceLeft = slotCapacity - count;
                int toInsert = amount - inserted;
                int gotInserted = Math.min(spaceLeft, toInsert);
                inserted += gotInserted;
                if (!simulate)
                    items.set(slot, stack.copyWithCount(count + gotInserted));
            }
            if (inserted == amount)
                break;
        }
        return inserted;
    }

    private int insertIntoNonEmptySlots(ItemStack stack, int amount, boolean simulate) {
        if (amount == 0)
            return 0;

        int inserted = 0;
        for (int slot = 0; slot < items.size(); ++slot) {
            ItemStack stackInSlot = items.get(slot);
            int count = stackInSlot.getCount();
            if (count == 0)
                continue;
            if (ItemStack.isSameItemSameComponents(stackInSlot, stack)) {
                int spaceLeft = slotCapacity - count;
                int toInsert = amount - inserted;
                int gotInserted = Math.min(spaceLeft, toInsert);
                inserted += gotInserted;
                if (!simulate)
                    items.set(slot, stack.copyWithCount(count + inserted));
            }
            if (inserted == amount)
                break;
        }
        return inserted;
    }

    private int insertIntoAnySlots(ItemStack stack, int amount, boolean simulate) {
        if (amount == 0)
            return 0;

        int inserted = 0;
        for (int slot = 0; slot < items.size(); ++slot) {
            ItemStack lockedStack = lockedSlots.get(slot);
            ItemStack stackInSlot = items.get(slot);
            int count = stackInSlot.getCount();
            if (lockedStack != null ? ItemStack.isSameItemSameComponents(lockedStack, stack) : stackInSlot.isEmpty() || ItemStack.isSameItemSameComponents(stackInSlot, stack)) {
                int spaceLeft = slotCapacity - count;
                int toInsert = amount - inserted;
                int gotInserted = Math.min(spaceLeft, toInsert);
                inserted += gotInserted;
                if (!simulate)
                    items.set(slot, stack.copyWithCount(count + inserted));
            }
            if (inserted == amount)
                break;
        }
        return inserted;
    }

    private boolean hasSlotWithItem(ItemStack stack) {

        for (ItemStack lockedStack : lockedSlots.values()) {
            if (ItemStack.isSameItemSameComponents(lockedStack, stack))
                return true;
        }

        for (ItemStack stackInSlot : items) {
            if (ItemStack.isSameItemSameComponents(stackInSlot, stack))
                return true;

        }
        return false;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stackInSlot = items.get(slot);
        int count = stackInSlot.getCount();
        int extracted = Math.min(count, amount);
        if (extracted == 0)
            return ItemStack.EMPTY;
        if (!simulate)
            items.set(slot, stackInSlot.copyWithCount(count - extracted));
        return stackInSlot.copyWithCount(extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotCapacity;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }
}
