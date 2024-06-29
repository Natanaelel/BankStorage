package net.natte.bankstorage.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.storage.BankItemHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BankDockBlockEntity extends BlockEntity {

    private static final String BANK_ITEM_KEY = "bank";

    @NotNull
    private ItemStack bankItem = ItemStack.EMPTY;
    private BankItemHandler bankItemHandler = null;

    public BankDockBlockEntity(BlockPos pos, BlockState state) {
        super(BankStorage.BANK_DOCK_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasBank() {
        return !this.bankItem.isEmpty();
    }

    public ItemStack getBank() {
        return this.bankItem;
    }

    public ItemStack pickUpBank() {
        ItemStack bank = this.bankItem;
        this.bankItem = ItemStack.EMPTY;
        this.setChanged();
        return bank;
    }

    public void putBank(ItemStack bank) {
        this.bankItem = bank.copy();
        this.setChanged();
    }

    @Override
    public void setChanged() {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        level.invalidateCapabilities(worldPosition);
        this.bankItemHandler = null;
        super.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        Tag itemAsNbt = this.bankItem.saveOptional(registryLookup);
        nbt.put(BANK_ITEM_KEY, itemAsNbt);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.bankItem = ItemStack.parseOptional(registryLookup, nbt.getCompound(BANK_ITEM_KEY));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    private BankItemStorage getInventory() {

        if (this.level.isClientSide)
            return null;

        if (Util.isBankLike(this.bankItem)) {
            return Util.getBankItemStorage(this.bankItem);
        }
        return null;
    }

    public IItemHandler getItemHandler() {
        if (!this.hasBank())
            return null;

        if (this.bankItemHandler == null) {
            BankItemStorage bankItemStorage = getInventory();
            if (bankItemStorage == null) {
                return null;
            }
            bankItemStorage.usedByPlayerUUID = BankStorage.FAKE_PLAYER_UUID;
            bankItemStorage.usedByPlayerName = "World";
            BankOptions options = bankItem.get(BankStorage.OptionsComponentType);
            PickupMode pickupMode = options == null ? PickupMode.ALL : options.pickupMode();
            this.bankItemHandler = bankItemStorage.getItemHandler(pickupMode);
        }
        return this.bankItemHandler;

    }
}
