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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.block.BankDockBlock;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankFunctionality;
import net.natte.bankstorage.item.BankItem;
import net.natte.bankstorage.network.OptionPackets;
import net.natte.bankstorage.network.PickupModePacket;
import net.natte.bankstorage.network.SortPacket;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.packet.BuildOptionPacketC2S;
import net.natte.bankstorage.packet.RequestBankStoragePacketC2S;
import net.natte.bankstorage.recipe.BankRecipeSerializer;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
	public static final BankType BANK_5 = new BankType("bank_5", 1024, 6, 9, 176, 114 + 18 * 6);
	public static final BankType BANK_6 = new BankType("bank_6", 4096, 9, 9, 176, 114 + 18 * 9);
	public static final BankType BANK_7 = new BankType("bank_7", 15625000, 12, 9, 176, 114 + 18 * 12);

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
		BANK_7.register(bankTypes, new FabricItemSettings().fireproof());

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(group -> {
			bankTypes.forEach(type -> {
				group.add(type.item);
			});
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
					BankItemStorage bankItemStorage = Util.getBankItemStorage(item, context.getSource().getWorld());
					player.sendMessage(Text.literal(bankItemStorage.saveToNbt().asString()));
				}

				return Command.SINGLE_SUCCESS;
			}));

			BiFunction<CommandContext<ServerCommandSource>, PickupMode, Integer> setPickupMode = (context,
					pickupMode) -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = Util.getBankItemStorage(
						context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND),
						world);
				bankItemStorage.options.pickupMode = pickupMode;
				return Command.SINGLE_SUCCESS;
			};

			BiFunction<CommandContext<ServerCommandSource>, BuildMode, Integer> setBuidMode = (context,
					buildMode) -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = Util.getBankItemStorage(
						context.getSource().getPlayer().getStackInHand(Hand.MAIN_HAND),
						world);
				bankItemStorage.options.buildMode = buildMode;
				return Command.SINGLE_SUCCESS;
			};

			Command<ServerCommandSource> logOptions = context -> {
				ServerWorld world = context.getSource().getWorld();
				BankItemStorage bankItemStorage = Util.getBankItemStorage(
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

			dispatcher.register(
					CommandManager.literal("bankstorage")
							.then(CommandManager.literal("list")
									.requires(context -> context.hasPermissionLevel(2))
									.executes(context -> {

										MutableText message = Text.empty();
										MinecraftServer server = context.getSource().getServer();
										BankStateSaverAndLoader.getServerState(server).bankMap
												.forEach((uuid, bankItemStorage) -> {
													String command = "/bankstorage fromuuid " + uuid.toString() + " "
															+ context.getSource().getPlayer().getEntityName();
													message.append(Text.literal(bankItemStorage.type.getName() + " "
															+ bankItemStorage.stacks.stream()
																	.filter(stack -> !stack.isEmpty()).count()
															+ " items " + bankItemStorage.uuid.toString() + "\n")
															.styled(style -> style.withClickEvent(new ClickEvent(
																	ClickEvent.Action.SUGGEST_COMMAND, command))
																	.withHoverEvent(new HoverEvent(
																			HoverEvent.Action.SHOW_TEXT,
																			Text.literal("Click for give command")))));
												});
										context.getSource().sendMessage(message);
										return Command.SINGLE_SUCCESS;
									}))
							.then(CommandManager.literal("fromuuid")
									.requires(context -> context.hasPermissionLevel(2))
									.then(CommandManager.argument("uuid", UuidArgumentType.uuid())
											.then(CommandManager.argument("player", EntityArgumentType.player())
											.executes(context -> {
												ServerPlayerEntity player = EntityArgumentType.getPlayer(context,
														"player");
												UUID uuid = UuidArgumentType.getUuid(context, "uuid");
												BankItemStorage bank = Util.getBankItemStorage(uuid,
														context.getSource().getWorld());
												if (bank == null) {
													context.getSource().sendFeedback(
															() -> Text.literal("No bank with that id"),
															false);
													return 0;
												}
												ItemStack stack = Registries.ITEM
														.get(new Identifier(MOD_ID, bank.type.getName()))
														.getDefaultStack();
												stack.getOrCreateNbt().putUuid(BankFunctionality.UUID_KEY, uuid);
												player.getInventory().insertStack(stack);
												return Command.SINGLE_SUCCESS;
											})))));
		});
	}

	public void registerNetworkListeners() {


		ServerPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketC2S.TYPE, new RequestBankStoragePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(BuildOptionPacketC2S.TYPE, new BuildOptionPacketC2S.Receiver());


		ServerPlayNetworking.registerGlobalReceiver(PickupModePacket.C2S_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					server.execute(() -> {

						onChangePickupMode(player);

					});
				});


		ServerPlayNetworking.registerGlobalReceiver(OptionPackets.SCROLL_C2S_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					UUID uuid = buf.readUuid();
					double scroll = buf.readDouble();
					server.execute(() -> {

						BankItemStorage bankItemStorage = Util.getBankItemStorage(uuid, player.getWorld());
						int selectedItemSlot = bankItemStorage.options.selectedItemSlot;
						selectedItemSlot -= (int) Math.signum(scroll);
						int size = bankItemStorage.getBlockItems().size();
						bankItemStorage.options.selectedItemSlot = size == 0 ? 0
								: Math.min(Math.max(selectedItemSlot, 0), size - 1);

						OptionPackets.sendOptions(player, bankItemStorage.options, uuid);
					});

				});

		ServerPlayNetworking.registerGlobalReceiver(SortPacket.C2S_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					server.execute(() -> {
						ScreenHandler screenHandler = player.currentScreenHandler;
						if (!(screenHandler instanceof BankScreenHandler bankScreenHandler))
							return;
						BankItemStorage bankItemStorage = (BankItemStorage) bankScreenHandler.inventory;

						Util.sortBank(bankItemStorage);

					});

				});
	}

	private void onChangePickupMode(ServerPlayerEntity player) {
		ItemStack stackInHand = player.getStackInHand(player.getActiveHand());
		if (Util.isBank(stackInHand)) {
			BankItemStorage bankItemStorage = Util.getBankItemStorage(stackInHand,
					player.getWorld());
			bankItemStorage.options.pickupMode = PickupMode
					.from((bankItemStorage.options.pickupMode.number + 1) % 4);
			player.sendMessage(Text.translatable("popup.bankstorage.pickupmode."
					+ bankItemStorage.options.pickupMode.toString().toLowerCase()), true);

			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeUuid(Util.getUUID(stackInHand));
			packet.writeNbt(bankItemStorage.options.asNbt());

			ServerPlayNetworking.send(player, OptionPackets.S2C_PACKET_ID, packet);
		}

	}

	public static void onChangeBuildMode(ServerPlayerEntity player) {
		ItemStack stackInHand = player.getStackInHand(player.getActiveHand());
		if (Util.isBank(stackInHand)) {
			BankItemStorage bankItemStorage = Util.getBankItemStorage(stackInHand,
					player.getWorld());
			bankItemStorage.options.buildMode = BuildMode
					.from((bankItemStorage.options.buildMode.number + 1) % 3);
			player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
					+ bankItemStorage.options.buildMode.toString().toLowerCase()), true);

			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeUuid(Util.getUUID(stackInHand));
			packet.writeNbt(bankItemStorage.options.asNbt());

			ServerPlayNetworking.send(player, OptionPackets.S2C_PACKET_ID, packet);
		}
	}

}