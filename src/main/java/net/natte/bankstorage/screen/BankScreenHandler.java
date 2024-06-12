package net.natte.bankstorage.screen;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType.ExtendedFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.inventory.LockedSlot;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public class BankScreenHandler extends AbstractContainerMenu {

    public Container inventory;

    private BankType type;
    // barebones on client
    private BankItemStorage bankItemStorage;

    private final ContainerLevelAccess context;

    public BankScreenHandlerSyncHandler bankScreenSync;

    private ItemStack bankLikeItem;

    public ItemStack getBankLikeItem() {
        return this.bankLikeItem;
    }

    private short lockedSlotsRevision = 0;

//    public static IContainerFactory<BankScreenHandler, ItemStack> fromType(BankType type) {
//        return (syncId, playerInventory, bankLikeItem) -> {
//            BankScreenHandler bankScreenHandler = new BankScreenHandler(syncId, playerInventory,
//                    new BankItemStorage(type, null), type,
//                    ContainerLevelAccess.NULL);
//            bankScreenHandler.bankLikeItem = bankLikeItem;
//            return bankScreenHandler;
//        };
//    }

    // This constructor gets called from the BlockEntity on the server without
    // calling the other constructor first, the server knows the inventory of the
    // container
    // and can therefore directly provide it as an argument. This inventory will
    // then be synced to the client.

    public BankScreenHandler(int syncId, Inventory playerInventory,
            BankType type, ItemStack bankItem, int slot, BankItemStorage bankItemStorage,
            ContainerLevelAccess context) {
        super(type.getScreenHandlerType(), syncId);
        this.context = context;

        this.bankLikeItem = bankItem;
//        this.bankLikeItem = inventory instanceof BankItemStorage bankItemStorage ? bankItemStorage.getItem()
//                : ItemStack.EMPTY;

        checkContainerSize(inventory, type.size());

        this.type = type;
        this.inventory = bankItemStorage.getContainer();

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
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, inventoryY + y * 18));
            }
        }

        // hotbar
        for (int x = 0; x < 9; ++x) {
            // cannot move opened bank
            if (playerInventory.selected == x
                    && Util.isBankLike(playerInventory.getItem(playerInventory.selected))
                    && this.context == ContainerLevelAccess.NULL) {
                this.addSlot(new LockedSlot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            } else {
                this.addSlot(new Slot(playerInventory, x, 8 + x * 18, inventoryY + 58));
            }
        }

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.context.get((world, pos) -> {
            if (!(world.getBlockEntity(pos) instanceof BankDockBlockEntity blockEntity))
                return false;
            if (!blockEntity.hasBank())
                return false;
            return true;
        }, true);
    }

    public ContainerLevelAccess getContext() {
        return this.context;
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
                if (!this.insertItemToPlayer(originalStack, this.inventory.size(), this.slots.size(), true)) {
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
        // add to locked stack
        while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
            slot = this.slots.get(i);
            int slotSize = slot.getMaxItemCount(stack);
            itemStack = slot.getStack();

            if (slot instanceof BankSlot bankSlot && bankSlot.isLocked() && bankSlot.canInsert(stack)) {
                if (itemStack.isEmpty()) {
                    slot.setStack(stack.split(slotSize));
                    slot.markDirty();
                    bl = true;
                } else {
                    int toMove = Math.min(slotSize - itemStack.getCount(), Math.min(slotSize, stack.getCount()));
                    if (toMove > 0) {
                        itemStack.increment(toMove);
                        stack.decrement(toMove);
                        slot.markDirty();
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
            int maxStackCount = slot.getMaxItemCount(stack);
            itemStack = slot.getStack();
            if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)
                    && ((slot instanceof BankSlot bankSlot) ? bankSlot.canInsert(stack) : true)) {
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

        // add to new stack
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)
                        && ((slot instanceof BankSlot bankSlot) ? bankSlot.canInsert(stack) : true)) {
                    if (stack.getCount() > slot.getMaxItemCount()) {
                        slot.setStack(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStack(stack.split(Math.min(stack.getCount(), stack.getMaxCount())));
                    }
                    slot.markDirty();
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
                int maxStackCount = slot.getMaxItemCount(stack);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
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
                    slot.setStack(stack
                            .split(Math.min(slot.getMaxItemCount(), Math.min(stack.getCount(), stack.getMaxCount()))));
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

        if (actionType == SlotActionType.SWAP) {
            // cannot move opened BankItem with numbers
            if (this.slots.get(slotIndex) instanceof LockedSlot ||
                    (button == player.getInventory().selectedSlot
                            && this.slots.get(this.slots.size() - 9 + button) instanceof LockedSlot))
                return;

            ItemStack stackInSlot = this.slots.get(slotIndex).getStack();

            // can't move large stack with numbers
            if (stackInSlot.getCount() > stackInSlot.getMaxCount())
                return;
        }

        UUID uuid = Util.getUUIDFromScreenHandler(this);

        super.onSlotClick(slotIndex, button, actionType, player);
        if (uuid != null)
            NetworkUtil.syncCachedBankS2C(uuid, ((ServerPlayerEntity) player));

    }


    @Override
    public boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack,
            ItemStack cursorStack) {

        // prevent dupe by putting item inside multiple bundles at the same time
        FeatureSet featureSet = player.getWorld().getEnabledFeatures();
        if (cursorStack.isItemEnabled(featureSet) && cursorStack.onStackClicked(slot, clickType, player)) {
            return true;
        }
        if (stack.getCount() > stack.getMaxCount()) {
            return false;
        }
        onContentChanged(this.inventory);
        return super.handleSlotClick(player, clickType, slot, stack, cursorStack);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        ItemStack left = player.getOffHandStack();
        ItemStack right = player.getMainHandStack();
        if (Util.hasUUID(left)) {
            CachedBankStorage.requestCacheUpdate(Util.getUUID(left));
        }
        if (Util.hasUUID(right)) {
            CachedBankStorage.requestCacheUpdate(Util.getUUID(right));
        }
        super.onClosed(player);
    }



    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
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
}
