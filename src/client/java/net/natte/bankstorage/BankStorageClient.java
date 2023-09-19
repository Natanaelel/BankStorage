package net.natte.bankstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.screen.BankScreen;

public class BankStorageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		for(BankType bankType : BankStorage.bankTypes){

			HandledScreens.register(bankType.getScreenHandlerType(), BankScreen.fromType(bankType));
			BlockEntityRendererRegistry.register(BankStorage.BANK_DOCK_BLOCK_ENTITY, BankDockBlockEntityRenderer::new);
		}

	}
}