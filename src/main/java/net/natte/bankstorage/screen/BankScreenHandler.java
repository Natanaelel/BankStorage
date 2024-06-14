package net.natte.bankstorage.screen;

import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.inventory.LockedSlot;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;
import org.jetbrains.annotations.Nullable;

public class BankScreenHandler extends AbstractContainerMenu {

    public Container inventory;

    private BankType type;
    // barebones on client
    private BankItemStorage bankItemStorage;
    // null on client
    @Nullable
    UUID uuid;

    private final ContainerLevelAccess context;

    public BankScreenHandlerSyncHandler bankScreenSync;

    private ItemStack bankLikeItem;
    private final int slotWithOpenedBank;

    public ItemStack getBankLikeItem() {
        return this.bankLikeItem;
    }

    private short lockedSlotsRevision = 0;

    public BankScreenHandler(int syncId, Inventory playerInventory,
                             BankType type, ItemStack bankItem, int slot, BankItemStorage bankItemStorage,
                             ContainerLevelAccess context) {
//        super(type.getScreenHandlerType(), syncId);
        super(BankStorage.MENU_TYPE, syncId);
        this.context = context;

        this.bankLikeItem = bankItem;
//        this.bankLikeItem = inventory instanceof BankItemStorage bankItemStorage ? bankItemStorage.getItem()
//                : ItemStack.EMPTY;
        this.slotWithOpenedBank = slot;
        checkContainerSize(inventory, type.size());

        this.type = type;
        this.inventory = bankItemStorage.getContainer();
        this.uuid = bankItemStorage.uuid;

        inventory.startOpen(playerInventory.player);
        int rows = this.type.rows;
        int cols = this.type.cols;

//        BankItemStorage bankItemStorage = (BankItemStorage) inventory;
        // bank
        for (int y = 0; y < rows; ++y) {
            for (int x = 0; x < cols; ++x) {
                int slotIndex = x + y * cols;
                this.addSlot(new BankSlot(inventory, slotIndex, 8 + x * 18, 18 + y * 18,
                        this.type.stackLimit, bankItemStorage.getLockedStack(slotIndex)));
            }
        }

        // player inventory
        int inventoryY = 32 + rows * 18;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                // cannot move opened bank
                if (x + y * 9 + 9 == slotWithOpenedBank)
                    this.addSlot(new LockedSlot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
                else
                    this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
            }
        }

