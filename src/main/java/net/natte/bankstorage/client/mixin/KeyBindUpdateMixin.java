package net.natte.bankstorage.client.mixin;

import net.minecraft.client.Options;
import net.natte.bankstorage.client.events.KeyBindUpdateEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class KeyBindUpdateMixin {
    @Inject(at = @At("HEAD"), method = "broadcastOptions")
    public void onOptionChange(CallbackInfo ci) {
        KeyBindUpdateEvents.onKeyBindChange();
    }
}
