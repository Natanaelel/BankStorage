package net.natte.bankstorage.inventory;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.util.Util;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;
    private boolean isLocked = false;
    private ItemStack lockedStack = null;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
    }

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier,
            @Nullable ItemStack lockedStack) {
        this(inventory, index, x, y, slotStorageMultiplier);
        if (lockedStack != null) {
            this.isLocked = true;
            this.lockedStack = lockedStack;
        }

    }

    public void lock(ItemStack stack) {
        this.isLocked = true;
        this.lockedStack = stack.copyWithCount(1);
    }

    public void unlock() {
        this.isLocked = false;
        this.lockedStack = null;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public ItemStack getLockedStack() {
        return this.lockedStack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (this.isLocked && !Util.canCombine(stack, this.lockedStack))
            return false;
        if (!Util.isAllowedInBank(stack))
            return false;

        return super.canInsert(stack);
    }

    @Override
    public int getMaxItemCount() {
        // I think unused
        return 64 * this.slotStorageMultiplier;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        // return stack.getMaxCount() * this.slotStorageMultiplier;
        return 64 * this.slotStorageMultiplier;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.inventory.setStack(this.getIndex(), stack);
    }

    // limit items picked up to stack size, prevent cursorstack to be larger than
    // normally possible
    @Override
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        if (!this.canTakeItems(player)) {
            return Optional.empty();
        }
        if (!this.canTakePartial(player) && max < this.getStack().getCount()) {
            return Optional.empty();
        }

        int stackMaxCount = this.getStack().getMaxCount();
        ItemStack itemStack = this.takeStack(Math.min(Math.min(min, max), stackMaxCount));

        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getStack().isEmpty()) {
            this.setStack(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }
}
