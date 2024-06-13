package net.natte.bankstorage.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

    @ModifyExpressionValue(method = "doClick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0))
    private int getMaxCountAllowedInSlot(int originalMaxCount, @Local Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }

    @ModifyExpressionValue(method = "canItemQuickReplace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private static int getMaxStackSize(int originalMaxCount, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }

    @ModifyReturnValue(method = "canItemQuickReplace", at = @At("RETURN"))
    private static boolean canInsertItemIntoSlot(boolean result, @Local(argsOnly = true) ItemStack stackToInsert, @Local(argsOnly = true) Slot slot) {
        if (result && slot instanceof BankSlot bankSlot) {
            return bankSlot.mayPlace(stackToInsert);
        }
        return result;
    }
}
