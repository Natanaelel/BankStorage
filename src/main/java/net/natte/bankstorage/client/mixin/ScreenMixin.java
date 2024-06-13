package net.natte.bankstorage.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.natte.bankstorage.inventory.BankSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public abstract class ScreenMixin {
    @ModifyExpressionValue(method = "recalculateQuickCraftRemaining", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0))
    private int getMaxCountAllowedInSlot(int originalMaxCount, @Local Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }

    @ModifyExpressionValue(method = "renderSlot", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int renderSlotgetMaxCountAllowedInSlot(int originalMaxCount, @Local(argsOnly = true) Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            return bankSlot.getMaxStackSize();
        return originalMaxCount;
    }
}
