package net.natte.bankstorage.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.screen.BankScreenHandlerSyncHandler;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @WrapOperation(method = "initMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setSynchronizer(Lnet/minecraft/world/inventory/ContainerSynchronizer;)V"))
    private void customSync(AbstractContainerMenu screenHandler, ContainerSynchronizer screenHandlerSyncHandler,
                            Operation<Void> updateSyncHandler) {
        if (screenHandler instanceof BankScreenHandler bankScreenHandler) {
            BankScreenHandlerSyncHandler bankScreenHandlerSyncHandler = new BankScreenHandlerSyncHandler(
                    screenHandlerSyncHandler, (ServerPlayer) (Object) this);
            updateSyncHandler.call(screenHandler, bankScreenHandlerSyncHandler);
            bankScreenHandler.setBankScreenSync(bankScreenHandlerSyncHandler);
        } else {
            updateSyncHandler.call(screenHandler, screenHandlerSyncHandler);
        }
    }

    @WrapOperation(method = "initMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setSynchronizer(Lnet/minecraft/world/inventory/ContainerSynchronizer;)V"))
    private void setCustomSyncronizer(AbstractContainerMenu screenHandler, ContainerSynchronizer originalSynchronizer, Operation<Void> setSyncronizer) {
        if (screenHandler instanceof BankScreenHandler bankScreenHandler) {
            BankScreenHandlerSyncHandler bankScreenHandlerSyncHandler = new BankScreenHandlerSyncHandler(originalSynchronizer, (ServerPlayer) (Object) this);
            bankScreenHandler.setSynchronizer(bankScreenHandlerSyncHandler);
        } else
            setSyncronizer.call(screenHandler, originalSynchronizer);
    }
}
