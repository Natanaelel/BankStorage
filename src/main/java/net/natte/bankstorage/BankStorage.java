package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.item.BankItem;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;

public class BankStorage implements ModInitializer {

	public static final String MOD_ID = "bankstorage";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BankType BANK_1 = new BankType("bank_1",        4, 1, 9, 0, 34, 0, 0, 176, 114 + 18 * 1);
	public static final BankType BANK_2 = new BankType("bank_2",       16, 2, 9, 0, 34, 0, 0, 176, 114 + 18 * 2);
	public static final BankType BANK_3 = new BankType("bank_3",       64, 3, 9, 0, 34, 0, 0, 176, 114 + 18 * 3);
	public static final BankType BANK_4 = new BankType("bank_4",      256, 4, 9, 0, 34, 0, 0, 176, 114 + 18 * 4);
	public static final BankType BANK_5 = new BankType("bank_5",     1024, 5, 9, 0, 34, 0, 0, 176, 114 + 18 * 5);
	public static final BankType BANK_6 = new BankType("bank_6",     4096, 6, 9, 0, 34, 0, 0, 176, 114 + 18 * 6);
	public static final BankType BANK_7 = new BankType("bank_7", 15625000, 7, 9, 0, 34, 0, 0, 176, 114 + 18 * 7);

	public static final List<BankType> bankTypes = new ArrayList<>();

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		BANK_1.register(bankTypes);
		BANK_2.register(bankTypes);
		BANK_3.register(bankTypes);
		BANK_4.register(bankTypes);
		BANK_5.register(bankTypes);
		BANK_6.register(bankTypes);
		BANK_7.register(bankTypes);


		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("bankdata").executes(context -> {
				ServerPlayerEntity player = context.getSource().getPlayer();
				ItemStack item = player.getStackInHand(player.getActiveHand());

				if(item.getItem() instanceof BankItem bankItem){
					BankItemStorage bankItemStorage = bankItem.getBankItemStorage(item, context.getSource().getWorld());
					player.sendMessage(Text.literal(bankItemStorage.saveToNbt().asString()));
				}

				return Command.SINGLE_SUCCESS;
			}));
		});
	}

	public static BankType getBankTypeFromName(String name) {
		for (BankType bankType : bankTypes) {
			if (bankType.getName().equals(name)) {
				return bankType;
			}
		}

		throw new Error("Cannot get BankType of name '" + name + "'");
	}

}