package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.access.SyncedRandomAccess;
import net.natte.bankstorage.block.BankDockBlock;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.command.RestoreBankCommands;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.recipe.BankLinkRecipe;
import net.natte.bankstorage.recipe.BankRecipe;
import net.natte.bankstorage.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStorage implements ModInitializer {

	public static final String MOD_ID = "bankstorage";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final BankType BANK_1 = new BankType("bank_1", 256, 1, 9, 176, 114 + 18 * 1);
	private static final BankType BANK_2 = new BankType("bank_2", 1024, 2, 9, 176, 114 + 18 * 2);
	private static final BankType BANK_3 = new BankType("bank_3", 4096, 3, 9, 176, 114 + 18 * 3);
	private static final BankType BANK_4 = new BankType("bank_4", 16_384, 4, 9, 176, 114 + 18 * 4);
	private static final BankType BANK_5 = new BankType("bank_5", 65_536, 6, 9, 176, 114 + 18 * 6);
	private static final BankType BANK_6 = new BankType("bank_6", 262_144, 9, 9, 176, 114 + 18 * 9);
	private static final BankType BANK_7 = new BankType("bank_7", 1_000_000_000, 12, 9, 176, 114 + 18 * 12);

	public static final LinkItem LINK_ITEM = new LinkItem(new FabricItemSettings().maxCount(1));

	public static final List<BankType> bankTypes = new ArrayList<>();

	public static final Block BANK_DOCK_BLOCK = new BankDockBlock(FabricBlockSettings.create().strength(5.0f, 6.0f)
			.mapColor(MapColor.BLACK).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque());

	public static BlockEntityType<BankDockBlockEntity> BANK_DOCK_BLOCK_ENTITY;

	@Override
	public void onInitialize() {

		registerBanks();
		registerDock();
		registerLink();
		registerRecipes();
		registerCommands();
		registerNetworkListeners();

		registerEventListeners();

	}

	private void registerEventListeners() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			long randomSeed = handler.player.getRandom().nextLong();
			((SyncedRandomAccess) handler.player).bankstorage$setSyncedRandom(new Random(randomSeed));
			sender.sendPacket(new SyncedRandomPacketS2C(randomSeed));
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			Random random = ((SyncedRandomAccess)oldPlayer).bankstorage$getSyncedRandom();
			((SyncedRandomAccess)newPlayer).bankstorage$setSyncedRandom(random);
		});

	}

	private void registerLink() {
		Registry.register(Registries.ITEM, Util.ID("bank_link"), LINK_ITEM);
	}

	private void registerBanks() {

		BANK_1.register(bankTypes);
		BANK_2.register(bankTypes);
		BANK_3.register(bankTypes);
		BANK_4.register(bankTypes);
		BANK_5.register(bankTypes);
		BANK_6.register(bankTypes);
		BANK_7.register(bankTypes, new FabricItemSettings().fireproof());

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(group -> {
			bankTypes.forEach(type -> {
				group.add(type.item);
			});
			group.add(LINK_ITEM);
			group.add(BANK_DOCK_BLOCK);
		});
	}

	private void registerDock() {

		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "bank_dock"), BANK_DOCK_BLOCK);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "bank_dock"),
				new BlockItem(BANK_DOCK_BLOCK, new FabricItemSettings()));

		BANK_DOCK_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				new Identifier(MOD_ID, "bank_dock_block_entity"),
				FabricBlockEntityTypeBuilder.create(BankDockBlockEntity::new, BANK_DOCK_BLOCK).build());

		ItemStorage.SIDED.registerForBlockEntity(
				(bankDockBlockEntity, direction) -> bankDockBlockEntity.getItemStorage(), BANK_DOCK_BLOCK_ENTITY);

	}

	private void registerRecipes() {
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "bank_upgrade"),
				new BankRecipe.Serializer());
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "bank_link"),
				new BankLinkRecipe.Serializer());

	}

	public void registerCommands() {
		RestoreBankCommands.register();

	}

	public void registerNetworkListeners() {

		ServerPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketC2S.TYPE,
				new RequestBankStoragePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(SortPacketC2S.TYPE, new SortPacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(PickupModePacketC2S.TYPE, new PickupModePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(SelectedSlotPacketC2S.TYPE, new SelectedSlotPacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(LockSlotPacketC2S.TYPE, new LockSlotPacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(KeyBindUpdatePacketC2S.TYPE, new KeyBindUpdatePacketC2S.Receiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateBankOptionsPacketC2S.TYPE, new UpdateBankOptionsPacketC2S.Receiver());

	}

}
