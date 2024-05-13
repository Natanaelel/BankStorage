package net.natte.bankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.screen.BankScreenHandlerSyncHandler;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @WrapOperation(method = "onScreenHandlerOpened", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;updateSyncHandler(Lnet/minecraft/screen/ScreenHandlerSyncHandler;)V"))
    private void customSync(ScreenHandler screenHandler, ScreenHandlerSyncHandler screenHandlerSyncHandler,
            Operation<Void> updateSyncHandler) {
        if (screenHandler instanceof BankScreenHandler bankScreenHandler) {
            BankScreenHandlerSyncHandler bankScreenHandlerSyncHandler = new BankScreenHandlerSyncHandler(
                    screenHandlerSyncHandler, (ServerPlayerEntity) (Object) this);
            updateSyncHandler.call(screenHandler, bankScreenHandlerSyncHandler);
            bankScreenHandler.setBankScreenSync(bankScreenHandlerSyncHandler);
        } else {
            updateSyncHandler.call(screenHandler, screenHandlerSyncHandler);
        }
    }
}
