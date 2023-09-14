package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStorage implements ModInitializer {

	public static final String MOD_ID = "bankstorage";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BankType BANK_1 = new BankType("bank_1", (int)Math.pow(2, 2), 1, 9, 0, 34, 0, 0, 176, 114 + 18 * 1);
	public static final BankType BANK_2 = new BankType("bank_2", (int)Math.pow(2, 4), 2, 9, 0, 34, 0, 0, 176, 114 + 18 * 2);
	public static final BankType BANK_3 = new BankType("bank_3", (int)Math.pow(2, 6), 3, 9, 0, 34, 0, 0, 176, 114 + 18 * 3);
	public static final BankType BANK_4 = new BankType("bank_4", (int)Math.pow(2, 8), 4, 9, 0, 34, 0, 0, 176, 114 + 18 * 4);
	public static final BankType BANK_5 = new BankType("bank_5", (int)Math.pow(2,10), 5, 9, 0, 34, 0, 0, 176, 114 + 18 * 5);
	public static final BankType BANK_6 = new BankType("bank_6", (int)Math.pow(2,12), 6, 9, 0, 34, 0, 0, 176, 114 + 18 * 6);
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