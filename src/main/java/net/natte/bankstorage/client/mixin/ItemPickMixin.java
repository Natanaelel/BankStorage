package net.natte.bankstorage.client.mixin;

import net.minecraft.client.Minecraft;
import net.natte.bankstorage.client.events.PickBlockEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.MinecraftClient;
import net.natte.bankstorage.events.PickBlockEvents;

@Mixin(Minecraft.class)
public abstract class ItemPickMixin {

    @WrapWithCondition(method = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
    public boolean onItemPick(Minecraft client) {
        boolean didBankPickBlock = PickBlockEvents.pickBlock(client);
        return !didBankPickBlock;
    }
}
