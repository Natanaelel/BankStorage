package net.natte.bankstorage.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class BankItemStorage extends SimpleInventory implements NamedScreenHandlerFactory {

    public BankOptions options;
    public BankType type;
    private Text displayName;
    public UUID uuid;
    public Random random;

    private Map<Integer, ItemStack> lockedSlots;

    public BankItemStorage(BankType type, UUID uuid) {
        super(type.rows * type.cols);
        this.type = type;
        this.options = new BankOptions();
        this.uuid = uuid;
        this.random = new Random();

        this.lockedSlots = new HashMap<>();
    }

    public BankItemStorage withDisplayName(Text displayName) {
        this.displayName = displayName;
        return this;
    }

    public BankItemStorage asType(BankType type) {
        if (this.type != type) {
            return changeType(type);
        }
        return this;
    }

    public BankItemStorage changeType(BankType type) {
        BankStorage.LOGGER
                .info("Upgrading bank from " + this.type.getName() + " to " + type.getName() + " uuid " + this.uuid);
        BankItemStorage newBankItemStorage = new BankItemStorage(type, this.uuid).withDisplayName(displayName);
        for (int i = 0; i < this.stacks.size(); ++i) {
            newBankItemStorage.stacks.set(i, this.stacks.get(i));
        }
        return newBankItemStorage;
    }

    @Override
    public BankScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BankScreenHandler(syncId, playerInventory, this, this.type);
    }

    @Override
    public Text getDisplayName() {
        return displayName;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.uuid != null)
            CachedBankStorage.bankRequestQueue.add(this.uuid);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public int getMaxCountPerStack() {
        return 64 * this.type.slotStorageMultiplier;
    }

    public int getStorageMultiplier() {
        return this.type.slotStorageMultiplier;
    }

    // same format as vanilla except itemstack count and slot saved as int instead
    // of byte
    public NbtCompound saveToNbt() {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putUuid("uuid", this.uuid);
        nbtCompound.put("options", this.options.asNbt());
        nbtCompound.putString("type", this.type.getName());

        NbtList nbtList = new NbtList();
        for (int i = 0; i < this.stacks.size(); ++i) {
            ItemStack itemStack = this.stacks.get(i);
            if (itemStack.isEmpty())
                continue;

            NbtCompound itemNbtCompound = Util.largeStackAsNbt(itemStack);
            itemNbtCompound.putInt("Slot", i);

            nbtList.add(itemNbtCompound);
        }
        nbtCompound.put("Items", nbtList);

        
        NbtList lockedSlotsNbt = new NbtList();

        this.lockedSlots.forEach((slot, lockedStack) -> {
            NbtCompound lockedSlotNbt = new NbtCompound();
            lockedSlotNbt.putInt("Slot", slot);
            lockedStack.writeNbt(lockedSlotNbt);
            lockedSlotsNbt.add(lockedSlotNbt);
        });
        nbtCompound.put("LockedSlots", lockedSlotsNbt);

        return nbtCompound;
    }

    // same format as vanilla except itemstack count and slot saved as int instead
    // of byte
    public static BankItemStorage createFromNbt(NbtCompound nbtCompound) {

        UUID uuid = nbtCompound.getUuid("uuid");
        BankType type = getBankTypeFromName(nbtCompound.getString("type"));

        BankItemStorage bankItemStorage = new BankItemStorage(type, uuid);

        bankItemStorage.options = BankOptions.fromNbt(nbtCompound.getCompound("options"));

        Inventories.readNbt(nbtCompound, bankItemStorage.stacks);
        NbtList nbtList = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbt = nbtList.getCompound(i);
            int j = nbt.getInt("Slot");
            if (j < 0 || j >= bankItemStorage.stacks.size()) {
                BankStorage.LOGGER.info("tried to insert item into slot " + j + " but storage size is only "
                        + bankItemStorage.stacks.size());
                continue;
            }

            ItemStack itemStack = Util.largeStackFromNbt(nbt);
            bankItemStorage.stacks.set(j, itemStack);
        }

        NbtList lockedSlotsNbt = nbtCompound.getList("LockedSlots", NbtElement.COMPOUND_TYPE);
        for(int i = 0 ; i < lockedSlotsNbt.size(); ++i){
            NbtCompound lockedSlotNbt = lockedSlotsNbt.getCompound(i);
            bankItemStorage.lockSlot(lockedSlotNbt.getInt("Slot"), ItemStack.fromNbt(lockedSlotNbt));
            
        }

        return bankItemStorage;
    }

    private static BankType getBankTypeFromName(String name) {
        for (BankType bankType : BankStorage.bankTypes) {
            if (bankType.getName().equals(name)) {
                return bankType;
            }
        }

        throw new Error("Cannot get BankType of name '" + name + "'");
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !(stack.getItem() instanceof BankItem) && super.canInsert(stack);
    }

    public List<ItemStack> getBlockItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                items.add(stack);
        }
        return items;
    }

    public ItemStack getSelectedItem() {
        List<ItemStack> items = getBlockItems();
        return items.isEmpty() ? ItemStack.EMPTY : items.get(this.options.selectedItemSlot);
    }

    public ItemStack getRandomItem() {
        List<ItemStack> items = getBlockItems();
        return items.isEmpty() ? ItemStack.EMPTY : items.get(this.random.nextInt(items.size()));
    }

    public ItemStack chooseItemToPlace() {

        return switch (this.options.buildMode) {
            case NONE -> ItemStack.EMPTY;
            case NORMAL -> getSelectedItem();
            case RANDOM -> getRandomItem();
        };
    }

    public @Nullable ItemStack getLockedStack(int slotIndex){
        return this.lockedSlots.get(slotIndex);
    }

    public void lockSlot(int slotIndex, ItemStack itemStack){
        this.lockedSlots.put(slotIndex, itemStack.copyWithCount(1));
    }

    public void unlockSlot(int slotIndex){
        this.lockedSlots.remove(slotIndex);
    }
}
