package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;
import net.natte.bankstorage.events.PickBlockEvents;

@Mixin(MinecraftClient.class)
public abstract class ItemPickMixin {

    @Redirect(method = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
    public void onItemPick(MinecraftClient client) {
        if(!PickBlockEvents.pickBlock(client)){
            client.doItemPick();
        }
    }
}
