package net.natte.bankstorage.screen;

// import org.jetbrains.annotations.Nullable;

// import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
// import net.minecraft.util.ClickType;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.inventory.LockedSlot;

public class BankScreenHandler extends ScreenHandler {

    public Inventory inventory;

    private BankType type;

    public static net.minecraft.screen.ScreenHandlerType.Factory<BankScreenHandler> fromType(BankType type) {
        return (syncId, playerInventory) -> {
            // return new BankScreenHandler(syncId, playerInventory, new
            // SimpleInventory(type.size()), type);
            return new BankScreenHandler(syncId, playerInventory, new BankItemStorage(type), type);
        };
    }

    // This constructor gets called from the BlockEntity on the server without
    // calling the other constructor first, the server knows the inventory of the
    // container
    // and can therefore directly provide it as an argument. This inventory will
    // then be synced to the client.
    public BankScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, BankType type) {
        super(type.getScreenHandlerType(), syncId);

        this.type = type;

        checkSize(inventory, type.size());
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        int rows = this.type.rows;
        int cols = this.type.cols;

        // bank
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                this.addSlot(new BankSlot(inventory, x + y * cols, 8 + x * 18, 18 + y * 18,
                        this.type.slotStorageMultiplier));
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
            if (playerInventory.selectedSlot == x) {
                this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            } else {
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            }
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
                // move from bank to player
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

            } // move from player to bank
            else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
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
        if (stack.isStackable() || !stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                int maxStackCount = slot.getMaxItemCount(stack);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= maxStackCount) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < maxStackCount) {
                        stack.decrement(maxStackCount - itemStack.getCount());
                        itemStack.setCount(maxStackCount);
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
                        slot.setStack(stack.split(Math.min(stack.getCount(), stack.getMaxCount())));
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

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        System.out.println("slotindex " + slotIndex + " button " + button + " action " + actionType + " player " + player);
        
        // cannot move BankItem with numbers
        if(actionType == SlotActionType.SWAP && button == player.getInventory().selectedSlot) return;


        // throw stacks of max size stack.maxsize, not slot size
        // prevent dropping 2+ saddles in a single stack
        // if (actionType == SlotActionType.THROW && this.getCursorStack().isEmpty() && slotIndex >= 0) {
        //     Slot slot3 = this.slots.get(slotIndex);
        //     int j = button == 0 ? 1 : slot3.getStack().getCount();
        //     ItemStack itemStack = slot3.takeStackRange(j, Integer.MAX_VALUE, player);
        //     while(!itemStack.isEmpty()){
        //         player.dropItem(itemStack.split(itemStack.getItem().getMaxCount()), true);
        //     }
        //     return;
        // } 

        super.onSlotClick(slotIndex, button, actionType, player);
    }

 
}
