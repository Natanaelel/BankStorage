package net.natte.bankstorage.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.natte.bankstorage.inventory.BankSlot;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {

    @Inject(method = "canItemQuickReplace", at = @At("HEAD"), cancellable = true)
    private static void canInsertItemIntoSlotMixin(@Nullable Slot slot, ItemStack stack, boolean allowOverflow,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (slot instanceof BankSlot bankSlot) {
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
                cir.setReturnValue(slotStack.getCount() + (allowOverflow ? 0 : stack.getCount()) <= bankSlot
                        .getMaxStackSize(stack));

            }
        }
    }

    @ModifyExpressionValue(method = "doClick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private static int getMaxCountAllowedInSlot(int originalMaxCount, @Local(ordinal = 1) Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }

    @ModifyExpressionValue(method = "canItemQuickReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private static int getMaxStackSize(int originalMaxCount, @Local Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }
}
