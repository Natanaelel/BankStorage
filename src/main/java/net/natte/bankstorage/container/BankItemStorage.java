package net.natte.bankstorage.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import java.time.LocalDateTime;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;

public class BankItemStorage extends SimpleInventory implements ExtendedScreenHandlerFactory<ItemStack> {

    // public BankOptions options;
    public BankType type;
    // private Text displayName;
    public UUID uuid;

    private Map<Integer, ItemStack> lockedSlots;
    private short lockedSlotsRevision = 0;
    private short revision = 1; // start different from client (0) to update client cache

    private ItemStack bankLikeItem;
    public UUID usedByPlayerUUID = FakePlayer.DEFAULT_UUID;
    public String usedByPlayerName = "World";
    public LocalDateTime dateCreated = LocalDateTime.MIN;

    public BankItemStorage(BankType type, UUID uuid) {
        super(type.rows * type.cols);
        this.type = type;
        // this.options = new BankOptions();
        this.uuid = uuid;

        this.lockedSlots = new HashMap<>();

    }

    public BankItemStorage withItem(ItemStack itemStack) {
        this.bankLikeItem = itemStack;
        return this;
    }

    public BankItemStorage asType(BankType type) {
        if (this.type != type) {
            if (type.size() < this.type.size()) {
                BankStorage.LOGGER.error(Util.invalid("BankItemStorage.asType(BankType)").getString());
                return this;
            }
            return changeType(type);
        }
        return this;
    }

    public BankItemStorage changeType(BankType type) {
        BankStorage.LOGGER
                .info("Upgrading bank from " + this.type.getName() + " to " + type.getName() + " uuid " + this.uuid);
        BankItemStorage newBankItemStorage = new BankItemStorage(type, this.uuid);
        for (int i = 0; i < this.heldStacks.size(); ++i) {
            newBankItemStorage.heldStacks.set(i, this.heldStacks.get(i));
            newBankItemStorage.lockedSlots = this.lockedSlots;
        }
        return newBankItemStorage;
    }

    @Override
    public BankScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BankScreenHandler(syncId, playerInventory, this, this.type,
                ScreenHandlerContext.EMPTY);
    }

    public ExtendedScreenHandlerFactory<ItemStack> withDockPosition(BlockPos pos) {
        return new ExtendedScreenHandlerFactory<ItemStack>() {

            public BankScreenHandler createMenu(int syncId, PlayerInventory playerInventory,
                    PlayerEntity playerEntity) {
                return new BankScreenHandler(syncId, playerInventory,
                        BankItemStorage.this,
                        BankItemStorage.this.type,
                        ScreenHandlerContext.create(playerEntity.getWorld(), pos));
            }

            @Override
            public Text getDisplayName() {
                return BankItemStorage.this.getDisplayName();
            }

            @Override
            public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
                return BankItemStorage.this.getScreenOpeningData(player);
            }

            // @Override
            // public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
            //     BankItemStorage.this.writeScreenOpeningData(player, buf);
            // }
        };
    }

    @Override
    public Text getDisplayName() {
        if (this.bankLikeItem == null) {
            return Util.invalid("getDisplayName()");
        }

        return this.bankLikeItem.getName();
    }

    public ItemStack getItem() {
        return this.bankLikeItem;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateLockedSlotsRevision();
        updateRevision();
        // if (this.uuid != null) {
        // CachedBankStorage.requestCacheUpdate(this.uuid);
        // }
    }

    @Override
    public int size() {
        return this.heldStacks.size();
    }

    @Override
    public int getMaxCountPerStack() {
        return this.type.stackLimit;
    }

    // same format as vanilla except slot saved as int instead
    // of byte
    public NbtCompound saveToNbt() {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putUuid("uuid", this.uuid);
        nbtCompound.putString("uuid_string", this.uuid.toString());
        nbtCompound.putString("type", this.type.getName());

        NbtList nbtList = new NbtList();
        for (int i = 0; i < this.heldStacks.size(); ++i) {
            ItemStack itemStack = this.heldStacks.get(i);
            if (itemStack.isEmpty())
                continue;

            NbtCompound itemNbtCompound = (NbtCompound) ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack).getOrThrow();
            itemNbtCompound.putInt("Slot", i);

            nbtList.add(itemNbtCompound);
        }
        nbtCompound.put("Items", nbtList);

        NbtList lockedSlotsNbt = new NbtList();

        this.lockedSlots.forEach((slot, lockedStack) -> {
            NbtCompound lockedSlotNbt = (NbtCompound) ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, lockedStack).getOrThrow();
            
            lockedSlotNbt.putInt("Slot", slot);

            lockedSlotsNbt.add(lockedSlotNbt);
        });
        nbtCompound.put("LockedSlots", lockedSlotsNbt);

        nbtCompound.putUuid("last_used_by_uuid", this.usedByPlayerUUID);
        nbtCompound.putString("last_used_by_uuid_string", this.usedByPlayerUUID.toString());
        nbtCompound.putString("last_used_by_player", this.usedByPlayerName);
        nbtCompound.putString("date_created", this.dateCreated.toString());

        return nbtCompound;
    }

    // same format as vanilla except slot saved as int instead
    // of byte
    public static BankItemStorage createFromNbt(NbtCompound nbtCompound) {

        UUID uuid = nbtCompound.getUuid("uuid");
        BankType type = getBankTypeFromName(nbtCompound.getString("type"));

        BankItemStorage bankItemStorage = new BankItemStorage(type, uuid);

        NbtList nbtList = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbt = nbtList.getCompound(i);
            int j = nbt.getInt("Slot");
            if (j < 0 || j >= bankItemStorage.heldStacks.size()) {
                BankStorage.LOGGER.info("tried to insert item into slot " + j + " but storage size is only "
                        + bankItemStorage.heldStacks.size());
                continue;
            }

            ItemStack itemStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
            bankItemStorage.heldStacks.set(j, itemStack);
        }

        NbtList lockedSlotsNbt = nbtCompound.getList("LockedSlots", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < lockedSlotsNbt.size(); ++i) {
            NbtCompound lockedSlotNbt = lockedSlotsNbt.getCompound(i);
            ItemStack itemStack = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, lockedSlotNbt).getOrThrow();
            bankItemStorage.lockSlot(lockedSlotNbt.getInt("Slot"), itemStack);

        }
        if (nbtCompound.containsUuid("last_used_by_uuid"))
            bankItemStorage.usedByPlayerUUID = nbtCompound.getUuid("last_used_by_uuid");
        if (nbtCompound.contains("last_used_by_name", NbtElement.STRING_TYPE))
            bankItemStorage.usedByPlayerName = nbtCompound.getString("last_used_by_name");
        if (nbtCompound.contains("date_created", NbtElement.STRING_TYPE))
            bankItemStorage.dateCreated = LocalDateTime.parse(nbtCompound.getString("date_created"));

        return bankItemStorage;
    }

    public static BankType getBankTypeFromName(String name) {
        for (BankType bankType : BankStorage.bankTypes) {
            if (bankType.getName().equals(name)) {
                return bankType;
            }
        }

        throw new Error("Cannot get BankType of name '" + name + "'");
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (!Util.isAllowedInBank(stack))
            return false;

        return super.canInsert(stack);
    }

    public List<ItemStack> getItems() {
        return this.heldStacks;
    }
    public List<ItemStack> getBlockItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : this.heldStacks) {
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

    // @Override
    // public void ScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
    //     buf.writeItemStack(bankLikeItem);
    // }

    @Override
    public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
        return bankLikeItem;
    }

}
