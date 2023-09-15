package net.natte.bankstorage.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class BankSlot extends Slot {

    public int slotStorageMultiplier;

    public int count;

    public BankSlot(Inventory inventory, int index, int x, int y, int slotStorageMultiplier) {
        super(inventory, index, x, y);
        this.slotStorageMultiplier = slotStorageMultiplier;
        this.count = 0;
    }

    @Override
    public int getMaxItemCount() {
        return super.getMaxItemCount();
        // return 64 * this.slotStorageMultiplier;
    }
    @Override
    public int getMaxItemCount(ItemStack stack) {
        return super.getMaxItemCount(stack);
        // return super.getMaxItemCount(stack) * this.slotStorageMultiplier;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public ItemStack getStack() {
        // TODO Auto-generated method stub
        return super.getStack();//.copyWithCount(this.count);
    }
    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return super.insertStack(stack, count);
        // if (stack.isEmpty() || !this.canInsert(stack)) {
        //     return stack;
        // }
        // ItemStack itemStack = this.getStack();
        // int spaceLeft = this.getMaxItemCount(stack) - this.count;
        // int i = Math.min(Math.min(count, stack.getCount()), spaceLeft);
        // if (itemStack.isEmpty()) {
        //     this.setStack(stack.split(i));
            
        // } else if (ItemStack.canCombine(itemStack, stack)) {
        //     stack.decrement(i);
        //     itemStack.increment(i);
        //     super.setStack(itemStack);

        //     this.count += i;
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
        super.setStack(stack);
        // System.out.println("setstack");
        // System.out.println(stack);
        // this.count = stack.getCount();
        // super.setStack(stack);
        // this.markDirty();
    }
    

    public void render(DrawContext context){
        com.mojang.datafixers.util.Pair<Identifier, Identifier> pair;
        Slot slot = (Slot)this;
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getStack();
        boolean bl = false;
        /* */
        Slot touchDragSlotStart = null;
        ItemStack touchDragStack = ItemStack.EMPTY;
        boolean touchIsRightClickDrag = false;

        int heldButtonType = 0;
        /* */
        boolean bl2 = slot == touchDragSlotStart && !touchDragStack.isEmpty() && !touchIsRightClickDrag;
        // ItemStack itemStack2 = this.handler.getCursorStack();
        ItemStack itemStack2 = this.getStack();
        String string = null;
        // if (slot == touchDragSlotStart && !touchDragStack.isEmpty() && touchIsRightClickDrag && !itemStack.isEmpty()) {
        //     itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        // } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !itemStack2.isEmpty()) {
        //     if (this.cursorDragSlots.size() == 1) {
        //         return;
        //     }
        //     if (ScreenHandler.canInsertItemIntoSlot((Slot)slot, (ItemStack)itemStack2, (boolean)true) && this.handler.canInsertIntoSlot(slot)) {
        //         bl = true;
        //         int k = Math.min(itemStack2.getMaxCount() * this.type.slotStorageMultiplier, slot.getMaxItemCount(itemStack2));
        //         int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
        //         int m = ScreenHandler.calculateStackSize(this.cursorDragSlots, (int)heldButtonType, (ItemStack)itemStack2) + l;
        //         if (m > k) {
        //             m = k;
        //             string = Formatting.YELLOW.toString() + k;
        //         }
        //         itemStack = itemStack2.copyWithCount(m);
        //     } else {
        //         this.cursorDragSlots.remove(slot);
        //         // this.calculateOffset();
        //     }
        // }
        
        // System.out.println("render?");
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        MinecraftClient client = MinecraftClient.getInstance();
        if (itemStack.isEmpty() && slot.isEnabled() && (pair = slot.getBackgroundSprite()) != null) {
            Sprite sprite = client.getSpriteAtlas((Identifier)pair.getFirst()).apply((Identifier)pair.getSecond());
            context.drawSprite(i, j, 0, 16, 16, sprite);
            bl2 = true;
        }
        if (!bl2) {
            if (bl) {
                context.fill(i, j, i + 16, j + 16, -2130706433);
            }
            // context.drawItem(itemStack, i, j, slot.x + slot.y * this.backgroundWidth);
            context.drawItem(itemStack, i, j, slot.x + slot.y * 123);
            // context.drawItemInSlot(this.textRenderer, itemStack, i, j, "900");
            context.drawItemInSlot(client.textRenderer, itemStack, i, j, itemStack.getCount() + "");
        }
        context.getMatrices().pop();
    
    }
    
    

    
}
