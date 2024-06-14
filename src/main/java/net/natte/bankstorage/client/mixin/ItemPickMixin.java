package net.natte.bankstorage.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.events.PickBlockEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class ItemPickMixin {

    @ModifyExpressionValue(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"))
    private int getSlotWithItem(int slot, @Local ItemStack pickedStack) {
        if (PickBlockEvents.pickBlock(pickedStack))
            slot = -1;
        return slot;
    }
}
