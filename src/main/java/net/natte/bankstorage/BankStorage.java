package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.block.BankDockBlock;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.network.BuildOptionPacket;
import net.natte.bankstorage.network.OptionPacket;
import net.natte.bankstorage.network.RequestBankStorage;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.recipe.BankRecipeSerializer;
import net.natte.bankstorage.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

public class BankStorage implements ModInitializer {

	public static final String MOD_ID = "bankstorage";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final BankType BANK_1 = new BankType("bank_1", 4, 1, 9, 176, 114 + 18 * 1);
	public static final BankType BANK_2 = new BankType("bank_2", 16, 2, 9, 176, 114 + 18 * 2);
	public static final BankType BANK_3 = new BankType("bank_3", 64, 3, 9, 176, 114 + 18 * 3);
	public static final BankType BANK_4 = new BankType("bank_4", 256, 4, 9, 176, 114 + 18 * 4);
	public static final BankType BANK_5 = new BankType("bank_5", 1024, 5, 9, 176, 114 + 18 * 5);
	public static final BankType BANK_6 = new BankType("bank_6", 4096, 6, 9, 176, 114 + 18 * 6);
	public static final BankType BANK_7 = new BankType("bank_7", 15625000, 7, 9, 176, 114 + 18 * 7);

	public static final List<BankType> bankTypes = new ArrayList<>();

	public static final Block BANK_DOCK_BLOCK = new BankDockBlock(FabricBlockSettings.create().strength(5.0f, 6.0f)
			.mapColor(MapColor.BLACK).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque());

	public static final BankRecipeSerializer bankRecipeSerializer = new BankRecipeSerializer();

	public static BlockEntityType<BankDockBlockEntity> BANK_DOCK_BLOCK_ENTITY;

	@Override
	public void onInitialize() {

		registerBanks();

		registerDock();

		registerRecipes();

		registerCommands();

		registerNetworkListeners();
	}

	private void registerBanks() {

		BANK_1.register(bankTypes);
		BANK_2.register(bankTypes);
		BANK_3.register(bankTypes);
		BANK_4.register(bankTypes);
		BANK_5.register(bankTypes);
		BANK_6.register(bankTypes);
		BANK_7.register(bankTypes);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SEARCH).register(group -> {
			bankTypes.forEach(type -> {
				group.add(type.item);
			});
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
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, "bank_upgrade"), bankRecipeSerializer);

	}

	public static BankType getBankTypeFromName(String name) {
		for (BankType bankType : bankTypes) {
			if (bankType.getName().equals(name)) {
				return bankType;
			}
		}

		throw new Error("Cannot get BankType of name '" + name + "'");
	}

	public void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("bankdata").executes(context -> {
				ServerPlayerEntity player = context.getSource().getPlayer();
				ItemStack item = player.getStackInHand(player.getActiveHand());

				if (item.getItem() instanceof BankItem bankItem) {
					BankItemStorage bankItemStorage = BankItem.getBankItemStorage(item, context.getSource().getWorld());
					player.sendMessage(Text.literal(bankItemStorage.saveToNbt().asString()));
				}

				return Command.SINGLE_SUCCESS;
			}));

			BiFunction<CommandContext<ServerCommandSource>, PickupMode, Integer> setPickupMode = (context,
					pickupMode) -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = BankItem.getBankItemStorage(
						context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND),
						world);
				bankItemStorage.options.pickupMode = pickupMode;
				return Command.SINGLE_SUCCESS;
			};

			BiFunction<CommandContext<ServerCommandSource>, BuildMode, Integer> setBuidMode = (context,
					buildMode) -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = BankItem.getBankItemStorage(
						context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND),
						world);
				bankItemStorage.options.buildMode = buildMode;
				return Command.SINGLE_SUCCESS;
			};

			Command<ServerCommandSource> logOptions = context -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = BankItem.getBankItemStorage(
						context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND),
						world);
				context.getSource().sendMessage(
						Text.literal(bankItemStorage.options.pickupMode + " " + bankItemStorage.options.buildMode));
				return Command.SINGLE_SUCCESS;
			};

			dispatcher.register(
					CommandManager.literal("bankoption")
							.then(CommandManager.literal("pickup")
									.then(CommandManager.literal("none")
											.executes(context -> setPickupMode.apply(context, PickupMode.NONE)))
									.then(CommandManager.literal("all")
											.executes(context -> setPickupMode.apply(context, PickupMode.ALL)))
									.then(CommandManager.literal("filtered")
											.executes(context -> setPickupMode.apply(context, PickupMode.FILTERED)))
									.then(CommandManager.literal("void")
											.executes(context -> setPickupMode.apply(context, PickupMode.VOID))))

							.then(CommandManager.literal("build")
									.then(CommandManager.literal("none")
											.executes(context -> setBuidMode.apply(context, BuildMode.NONE)))
									.then(CommandManager.literal("normal")
											.executes(context -> setBuidMode.apply(context, BuildMode.NORMAL)))
									.then(CommandManager.literal("random")
											.executes(context -> setBuidMode.apply(context, BuildMode.RANDOM))))

							.then(CommandManager.literal("get").executes(logOptions)));
		});
	}

	public void registerNetworkListeners() {

		ServerPlayNetworking.registerGlobalReceiver(BuildOptionPacket.C2S_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					ItemStack stackInHand = player.getStackInHand(player.getActiveHand());
					if (Util.isBank(stackInHand)) {
						BankItemStorage bankItemStorage = BankItem.getBankItemStorage(stackInHand, player.getWorld());
						bankItemStorage.options.buildMode = BuildMode
								.from((bankItemStorage.options.buildMode.number + 1) % 3);
						player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
								+ bankItemStorage.options.buildMode.toString().toLowerCase()), true);

						PacketByteBuf packet = PacketByteBufs.create();
						packet.writeUuid(Util.getUUID(stackInHand));
						bankItemStorage.options.writeToPacketByteBuf(packet);

						responseSender.sendPacket(OptionPacket.S2C_PACKET_ID, packet);
					}
				});

		ServerPlayNetworking.registerGlobalReceiver(RequestBankStorage.C2S_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {

					ItemStack itemStack = buf.readItemStack();

					BankItemStorage bankItemStorage = BankItem.getBankItemStorage(itemStack, player.getWorld());
					List<ItemStack> items = bankItemStorage.getUniqueItems();
					PacketByteBuf packet = RequestBankStorage.createPacketS2C(items, bankItemStorage.selectedItemSlot,
							Util.getUUID(itemStack), bankItemStorage.options);

					responseSender.sendPacket(RequestBankStorage.S2C_PACKET_ID, packet);

				});
	}

}