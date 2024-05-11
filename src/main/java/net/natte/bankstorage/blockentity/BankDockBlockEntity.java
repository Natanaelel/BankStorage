package net.natte.bankstorage.blockentity;

import java.util.ArrayList;
import java.util.List;

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
    public void markDirty() {
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        this.storage = null;
        super.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        NbtElement itemAsNbt = this.bankItem.encodeAllowEmpty(registryLookup);
        nbt.put("bank", itemAsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        this.bankItem = ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("bank"));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    private BankItemStorage getInventory() {
        
        if(this.world.isClient)
            return null;

        if (Util.isBankLike(this.bankItem)) {
            return Util.getBankItemStorage(this.bankItem, this.world);
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
