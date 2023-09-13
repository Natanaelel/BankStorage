package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.screen.BankScreenHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStorage implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("bankstorage");

	public static final String MOD_ID = "bankstorage";

	public static final Item BANK_ITEM = new BankItem(new FabricItemSettings());


	public static final ScreenHandlerType<BankScreenHandler> BANK_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(BankScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
   

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "bank"), BANK_ITEM);


		Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "bank"), BANK_SCREEN_HANDLER_TYPE);
		
	}

}