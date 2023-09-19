package net.natte.bankstorage.blockentity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.inventory.BankSingleStackStorage;
import net.natte.bankstorage.item.BankItem;

public class BankDockBlockEntity extends BlockEntity {

    private ItemStack bankItem = ItemStack.EMPTY;
    private CombinedStorage<ItemVariant, SingleStackStorage> storage = null;

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
        this.storage = null;
        return bank;
    }

    public void putBank(ItemStack bank) {
        this.bankItem = bank.copy();
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtCompound itemAsNbt = new NbtCompound();
        this.bankItem.writeNbt(itemAsNbt);
        nbt.put("bank", itemAsNbt);
        // System.out.println("saving " + nbt);
        // System.out.println("saved " + this.bankItem);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        // System.out.println("loading " + nbt);

        this.bankItem = ItemStack.fromNbt(nbt.getCompound("bank"));
        // System.out.println("loaded " + this.bankItem);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    private BankItemStorage getInventory() {
        if (this.bankItem.getItem() instanceof BankItem) {
            return BankItem.getBankItemStorage(this.bankItem, this.world);
        }
        return null;
    }

    public Storage<ItemVariant> getItemStorage() {

        
        if (this.storage == null) {
            BankItemStorage bankItemStorage = getInventory();
            if(bankItemStorage == null){
                return Storage.empty();
            }
            this.storage = createItemStorage(bankItemStorage);
        }
        return this.storage;

    }

    private CombinedStorage<ItemVariant, SingleStackStorage> createItemStorage(BankItemStorage bankItemStorage) {
        int slots = bankItemStorage.size();

        List<SingleStackStorage> storages = new ArrayList<>();

        for (int i = 0; i < slots; i++) {
            SingleStackStorage storage = new BankSingleStackStorage(bankItemStorage, i);
            storages.add(storage);
        }

        return new CombinedStorage<>(storages);
    }

}
