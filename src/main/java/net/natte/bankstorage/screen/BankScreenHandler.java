package net.natte.bankstorage.screen;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.packet.NetworkUtil;
import net.natte.bankstorage.util.Util;

public class BankScreenHandler extends ScreenHandler {

    public Inventory inventory;

    private final BankType type;

    private final ScreenHandlerContext context;

    private BankScreenSync bankScreenSync;

    private ItemStack bankLikeItem;

    public ItemStack getBankLikeItem() {
        return this.bankLikeItem;
    }

    private short lockedSlotsRevision = 0;

    public static ExtendedFactory<BankScreenHandler> fromType(BankType type) {
        return (syncId, playerInventory, packetByteBuf) -> {
            BankScreenHandler bankScreenHandler = new BankScreenHandler(syncId, playerInventory,
                    new BankItemStorage(type, null), type,
                    ScreenHandlerContext.EMPTY);
            bankScreenHandler.bankLikeItem = packetByteBuf.readItemStack();
            return bankScreenHandler;
        };
    }

    // This constructor gets called from the BlockEntity on the server without
    // calling the other constructor first, the server knows the inventory of the
    // container
    // and can therefore directly provide it as an argument. This inventory will
    // then be synced to the client.

    public BankScreenHandler(int syncId, PlayerInventory playerInventory,
            Inventory inventory, BankType type,
            ScreenHandlerContext context) {
        super(type.getScreenHandlerType(), syncId);
        this.context = context;

        this.bankLikeItem = inventory instanceof BankItemStorage bankItemStorage ? bankItemStorage.getItem()
                : ItemStack.EMPTY;

        checkSize(inventory, type.size());

        this.type = type;
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player);
        int rows = this.type.rows;
        int cols = this.type.cols;

