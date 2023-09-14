package net.natte.bankstorage.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
    }

    @Override
    public int getMaxItemCount() {
        return 64 * this.slotStorageMultiplier;
    }
    @Override
    public int getMaxItemCount(ItemStack stack) {
        return super.getMaxItemCount(stack) * slotStorageMultiplier;
    }

    @Override
    public void setStack(ItemStack stack) {
        // TODO Auto-generated method stub
        System.out.println("setstack");
        System.out.println(stack);
        super.setStack(stack);
    }
    
    

    
}
