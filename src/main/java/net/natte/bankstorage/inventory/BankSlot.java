package net.natte.bankstorage.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BankSlot extends Slot {

    private final int stackLimit;
    private @Nullable ItemStack lockedStack = null;

    public BankSlot(Container inventory, int index, int x, int y, int stackLimit) {
        super(inventory, index, x, y);
        this.stackLimit = stackLimit;
    }

    public BankSlot(Container inventory, int index, int x, int y, int stackLimit,
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
    public boolean mayPlace(ItemStack stack) {
        if (this.lockedStack != null && !ItemStack.isSameItemSameComponents(stack, this.lockedStack))
            return false;
        if (!Util.isAllowedInBank(stack))
            return false;

        return super.mayPlace(stack);
    }

    @Override
    public int getMaxStackSize() {
        // I think unused

        return stackLimit;
    }
    @Override
    public int getMaxStackSize(ItemStack stack) {
        // return stack.getMaxCount() * this.stackLimit;
        return this.stackLimit;
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        this.container.setItem(this.getSlotIndex(), stack);
    }

    // limit items picked up to stack size, prevent cursorstack to be larger than
    // normally possible
    @Override
    public Optional<ItemStack> tryRemove(int min, int max, Player player) {
        if (!this.mayPickup(player)) {
            return Optional.empty();
        }
        if (!this.allowModification(player) && max < this.getItem().getCount()) {
            return Optional.empty();
        }

        int stackMaxCount = this.getItem().getMaxStackSize();
        ItemStack itemStack = this.remove(Math.min(Math.min(min, max), stackMaxCount));

        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getItem().isEmpty()) {
            this.setByPlayer(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }
}
