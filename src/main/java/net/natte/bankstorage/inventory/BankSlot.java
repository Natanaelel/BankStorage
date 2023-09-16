package net.natte.bankstorage.inventory;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.item.BankItem;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;

    public int count;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
        this.count = 0;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        // TODO Auto-generated method stub
        return !(stack.getItem() instanceof BankItem) && super.canInsert(stack);
    }

    @Override
    public int getMaxItemCount() {
        // return 32;
        // return super.getMaxItemCount();
        // System.out.println("getMaxItemCount () = " + 64 *
        // this.slotStorageMultiplier);
        return 64 * this.slotStorageMultiplier;
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        // return super.getMaxItemCount(stack);
        int c = Math.min(this.getMaxItemCount(), stack.getMaxCount() * this.slotStorageMultiplier);
        // return super.getMaxItemCount(stack) * this.slotStorageMultiplier;
        // System.out.println("getMaxItemCount itemstack = " + c);
        return c;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ItemStack getStack() {
        // TODO Auto-generated method stub
        return super.getStack();// .copyWithCount(this.count);
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        System.out.println("insertStack");
        System.out.println(count + " | " + stack);
        return super.insertStack(stack, count);
        // if (stack.isEmpty() || !this.canInsert(stack)) {
        // return stack;
        // }
        // ItemStack itemStack = this.getStack();
        // System.out.println("insert into current stack: " + itemStack);
        // int i = Math.min(Math.min(count, stack.getCount()),
        // this.getMaxItemCount(stack) - itemStack.getCount());
        // System.out.println("i = " + i);
        // if (itemStack.isEmpty()) {
        // System.out.println("into empty");
        // System.out.println("split " + stack + " to " + i);
        // System.out.println("split " + stack + " to " + i);
        // ItemStack temp = stack.split(i);
        // System.out.println("result: " + temp);
        // this.setStack(temp);
        // } else if (ItemStack.canCombine(itemStack, stack)) {
        // System.out.println("into non-empty");
        // stack.decrement(i);
        // itemStack.increment(i);
        // this.setStack(itemStack);
        // }
        // return stack;

        // if (stack.isEmpty() || !this.canInsert(stack)) {
        // return stack;
        // }
        // ItemStack itemStack = this.getStack();
        // int spaceLeft = this.getMaxItemCount(stack) - this.count;
        // int i = Math.min(Math.min(count, stack.getCount()), spaceLeft);
        // if (itemStack.isEmpty()) {
        // this.setStack(stack.split(i));

        // } else if (ItemStack.canCombine(itemStack, stack)) {
        // stack.decrement(i);
        // itemStack.increment(i);
        // super.setStack(itemStack);

        // this.count += i;
        // }
        // super.setStack(this.getStack());
        // this.markDirty();
        // return stack;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return super.takeStack(amount);
        // TODO Auto-generated method stub
        // ItemStack ret = super.takeStack(amount);
        // this.count -= ret.getCount();
        // this.markDirty();
        // return ret;
    }

    @Override
    protected void onTake(int amount) {
        // TODO Auto-generated method stub
        super.onTake(amount);
    }

    @Override
    public void setStack(ItemStack stack) {

        System.out.println("setstack " + stack);
        // super.setStack(stack);
        this.inventory.setStack(this.getIndex(), stack);
        System.out.println("setstack result " + stack);
        // for(var trace : Thread.currentThread().getStackTrace()){
        // System.out.println(trace);
        // }
        // this.count = stack.getCount();
        // super.setStack(stack);
        // this.markDirty();
    }

    // limit items picked up to stack size, prevent cursorstack to be larger than
    // normally possible
    @Override
    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        if (!this.canTakeItems(player)) {
            return Optional.empty();
        }
        if (!this.canTakePartial(player) && max < this.getStack().getCount()) {
            return Optional.empty();
        }

        
        ItemStack itemStack = this.takeStack(Math.min(Math.min(min, max), this.getStack().getMaxCount()));
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getStack().isEmpty()) {
            this.setStack(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }
}