        // hotbar
        for (int x = 0; x < 9; ++x) {
            // cannot move opened bank
            if (x == slotWithOpenedBank) {
                this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            } else {
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            }
        }

    }

    @Override
    public boolean stillValid(Player player) {
        if (!AbstractContainerMenu.stillValid(this.context, player, BankStorage.BANK_DOCK_BLOCK.get()))
            return false;

        return this.context.evaluate((world, pos) -> {
            if (!(world.getBlockEntity(pos) instanceof BankDockBlockEntity blockEntity))
                return false;
            if (blockEntity.hasBank())
                return false;
            if (!blockEntity.getBank().has(BankStorage.UUIDComponentType))
                return false;
            if (!blockEntity.getBank().get(BankStorage.UUIDComponentType).equals(this.uuid))
                return false;
            return true;
        }, true);
    }

    public ContainerLevelAccess getContext() {
        return this.context;
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                // move from bank to player
                if (!this.insertItemToPlayer(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

            } // move from player to bank
            else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        // add to locked stack
        while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
            slot = this.slots.get(i);
            int slotSize = slot.getMaxStackSize(stack);
            itemStack = slot.getItem();

            if (slot instanceof BankSlot bankSlot && bankSlot.isLocked() && bankSlot.mayPlace(stack)) {
                if (itemStack.isEmpty()) {
                    slot.setByPlayer(stack.split(slotSize));
                    slot.setChanged();
                    bl = true;
                } else {
                    int toMove = Math.min(slotSize - itemStack.getCount(), Math.min(slotSize, stack.getCount()));
                    if (toMove > 0) {
                        itemStack.grow(toMove);
                        stack.shrink(toMove);
                        slot.setChanged();
                        bl = true;

                    }

                }
            }
            if (fromLast) {
                --i;
                continue;
            }
            ++i;
        }

        // add to existing stack
        i = fromLast ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
            slot = this.slots.get(i);
            int maxStackCount = slot.getMaxStackSize(stack);
            itemStack = slot.getItem();
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)
                    && ((slot instanceof BankSlot bankSlot) ? bankSlot.mayPlace(stack) : true)) {
                int j = itemStack.getCount() + stack.getCount();
                if (j <= maxStackCount) {
                    stack.setCount(0);
                    itemStack.setCount(j);
                    slot.setChanged();
                    bl = true;
                } else if (itemStack.getCount() < maxStackCount) {
                    stack.shrink(maxStackCount - itemStack.getCount());
                    itemStack.setCount(maxStackCount);
                    slot.setChanged();
                    bl = true;
                }
            }
            if (fromLast) {
                --i;
                continue;
            }
            ++i;
        }

        // add to new stack
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(stack)
                        && ((slot instanceof BankSlot bankSlot) ? bankSlot.mayPlace(stack) : true)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
                    }
                    slot.setChanged();
                    bl = true;
                    // break;
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

    protected boolean insertItemToPlayer(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                int maxStackCount = slot.getMaxStackSize(stack);
                itemStack = slot.getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= maxStackCount) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.setChanged();
                        bl = true;
                    } else if (itemStack.getCount() < maxStackCount) {
                        stack.shrink(maxStackCount - itemStack.getCount());
                        itemStack.setCount(maxStackCount);
                        slot.setChanged();
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
                itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                    slot.setByPlayer(stack
                            .split(Math.min(slot.getMaxStackSize(), Math.min(stack.getCount(), stack.getMaxStackSize()))));
                    slot.setChanged();
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
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {

        if (actionType == ClickType.SWAP) {
            // cannot move opened BankItem with numbers
            if (!this.slots.get(slotIndex).mayPickup(player) || button == slotWithOpenedBank)
//                    (button == slotWithOpenedBank && button >= 0 && button )
//                            && this.slots.get(this.slots.size() - 9 + button) instanceof LockedSlot))
                return;

            ItemStack stackInSlot = this.slots.get(slotIndex).getItem();

            // can't move large stack with numbers
            if (stackInSlot.getCount() > stackInSlot.getMaxStackSize())
                return;
        }

        super.clicked(slotIndex, button, actionType, player);
        if (uuid != null)
            NetworkUtil.syncCachedBankS2C(uuid, ((ServerPlayer) player));

    }


//    @Override
//    public boolean tryItemClickBehaviourOverride(Player player, ClickAction clickAction, Slot slot, ItemStack stack, ItemStack cursorStack) {
//
//        // is v needed anymore? TODO: check
//        // prevent dupe by putting item inside multiple bundles at the same time
//        FeatureFlagSet featureSet = player.level().enabledFeatures();
//        if (cursorStack.isItemEnabled(featureSet) && cursorStack.overrideStackedOnOther(slot, clickAction, player)) {
//            return true;
//        }
//        if (stack.getCount() > stack.getMaxStackSize()) {
//            return false;
//        }
//        slotsChanged(this.inventory);
//        return super.tryItemClickBehaviourOverride(player, clickAction, slot, stack, cursorStack);
//    }

    @Override
    public void removed(Player player) {
        if (uuid != null)
            NetworkUtil.syncCachedBankS2C(uuid, ((ServerPlayer) player));
        super.removed(player);
    }


    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.bankScreenSync == null)
            return;
        BankItemStorage bankItemStorage = (BankItemStorage) inventory;
        if (bankItemStorage.getLockedSlotsRevision() != this.lockedSlotsRevision) {
            this.setLockedSlotsNoSync(bankItemStorage.getlockedSlots());
            this.bankScreenSync.syncLockedSlots(this, bankItemStorage.getlockedSlots());
            this.lockedSlotsRevision = bankItemStorage.getLockedSlotsRevision();
        }
    }

    public void setBankScreenSync(BankScreenHandlerSyncHandler bankScreenSync) {
        this.bankScreenSync = bankScreenSync;
    }

    public void lockSlot(int index, ItemStack stack) {
        ((BankSlot) this.slots.get(index)).lock(stack);
        ((BankItemStorage) this.inventory).lockSlot(index, stack);
    }

    public void unlockSlot(int index) {
        ((BankSlot) this.slots.get(index)).unlock();
        ((BankItemStorage) this.inventory).unlockSlot(index);
    }

    public void setLockedSlotsNoSync(Map<Integer, ItemStack> lockedSlots) {
        BankItemStorage bankItemStorage = (BankItemStorage) this.inventory;

        for (int i = 0; i < this.slots.size(); ++i) {
            Slot slot = this.slots.get(i);
            if (!(slot instanceof BankSlot bankSlot))
                continue;
            ItemStack stack = lockedSlots.get(i);
            if (stack == null) {
                bankItemStorage.unlockSlot(i);
                bankSlot.unlock();
            } else {
                bankItemStorage.lockSlot(i, stack);
                bankSlot.lock(stack);
            }
        }
    }

    public void lockedSlotsMarkDirty() {
        ((BankItemStorage) this.inventory).updateLockedSlotsRevision();
    }

    public BankType getBankType() {
        return type;
    }
}
