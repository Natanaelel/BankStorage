package net.natte.bankstorage.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.inventory.ItemPickupHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemHandlerHelper.class)
public abstract class ItemHandlerHelperMixin {

    @ModifyVariable(method = "giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", at = @At("STORE"), ordinal = 1)
    private static ItemStack onAssignRemainder(ItemStack stack, @Local(argsOnly = true) Player player, @Local(argsOnly = true) int preferredSlot) {
        if (preferredSlot != -1)
            return stack;

        ItemStack copy = stack.copy();

        boolean nothingLeft = ItemPickupHandler.pickUpStack(copy, player.getInventory());
        if (nothingLeft)
            return ItemStack.EMPTY;
        else
            return copy;
    }
}
