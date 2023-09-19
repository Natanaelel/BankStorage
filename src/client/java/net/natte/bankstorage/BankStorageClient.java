package net.natte.bankstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.screen.BankScreen;

public class BankStorageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		
		BlockEntityRendererRegistryImpl.register(BankStorage.BANK_DOCK_BLOCK_ENTITY, BankDockBlockEntityRenderer::new);
		
		for(BankType bankType : BankStorage.bankTypes){
			HandledScreens.register(bankType.getScreenHandlerType(), BankScreen.fromType(bankType));
		}


	}
}