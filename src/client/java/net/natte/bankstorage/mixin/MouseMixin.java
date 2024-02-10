package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import net.natte.bankstorage.events.MouseEvents;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {

    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    public boolean onScroll(PlayerInventory playerInventory, double scroll) {
        boolean bypassesVanilla = MouseEvents.onScroll(playerInventory, scroll);
        return !bypassesVanilla;
    }
}