        BankItemStorage bankItemStorage = (BankItemStorage) inventory;
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
            if (playerInventory.selectedSlot == x
                    && Util.isBankLike(playerInventory.getStack(playerInventory.selectedSlot))
                    && this.context == ScreenHandlerContext.EMPTY) {
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

    public ScreenHandlerContext getContext() {
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
            if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)
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
    public void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        ItemStack itemStack;
        ItemStack itemStack2;
        PlayerInventory playerInventory;
        ClickType clickType;
        ItemStack itemStack3;

        playerInventory = player.getInventory();
        if (actionType == SlotActionType.QUICK_CRAFT) {
            int lastQuickCraftStage = this.quickCraftStage;
            this.quickCraftStage = ScreenHandler.unpackQuickCraftStage(button);
            if (!(lastQuickCraftStage == 1 && this.quickCraftStage == 2
                    || lastQuickCraftStage == this.quickCraftStage)) {
                this.endQuickCraft();
            } else if (this.getCursorStack().isEmpty()) {
                this.endQuickCraft();
            } else if (this.quickCraftStage == 0) {
                this.quickCraftButton = ScreenHandler.unpackQuickCraftButton(button);
                if (ScreenHandler.shouldQuickCraftContinue(this.quickCraftButton, player)) {
                    this.quickCraftStage = 1;
                    this.quickCraftSlots.clear();
                } else {
                    this.endQuickCraft();
                }
            } else if (this.quickCraftStage == 1) {
                Slot slot = this.slots.get(slotIndex);
                if (!BankScreenHandler.canInsertItemIntoSlot(slot, itemStack3 = this.getCursorStack(), true)
                        || !slot.canInsert(itemStack3)
                        || this.quickCraftButton != 2 && itemStack3.getCount() <= this.quickCraftSlots.size()
                        || !this.canInsertIntoSlot(slot))
                    return;
                this.quickCraftSlots.add(slot);
            } else if (this.quickCraftStage == 2) {
                if (!this.quickCraftSlots.isEmpty()) {
                    if (this.quickCraftSlots.size() == 1) {
                        int j = this.quickCraftSlots.iterator().next().id;
                        this.endQuickCraft();
                        this.internalOnSlotClick(j, this.quickCraftButton, SlotActionType.PICKUP, player);
                        return;
                    }
                    ItemStack itemStack22 = this.getCursorStack().copy();
                    if (itemStack22.isEmpty()) {
                        this.endQuickCraft();
                        return;
                    }
                    int k = this.getCursorStack().getCount();
                    for (Slot slot2 : this.quickCraftSlots) {
                        ItemStack itemStack4 = this.getCursorStack();
                        if (slot2 == null || !BankScreenHandler.canInsertItemIntoSlot(slot2, itemStack4, true)
                                || !slot2.canInsert(itemStack4)
                                || this.quickCraftButton != 2 && itemStack4.getCount() < this.quickCraftSlots.size()
                                || !this.canInsertIntoSlot(slot2))
                            continue;
                        int l = slot2.hasStack() ? slot2.getStack().getCount() : 0;
                        int m = itemStack22.getMaxCount();
                        if (slot2 instanceof BankSlot) {
                            m = slot2.getMaxItemCount(itemStack22);
                        }
                        int n = Math
                                .min(BankScreenHandler.calculateStackSize(this.quickCraftSlots, this.quickCraftButton,
                                        itemStack22) + l, m);
                        k -= n - l;
                        slot2.setStack(itemStack22.copyWithCount(n));
                    }
                    itemStack22.setCount(k);
                    this.setCursorStack(itemStack22);
                }
                this.endQuickCraft();
            } else {
                this.endQuickCraft();
            }
        } else if (this.quickCraftStage != 0) {
            this.endQuickCraft();
        } else if ((actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE)
                && (button == 0 || button == 1)) {
            clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
            if (slotIndex == EMPTY_SPACE_SLOT_INDEX) {

                if (!this.getCursorStack().isEmpty()) {

                    if (clickType == ClickType.LEFT) {
                        player.dropItem(this.getCursorStack(), true);
                        this.setCursorStack(ItemStack.EMPTY);
                    } else {
                        player.dropItem(this.getCursorStack().split(1), true);
                    }
                }

                return;
            }
            if (actionType == SlotActionType.QUICK_MOVE) {
                if (slotIndex >= 0) {
                    Slot slot = this.slots.get(slotIndex);
                    if (slot.canTakeItems(player)) {
                        ItemStack itemStack4 = this.quickMove(player, slotIndex);
                        while (!itemStack4.isEmpty() && ItemStack.areItemsEqual(slot.getStack(), itemStack4)) {
                            itemStack4 = this.quickMove(player, slotIndex);
                        }
                    }
                }
            } else {
                if (slotIndex < 0) {
                    return;
                }
                Slot slot = this.slots.get(slotIndex);
                ItemStack hoveredStack = slot.getStack();
                ItemStack cursorStack = this.getCursorStack();
                player.onPickupSlotClick(cursorStack, slot.getStack(), clickType);
                if (!this.handleSlotClick(player, clickType, slot, hoveredStack, cursorStack)) {
                    if (hoveredStack.isEmpty()) {
                        if (!cursorStack.isEmpty()) {
                            int o = clickType == ClickType.LEFT ? cursorStack.getCount() : 1;
                            this.setCursorStack(slot.insertStack(cursorStack, o));
                        }
                    } else if (slot.canTakeItems(player)) {
                        if (cursorStack.isEmpty()) {
                            int o = clickType == ClickType.LEFT
                                    ? Math.min(hoveredStack.getCount(), hoveredStack.getMaxCount())
                                    : (Math.min(hoveredStack.getCount(), hoveredStack.getMaxCount()) + 1) / 2;
                            Optional<ItemStack> optional = slot.tryTakeStackRange(o, Integer.MAX_VALUE, player);
                            optional.ifPresent(stack -> {
                                this.setCursorStack((ItemStack) stack);
                                slot.onTakeItem(player, (ItemStack) stack);
                            });
                        } else if (slot.canInsert(cursorStack)) {
                            if (ItemStack.canCombine(hoveredStack, cursorStack)) {
                                int o = clickType == ClickType.LEFT ? cursorStack.getCount() : 1;
                                this.setCursorStack(slot.insertStack(cursorStack, o));
                            } else if (cursorStack.getCount() <= slot.getMaxItemCount(cursorStack)) {
                                if (hoveredStack.getCount() <= hoveredStack.getMaxCount()) {
                                    this.setCursorStack(hoveredStack);
                                    slot.setStack(cursorStack);
                                }
                            }
                        } else if (ItemStack.canCombine(hoveredStack, cursorStack)) {
                            Optional<ItemStack> optional2 = slot.tryTakeStackRange(hoveredStack.getCount(),
                                    cursorStack.getMaxCount() - cursorStack.getCount(), player);
                            optional2.ifPresent(stack -> {
                                cursorStack.increment(stack.getCount());
                                slot.onTakeItem(player, (ItemStack) stack);
                            });
                        }
                    }
                }
                slot.markDirty();
            }
            return;
        }
        if (actionType == SlotActionType.SWAP) {
            Slot slot3 = this.slots.get(slotIndex);
            itemStack2 = playerInventory.getStack(button);
            itemStack = slot3.getStack();
            if (itemStack2.isEmpty() && itemStack.isEmpty())
                return;
            if (itemStack2.isEmpty()) {
                if (!slot3.canTakeItems(player))
                    return;
                playerInventory.setStack(button, itemStack);
                // slot3.onTake(itemStack.getCount());
                slot3.setStack(ItemStack.EMPTY);
                slot3.onTakeItem(player, itemStack);
                return;
            }
            if (itemStack.isEmpty()) {
                if (!slot3.canInsert(itemStack2))
                    return;
                int p = slot3.getMaxItemCount(itemStack2);
                if (itemStack2.getCount() > p) {
                    slot3.setStack(itemStack2.split(p));
                } else {
                    playerInventory.setStack(button, ItemStack.EMPTY);
                    slot3.setStack(itemStack2);
                }
                return;
            }
            if (!slot3.canTakeItems(player) || !slot3.canInsert(itemStack2))
                return;
            int p = slot3.getMaxItemCount(itemStack2);
            if (itemStack2.getCount() > p) {
                slot3.setStack(itemStack2.split(p));
                slot3.onTakeItem(player, itemStack);
                if (playerInventory.insertStack(itemStack))
                    return;
                player.dropItem(itemStack, true);
                return;
            }
            playerInventory.setStack(button, itemStack);
            slot3.setStack(itemStack2);
            slot3.onTakeItem(player, itemStack);
            return;
        }

        if (actionType == SlotActionType.CLONE && player.getAbilities().creativeMode && this.getCursorStack().isEmpty()
                && slotIndex >= 0) {
            Slot slot3 = this.slots.get(slotIndex);
            if (!slot3.hasStack())
                return;
            ItemStack itemStack5 = slot3.getStack();
            this.setCursorStack(itemStack5.copyWithCount(itemStack5.getMaxCount()));
            return;
        }

        if (actionType == SlotActionType.THROW && this.getCursorStack().isEmpty() && slotIndex >= 0) {
            Slot slot3 = this.slots.get(slotIndex);
            int j = button == 0 ? 1 : slot3.getStack().getCount();
            ItemStack itemStack6 = slot3.takeStackRange(j, Integer.MAX_VALUE, player);
            player.dropItem(itemStack6, true);
        } else if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
            Slot slot3 = this.slots.get(slotIndex);
            ItemStack itemStack7 = this.getCursorStack();
            if (!(itemStack7.isEmpty() || slot3.hasStack() && slot3.canTakeItems(player))) {
                int k = button == 0 ? 0 : this.slots.size() - 1;
                int p = button == 0 ? 1 : -1;
                for (int o = 0; o < 2; ++o) {
                    for (int q = k; q >= 0 && q < this.slots.size()
                            && itemStack7.getCount() < itemStack7.getMaxCount(); q += p) {
                        Slot slot4 = this.slots.get(q);
                        if (!slot4.hasStack() || !BankScreenHandler.canInsertItemIntoSlot(slot4, itemStack7, true)
                                || !slot4.canTakeItems(player) || !this.canInsertIntoSlot(itemStack7, slot4))
                            continue;
                        ItemStack itemStack5 = slot4.getStack();
                        if (o == 0 && itemStack5.getCount() == itemStack5.getMaxCount())
                            continue;
                        ItemStack itemStack6 = slot4.takeStackRange(itemStack5.getCount(),
                                itemStack7.getMaxCount() - itemStack7.getCount(), player);
                        itemStack7.increment(itemStack6.getCount());
                    }
                }
            }
        }
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

    public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl = slot != null && slot.hasStack();
        if (bl && ItemStack.canCombine(stack, slot.getStack())) {
            if (slot instanceof BankSlot bankSlot) {
                return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= bankSlot
                        .getMaxItemCount(stack);

            }
        }
        return ScreenHandler.canInsertItemIntoSlot(slot, stack, allowOverflow);
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

    public void setBankScreenSync(BankScreenSync bankScreenSync) {
        this.updateSyncHandler(bankScreenSync);
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

    public void setLockedSlots(Map<Integer, ItemStack> lockedSlots) {
        BankItemStorage bankItemStorage = (BankItemStorage) this.inventory;

        for (int i = 0; i < this.slots.size(); ++i) {
            Slot slot = this.slots.get(i);
            if (!(slot instanceof BankSlot bankSlot))
                continue;
            ItemStack stack = lockedSlots.get(i);
            if (stack == null) {
                bankItemStorage.lockSlot(i, stack);
                bankSlot.unlock();
            } else {
                bankItemStorage.unlockSlot(i);
                bankSlot.lock(stack);
            }
        }
        this.lockedSlotsMarkDirty();
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
