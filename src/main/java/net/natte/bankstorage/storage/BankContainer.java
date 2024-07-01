package net.natte.bankstorage.storage;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankItemStorage;

import java.util.Collections;
import java.util.List;

public class BankContainer implements Container {

    private final BankItemStorage bankItemStorage;
    private final List<ItemStack> items;

    public BankContainer(BankItemStorage bankItemStorage) {
        this.bankItemStorage = bankItemStorage;
        this.items = bankItemStorage.getItems();
    }

    @Override
    public int getContainerSize() {
        return bankItemStorage.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public void setChanged() {
        bankItemStorage.markDirty();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        // who would call this??
        Collections.fill(items, ItemStack.EMPTY);
    }
}
