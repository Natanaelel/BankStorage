package net.natte.bankstorage.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.BankStorage;

public class BankScreenHandler extends ScreenHandler {

    public Inventory inventory;

    public int rows;
    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public BankScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }
 
    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public BankScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(BankStorage.BANK_SCREEN_HANDLER_TYPE, syncId);

        int k;
        int j;
        checkSize(inventory, 9);
        this.inventory = inventory;
        this.rows = 1;
        inventory.onOpen(playerInventory.player);
        
        int i = this.rows * 9 - 9;
        // bank
        for (j = 0; j < this.rows; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 52 + j * 18));
            }
        }
        // player inventory
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18 + i));
            }
        }
        // hotbar
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142 + i));
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
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
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
    

}
