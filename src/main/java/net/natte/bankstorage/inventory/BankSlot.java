package net.natte.bankstorage.inventory;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.item.BankItem;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !(stack.getItem() instanceof BankItem) && super.canInsert(stack);
    }

    @Override
    public int getMaxItemCount() {
        // I think unused
        return 64 * this.slotStorageMultiplier;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return stack.getMaxCount() * this.slotStorageMultiplier;
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
