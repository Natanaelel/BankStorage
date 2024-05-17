package net.natte.bankstorage.inventory;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.util.Util;

public class BankSlot extends Slot {

    public int stackLimit;
    private @Nullable ItemStack lockedStack = null;

    public BankSlot(Inventory inventory, int index, int x, int y, int stackLimit) {
        super(inventory, index, x, y);
        this.stackLimit = stackLimit;
    }

    public BankSlot(Inventory inventory, int index, int x, int y, int stackLimit,
            @Nullable ItemStack lockedStack) {
        this(inventory, index, x, y, stackLimit);
        if (lockedStack != null) {
            this.lockedStack = lockedStack;
        }

    }

    public void lock(ItemStack stack) {
        this.lockedStack = stack.copyWithCount(1);
    }

    public void unlock() {
        this.lockedStack = null;
    }

    public boolean isLocked() {
        return this.lockedStack != null;
    }

    public @Nullable ItemStack getLockedStack() {
        return this.lockedStack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (this.lockedStack != null && !ItemStack.areItemsAndComponentsEqual(stack, this.lockedStack))
            return false;
        if (!Util.isAllowedInBank(stack))
            return false;

        return super.canInsert(stack);
    }

    @Override
    public int getMaxItemCount() {
        // I think unused
        return this.stackLimit;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        // return stack.getMaxCount() * this.stackLimit;
        return this.stackLimit;
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
