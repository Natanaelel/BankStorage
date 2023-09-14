package net.natte.bankstorage;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.natte.bankstorage.screen.BankScreen;

public class BankStorageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		for(BankType bankType : BankStorage.bankTypes){

			HandledScreens.register(bankType.getScreenHandlerType(), BankScreen.fromType(bankType));
		}

	}
}