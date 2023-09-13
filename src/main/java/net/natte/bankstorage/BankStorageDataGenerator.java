package net.natte.bankstorage;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.natte.bankstorage.datagen.ItemModelProvider;

public class BankStorageDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		
		pack.addProvider(ItemModelProvider::new);
	}
	
	
}
