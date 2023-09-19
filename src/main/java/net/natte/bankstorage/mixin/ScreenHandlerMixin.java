package net.natte.bankstorage.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.natte.bankstorage.inventory.BankSlot;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    
      @Inject(method = "Lnet/minecraft/screen/ScreenHandler;canInsertItemIntoSlot(Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/item/ItemStack;Z)Z", at = @At("HEAD"), cancellable = true)
    private static void canInsertItemIntoSlotMixin(@Nullable Slot slot, ItemStack stack, boolean allowOverflow,
            CallbackInfoReturnable<Boolean> cir) {
        boolean bl = slot == null || !slot.hasStack();
        if (!bl && ItemStack.canCombine(stack, slot.getStack())) {
            if (slot instanceof BankSlot bankSlot) {
                cir.setReturnValue(slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= bankSlot
                        .getMaxItemCount(stack));

            }
        }
    }
}
