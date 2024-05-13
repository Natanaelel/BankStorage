package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.screen.BankScreenSync;

// TODO: is this needed anymore?
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onScreenHandlerOpened", at = @At("RETURN"))
    private void customSync(ScreenHandler screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof BankScreenHandler bankScreenHandler) {
            bankScreenHandler.setBankScreenSync(new BankScreenSync((ServerPlayerEntity) (Object) this));
        }
    }
}
