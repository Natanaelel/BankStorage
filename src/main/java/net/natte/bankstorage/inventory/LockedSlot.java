package net.natte.bankstorage.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class LockedSlot extends Slot {

    public LockedSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
    @Override
    public boolean canTakePartial(PlayerEntity player) {
        return false;
    }
    @Override
    public boolean isEnabled() {
        return true;
        // super.isEnabled()
    }
    @Override
    public ItemStack takeStack(int amount) {
        // TODO Auto-generated method stub
        return ItemStack.EMPTY;
    }
    @Override
    public void setStack(ItemStack stack) {
        // TODO Auto-generated method stub
        // super.setStack(stack);
    }
}
