package net.natte.bankstorage.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import java.time.LocalDateTime;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.storage.BankContainer;
import net.natte.bankstorage.storage.BankItemHandler;
import net.natte.bankstorage.storage.BankSlotData;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.util.Util;

public class BankItemStorage {

    public BankType type;
    public UUID uuid;

    // this is where the items are stored
    private List<ItemStack> items;

    private Map<Integer, ItemStack> lockedSlots;
    private short lockedSlotsRevision = 0;
    private short revision = 1; // start different from client (0) to update client cache

    private ItemStack bankLikeItem;
    public UUID usedByPlayerUUID = BankStorage.FAKE_PLAYER_UUID;
    public String usedByPlayerName = "World";
    public LocalDateTime dateCreated = LocalDateTime.MIN;

    public BankItemStorage(BankType type, UUID uuid) {
        this.type = type;
        this.uuid = uuid;

        this.lockedSlots = new HashMap<>();

        this.items = NonNullList.createWithCapacity(type.size());

    }

    public BankItemStorage withItem(ItemStack itemStack) {
        this.bankLikeItem = itemStack;
        return this;
    }

    public BankItemStorage asType(BankType type) {
        assert type.size() >= this.type.size() : "Cannot downgrade banks!";
        if (this.type != type) {
            if (type.size() < this.type.size()) {
//                BankStorage.LOGGER.error("Cannot downgrade banks! This probably happened because of a duplicated bank item (there are links! use them!)");
                return this;
            }
            return changeType(type);
        }
        return this;
    }

    public BankItemStorage changeType(BankType type) {
        BankStorage.LOGGER.debug("Upgrading bank from " + this.type.getName() + " to " + type.getName() + " uuid " + this.uuid);

        assert type.size() > this.type.size() : "Cannot downgrade banks!";

        BankItemStorage newBankItemStorage = new BankItemStorage(type, this.uuid);
        for (int i = 0; i < type.size(); ++i) {
            newBankItemStorage.items.set(i, i < this.items.size() ? this.items.get(i) : new BankSlotData());
            newBankItemStorage.lockedSlots = this.lockedSlots;
        }
        return newBankItemStorage;
    }

    public ItemStack getItem() {
        return this.bankLikeItem;
    }

    public BankItemHandler getItemHandler(PickupMode pickupMode){
        return new BankItemHandler(items, lockedSlots, pickupMode);
    }

    public Container getContainer(){
        return new BankContainer(this);
    }

    public void markDirty() {
        updateLockedSlotsRevision();
        updateRevision();
    }

    public int size() {
        return this.items.size();
    }

    public int getMaxCountPerStack() {
        return this.type.stackLimit;
    }

    @Override
    public int getMaxCount(ItemStack stack) {
        return getMaxCountPerStack();
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!Util.isAllowedInBank(stack))
            return false;

        return super.canInsert(stack);
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public List<ItemStack> getBlockItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                items.add(stack);
        }
        return items;
    }

    public ItemStack getSelectedItem(int selectedItemSlot) {
        List<ItemStack> items = getBlockItems();
        return items.isEmpty() ? ItemStack.EMPTY : items.get(Math.min(selectedItemSlot, items.size() - 1));
    }

    public ItemStack getRandomItem(Random random) {
        List<ItemStack> items = getBlockItems();
        return items.isEmpty() ? ItemStack.EMPTY : items.get(random.nextInt(items.size()));
    }

    public ItemStack chooseItemToPlace(BankOptions options, Random random) {

        return switch (options.buildMode) {
            case NONE -> ItemStack.EMPTY;
            case NORMAL -> getSelectedItem(options.selectedItemSlot);
            case RANDOM -> getRandomItem(random);
        };
    }

    public @Nullable ItemStack getLockedStack(int slotIndex) {
        return this.lockedSlots.get(slotIndex);
    }

    public void lockSlot(int slotIndex, ItemStack itemStack) {
        this.lockedSlots.put(slotIndex, itemStack.copyWithCount(1));
    }

    public void unlockSlot(int slotIndex) {
        this.lockedSlots.remove(slotIndex);
    }

    public Map<Integer, ItemStack> getlockedSlots() {
        return this.lockedSlots;
    }

    public short getLockedSlotsRevision() {
        return this.lockedSlotsRevision;
    }

    public void updateLockedSlotsRevision() {
        this.lockedSlotsRevision = (short) ((this.lockedSlotsRevision + 1) & Short.MAX_VALUE);
    }

    public short getRevision() {
        return this.revision;
    }

    private void updateRevision() {
        this.revision = (short) ((this.revision + 1) & Short.MAX_VALUE);
    }

    public static BankItemStorage createFromCodec(
            UUID uuid,
            BankType type,
            List<ItemStack> items,
            Map<Integer, ItemStack> lockedSlots,
            String dateCreated,
            UUID lastUsedByPlayerUuid,
            String lastUsedByPlayerName) {

        BankItemStorage bankItemStorage = new BankItemStorage(type, uuid);

        for (int i = 0; i < items.size(); ++i) {
            // SETSTACK
            bankItemStorage.items.set(i, items.get(i));
        }

        for (int i : lockedSlots.keySet()) {
            bankItemStorage.lockSlot(i, lockedSlots.get(i));
        }
        bankItemStorage.dateCreated = LocalDateTime.parse(dateCreated);
        bankItemStorage.usedByPlayerUUID = lastUsedByPlayerUuid;
        bankItemStorage.usedByPlayerName = lastUsedByPlayerName;

        return bankItemStorage;
    }
}
