package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;
import net.natte.bankstorage.events.KeyBindUpdateEvents;

@Mixin(GameOptions.class)
public abstract class KeyBindUpdateMixin {
    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V")
    public void onKeyBindChange(KeyBinding keyBinding, Key key, CallbackInfo ci) {
        KeyBindUpdateEvents.onKeyBindChange();
    }
}
