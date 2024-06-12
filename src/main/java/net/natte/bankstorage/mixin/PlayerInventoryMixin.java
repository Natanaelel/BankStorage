package net.natte.bankstorage.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.natte.bankstorage.inventory.ItemPickupHandler;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void onInsertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean nothingLeft = ItemPickupHandler.pickUpStack(stack, (Inventory) (Object) this);
        if (nothingLeft)
            cir.setReturnValue(true);
    }
}
