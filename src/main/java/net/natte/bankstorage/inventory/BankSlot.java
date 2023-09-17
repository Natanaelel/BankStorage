package net.natte.bankstorage.inventory;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.item.BankItem;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;

    public int count;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
        this.count = 0;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !(stack.getItem() instanceof BankItem) && super.canInsert(stack);
    }

    @Override
    public int getMaxItemCount() {
        return 64 * this.slotStorageMultiplier;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return Math.min(this.getMaxItemCount(), stack.getMaxCount() * this.slotStorageMultiplier);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ItemStack getStack() {
        return super.getStack();
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return super.insertStack(stack, count);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return super.takeStack(amount);
    }

    @Override
    protected void onTake(int amount) {
        super.onTake(amount);
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

        boolean isTryingToTakeHalf = min == (this.getStack().getCount() + 1) / 2;
        int stackMaxCount = this.getStack().getMaxCount();
        int takeCount = isTryingToTakeHalf ? (stackMaxCount + 1) / 2 : stackMaxCount;
        ItemStack itemStack = this.takeStack(Math.min(Math.min(min, max), takeCount));

        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getStack().isEmpty()) {
            this.setStack(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }
}
