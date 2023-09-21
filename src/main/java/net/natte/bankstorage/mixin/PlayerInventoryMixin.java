package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.inventory.ItemPickupHandler;


@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    
    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void onInsertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(ItemPickupHandler.onItemPickup(stack, (PlayerInventory)(Object)this)) cir.setReturnValue(true);;
    }
}
