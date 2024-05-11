package net.natte.bankstorage;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.BoolArgumentType;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
// import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.events.KeyBindUpdateEvents;
import net.natte.bankstorage.events.MouseEvents;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.network.ItemStackBobbingAnimationPacketReceiver;
import net.natte.bankstorage.network.RequestBankStoragePacketReceiver;
import net.natte.bankstorage.network.SyncedRandomPacketReceiver;
import net.natte.bankstorage.network.screensync.SyncLargeSlotInventoryS2C;
import net.natte.bankstorage.network.screensync.SyncLargeSlotS2C;
import net.natte.bankstorage.network.screensync.SyncLockedSlotsReceiver;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.screensync.SyncContainerPacketS2C;
import net.natte.bankstorage.packet.screensync.SyncLargeSlotPacketS2C;
import net.natte.bankstorage.packet.server.KeyBindUpdatePacketC2S;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.OpenBankFromKeyBindPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.packet.server.UpdateBankOptionsPacketC2S;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;
import net.natte.bankstorage.tooltip.BankTooltipComponent;
import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class BankStorageClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

	public static KeyBinding toggleBuildModeKeyBinding;
	public static KeyBinding togglePickupModeKeyBinding;
	public static KeyBinding lockSlotKeyBinding;
	public static KeyBinding openBankFromKeyBinding;

	public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
		Util.onToggleBuildMode = MouseEvents::onToggleBuildMode;
		CachedBankStorage.setCacheUpdater(uuid -> CachedBankStorage.bankRequestQueue.add(uuid));
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerKeyBinds();
		registerKeyBindListeners();
		registerModelPredicates();
		registerRenderers();
		registerPackets();
		registerNetworkListeners();
		registerTickEvents();
		registerEventListeners();
		registerCommands();
		registerItemColors();

	}

	private void registerItemColors() {

		for (BankType type : BankStorage.bankTypes) {
			ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
				// return ((DyeableItem) stack.getItem()).getColor(stack);
				return DyedColorComponent.getColor(stack, 0);//((DyeableItem) stack.getItem()).getColor(stack);
			}, new ItemConvertible[] { type.item });
		}

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			return DyedColorComponent.getColor(stack, 0);
			// return ((DyeableItem) stack.getItem()).getColor(stack);
		}, new ItemConvertible[] { BankStorage.LINK_ITEM });
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					literal("bankstorageclient")
							.then(literal("debug")
									.then(argument("debug", BoolArgumentType.bool())
											.executes(context -> {
												Util.isDebugMode = BoolArgumentType.getBool(context, "debug");
												return 1;
											}))));
		});
	}

	private void registerEventListeners() {

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			KeyBindUpdateEvents.onKeyBindChange();
		});

		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof BankTooltipData bankTooltipData) {
				return new BankTooltipComponent(bankTooltipData.items());
			}
			return null;
		});

	}

	private void registerModelPredicates() {
		ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("linked_bank"),
				(itemStack, clientWorld, livingEntity, i) -> {
					return Float.valueOf(LinkItem.getTypeName(itemStack).split("_")[1]) / 10;
				});
		for (BankType type : BankStorage.bankTypes) {
			ModelPredicateProviderRegistry.register(type.item, new Identifier("has_color"),
					(itemStack, clientWorld, livingEntity, i) -> {
						return itemStack.contains(DataComponentTypes.DYED_COLOR) ? 1.0f : 0.0f;
					});
		}
		ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("has_color"),
				(itemStack, clientWorld, livingEntity, i) -> {
					// return ((DyeableItem) BankStorage.LINK_ITEM).hasColor(itemStack) ? 1.0f : 0.0f;
					return itemStack.contains(DataComponentTypes.DYED_COLOR) ? 1.0f : 0.0f;
				});

	}

	private void registerRenderers() {

		HudRenderCallback.EVENT.register(buildModePreviewRenderer::render);

		BlockEntityRendererRegistryImpl.register(BankStorage.BANK_DOCK_BLOCK_ENTITY, BankDockBlockEntityRenderer::new);
		BlockEntityRendererRegistryImpl.register(BankStorage.BANK_DOCK_BLOCK_ENTITY, BankDockBlockEntityRenderer::new);
	}

	private void registerTickEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(buildModePreviewRenderer::onEndTick);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null)
				return;
			for (UUID uuid : CachedBankStorage.bankRequestQueue) {
				short cachedRevision = 0;
				CachedBankStorage storage = CachedBankStorage.getBankStorage(uuid);
				if (storage != null)
					cachedRevision = storage.revision;
				ClientPlayNetworking.send(new RequestBankStoragePacketC2S(uuid, cachedRevision));
			}
			CachedBankStorage.bankRequestQueue.clear();
		});

	}

	private void registerHandledScreens() {
		for (BankType bankType : BankStorage.bankTypes) {
			HandledScreens.register(bankType.getScreenHandlerType(), BankScreen.fromType(bankType));
		}
	}

	public void registerKeyBinds() {
		toggleBuildModeKeyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.bankstorage.togglebuildmode", GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage"));
		togglePickupModeKeyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.bankstorage.togglepickupmode", GLFW.GLFW_KEY_P, "category.bankstorage"));
		lockSlotKeyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.bankstorage.lockslot", GLFW.GLFW_KEY_LEFT_ALT, "category.bankstorage"));
		openBankFromKeyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.bankstorage.openbankfromkeybind", GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage"));
	}

	public void registerKeyBindListeners() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			while (toggleBuildModeKeyBinding.wasPressed()) {
				Util.onToggleBuildMode.accept(client.player);
			}

			while (togglePickupModeKeyBinding.wasPressed()) {
				ClientPlayNetworking.send(new PickupModePacketC2S());
			}

			while (openBankFromKeyBinding.wasPressed()) {
				ClientPlayNetworking.send(new OpenBankFromKeyBindPacketC2S());
			}
		});
	}

	private void registerPackets() {
		
		PayloadTypeRegistry.playS2C().register(ItemStackBobbingAnimationPacketS2C.PACKET_ID, ItemStackBobbingAnimationPacketS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RequestBankStoragePacketS2C.PACKET_ID, RequestBankStoragePacketS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncedRandomPacketS2C.PACKET_ID, SyncedRandomPacketS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncLargeSlotPacketS2C.PACKET_ID, SyncLargeSlotPacketS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SyncContainerPacketS2C.PACKET_ID, SyncContainerPacketS2C.PACKET_CODEC);

		PayloadTypeRegistry.playC2S().register(UpdateBankOptionsPacketC2S.PACKET_ID, UpdateBankOptionsPacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(OpenBankFromKeyBindPacketC2S.PACKET_ID, OpenBankFromKeyBindPacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(RequestBankStoragePacketC2S.PACKET_ID, RequestBankStoragePacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(SortPacketC2S.PACKET_ID, SortPacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(PickupModePacketC2S.PACKET_ID, PickupModePacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(SelectedSlotPacketC2S.PACKET_ID, SelectedSlotPacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(LockSlotPacketC2S.PACKET_ID, LockSlotPacketC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(KeyBindUpdatePacketC2S.PACKET_ID, KeyBindUpdatePacketC2S.PACKET_CODEC);

	}

	public void registerNetworkListeners() {
		// ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationPacketS2C.TYPE,
				// new ItemStackBobbingAnimationPacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationPacketS2C.PACKET_ID,
				new ItemStackBobbingAnimationPacketReceiver());

		ClientPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketS2C.PACKET_ID,
				new RequestBankStoragePacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(SyncedRandomPacketS2C.PACKET_ID,
				new SyncedRandomPacketReceiver());

		// ClientPlayNetworking.registerGlobalReceiver(BankSyncPacketHandler.sync_slot, new SyncLargeSlotS2C());
		ClientPlayNetworking.registerGlobalReceiver(SyncLargeSlotPacketS2C.PACKET_ID, new SyncLargeSlotS2C());
		ClientPlayNetworking.registerGlobalReceiver(SyncContainerPacketS2C.PACKET_ID,
				new SyncLargeSlotInventoryS2C());
		ClientPlayNetworking.registerGlobalReceiver(LockedSlotsPacketS2C.PACKET_ID, new SyncLockedSlotsReceiver());
	}
}
