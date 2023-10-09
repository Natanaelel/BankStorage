package net.natte.bankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.block.BankDockBlock;
import net.natte.bankstorage.blockentity.BankDockBlockEntity;
import net.natte.bankstorage.container.BankItemStorage;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.BankFunctionality;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.client.OptionPacketS2C;
import net.natte.bankstorage.packet.server.BuildModePacketC2S;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.packet.server.ScrollPacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.recipe.BankRecipeSerializer;
import net.natte.bankstorage.util.Util;
import net.natte.bankstorage.world.BankStateSaverAndLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

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

	public static final List<BankType> bankTypes = new ArrayList<>();

	private static final Block BANK_DOCK_BLOCK = new BankDockBlock(FabricBlockSettings.create().strength(5.0f, 6.0f)
			.mapColor(MapColor.BLACK).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque());

	private static final BankRecipeSerializer bankRecipeSerializer = new BankRecipeSerializer();

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

	public void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

			dispatcher.register(
					literal("bankstorage")
							.then(literal("list")
									.requires(context -> context.hasPermissionLevel(2))
									.executes(BankStorage::listBankStorages))
							.then(literal("fromuuid")
									.requires(context -> context.hasPermissionLevel(2))
									.then(argument("uuid", UuidArgumentType.uuid())
											.then(argument("player", EntityArgumentType.player())
													.executes(BankStorage::restoreBankCommand)))));
		});
	}

	public void registerNetworkListeners() {

		ServerPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketC2S.TYPE,
				new RequestBankStoragePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(SortPacketC2S.TYPE, new SortPacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(BuildModePacketC2S.TYPE, new BuildModePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(PickupModePacketC2S.TYPE, new PickupModePacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(ScrollPacketC2S.TYPE, new ScrollPacketC2S.Receiver());
		ServerPlayNetworking.registerGlobalReceiver(LockSlotPacketC2S.TYPE, new LockSlotPacketC2S.Receiver());

	}

	public static void onChangeBuildMode(ServerPlayerEntity player, ItemStack bank) {
		if (Util.isBank(bank)) {
			BankItemStorage bankItemStorage = Util.getBankItemStorage(bank,
					player.getWorld());
			bankItemStorage.options.buildMode = BuildMode
					.from((bankItemStorage.options.buildMode.number + 1) % 3);
			player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
					+ bankItemStorage.options.buildMode.toString().toLowerCase()), true);

			ServerPlayNetworking.send(player,
					new OptionPacketS2C(Util.getUUID(bank), bankItemStorage.options.asNbt()));
		} else {
			LOGGER.info(player.getName().getString() + " tried to change build mode while holding " + bank);
		}
	}

	private static int listBankStorages(CommandContext<ServerCommandSource> context) {

		MutableText message = Text.empty();
		MinecraftServer server = context.getSource().getServer();
		ServerPlayerEntity player = context.getSource().getPlayer();

		Map<UUID, BankItemStorage> bankMap = BankStateSaverAndLoader.getServerStateSaverAndLoader(server).getBankMap();

		for (Map.Entry<UUID, BankItemStorage> entry : bankMap.entrySet()) {
			UUID uuid = entry.getKey();
			BankItemStorage bankItemStorage = entry.getValue();

			long nonEmptyStacks = bankItemStorage.stacks.stream().filter(stack -> !stack.isEmpty()).count();

			String command = "/bankstorage fromuuid " + uuid.toString() + " " + player.getEntityName();

			ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
			HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Text.translatable("command.bankstorage.hoverinfo"));

			message.append(Text
					.translatable("command.bankstorage.bankinfo", bankItemStorage.type.getName(), nonEmptyStacks,
							bankItemStorage.uuid.toString())
					.styled(style -> style
							.withClickEvent(clickEvent)
							.withHoverEvent(hoverEvent)));
		}
		context.getSource().sendMessage(message);
		return Command.SINGLE_SUCCESS;

	}

	private static int restoreBankCommand(CommandContext<ServerCommandSource> context) {

		ServerPlayerEntity player;
		try {
			player = EntityArgumentType.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			return 0;
		}
		UUID uuid = UuidArgumentType.getUuid(context, "uuid");
		BankItemStorage bank = Util.getBankItemStorage(uuid,
				context.getSource().getWorld());
		if (bank == null) {
			context.getSource().sendFeedback(() -> Text.translatable("command.bankstorage.unknownid"), false);
			return 0;
		}
		ItemStack stack = Registries.ITEM
				.get(Util.ID(bank.type.getName()))
				.getDefaultStack();
		stack.getOrCreateNbt().putUuid(BankFunctionality.UUID_KEY,
				uuid);
		player.getInventory().insertStack(stack);
		return Command.SINGLE_SUCCESS;
	}

}
