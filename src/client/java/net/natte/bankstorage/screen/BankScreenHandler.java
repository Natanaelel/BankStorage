package net.natte.bankstorage.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.inventory.LockedSlot;

public class BankScreenHandler extends ScreenHandler {

    public Inventory inventory;

    public int rows;
    public int cols;

    private BankType type;


    public static net.minecraft.screen.ScreenHandlerType.Factory<BankScreenHandler> fromType(BankType type){
        return (syncId, playerInventory) -> {
            return new BankScreenHandler(syncId, playerInventory, new SimpleInventory(type.size()), type);
        };
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public BankScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, BankType type) {
        super(type.getScreenHandlerType() , syncId);

        
        this.type = type;
        this.rows = this.type.rows;
        this.cols = this.type.cols;

        checkSize(inventory, type.size());
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        
        
        int rows = this.type.rows;
        int cols = this.type.cols;
        
        // bank
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                this.addSlot(new BankSlot(inventory, x + y * cols, 8 + x * 18, 18 + y * 18, this.type.slotStorageMultiplier));
            }
        }

        // player inventory
        int inventoryY = 32 + rows * 18;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
            }
        }
        // hotbar
        for (int x = 0; x < 9; ++x) {
            // cannot move opened dank
            if(playerInventory.selectedSlot == x){
                this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            }
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
        }
 
    }

    
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
 
    // Shift + Player Inv Slot
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!super.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
 
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
 
        return newStack;
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        int m = this.type.slotStorageMultiplier;
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount() * m) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount() * m) {
                        stack.decrement(stack.getMaxCount() * m - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount() * m);
                        slot.markDirty();
                        bl = true;
                    }
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    if (stack.getCount() > slot.getMaxItemCount()) {
                        slot.setStack(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }
                    slot.markDirty();
                    bl = true;
                    break;
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

}
