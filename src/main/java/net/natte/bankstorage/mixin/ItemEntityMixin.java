package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.natte.bankstorage.util.Util;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    // make dropped bankItem never despawn
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    public void noDespawn(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (Util.isBank(stack)) {
            ((ItemEntity) (Object) this).setNeverDespawn();
        }
    }
}
