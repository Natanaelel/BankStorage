package net.natte.bankstorage.blockentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.storage.BankCombinedStorage;
import net.natte.bankstorage.storage.BankSingleStackStorage;
import net.natte.bankstorage.util.Util;

public class BankDockBlockEntity extends BlockEntity {

    private ItemStack bankItem = ItemStack.EMPTY;
    private BankCombinedStorage storage = null;

    public BankDockBlockEntity(BlockPos pos, BlockState state) {
        super(BankStorage.BANK_DOCK_BLOCK_ENTITY, pos, state);
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
        this.markDirty();
        return bank;
    }

    public void putBank(ItemStack bank) {
        this.bankItem = bank.copy();
        this.markDirty();
    }

    @Override
    public void setChanged() {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        this.storage = null;
        super.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        Tag itemAsNbt = this.bankItem.saveOptional(registryLookup);
        nbt.put("bank", itemAsNbt);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.bankItem = ItemStack.parseOptional(registryLookup, nbt.getCompound("bank"));
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
            return Util.getBankItemStorage(this.bankItem, this.level);
        }
        return null;
    }

    public Storage<ItemVariant> getItemStorage() {

        if (this.storage == null) {
            BankItemStorage bankItemStorage = getInventory();
            if (bankItemStorage == null) {
                return Storage.empty();
            }
            bankItemStorage.usedByPlayerUUID = FakePlayer.DEFAULT_UUID;
            bankItemStorage.usedByPlayerName = "World";
            this.storage = createItemStorage(bankItemStorage);
        }
        return this.storage;

    }

    private BankCombinedStorage createItemStorage(BankItemStorage bankItemStorage) {
        int slots = bankItemStorage.size();

        List<BankSingleStackStorage> storages = new ArrayList<>();

        for (int i = 0; i < slots; i++) {
            BankSingleStackStorage storage = new BankSingleStackStorage(bankItemStorage, i);
            storages.add(storage);
        }

        return new BankCombinedStorage(storages, Util.getOrCreateOptions(this.bankItem).pickupMode);
    }
}
