package net.natte.bankstorage;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.events.KeyBindUpdateEvents;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.item.LinkItem;
import net.natte.bankstorage.network.ItemStackBobbingAnimationPacketReceiver;
import net.natte.bankstorage.network.RequestBankStoragePacketReceiver;
import net.natte.bankstorage.network.SyncedRandomPacketReceiver;
import net.natte.bankstorage.network.screensync.SyncLargeSlotInventoryS2C;
import net.natte.bankstorage.network.screensync.SyncLargeSlotS2C;
import net.natte.bankstorage.network.screensync.SyncLockedSlotsReceiver;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.packet.screensync.BankSyncPacketHandler;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.server.BuildModePacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;

import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class BankStorageClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

	public static KeyBinding toggleBuildModeKeyBinding;
	public static KeyBinding togglePickupModeKeyBinding;
	public static KeyBinding lockSlotKeyBinding;

	public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
		CachedBankStorage.setCacheUpdater(uuid -> CachedBankStorage.bankRequestQueue.add(uuid));
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerKeyBinds();
		registerKeyBindListeners();
		registerModelPredicates();
		registerRenderers();
		registerNetworkListeners();
		registerTickEvents();
		registerEventListeners();

	}

	private void registerEventListeners() {
		ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
			KeyBindUpdateEvents.onKeyBindChange();
		}));

	}

	private void registerModelPredicates() {
		ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("linked_bank"),
				(itemStack, clientWorld, livingEntity, i) -> {
					return Float.valueOf(LinkItem.getTypeName(itemStack).split("_")[1]) / 10;
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
				ClientPlayNetworking.send(new RequestBankStoragePacketC2S(uuid));
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
	}

	public void registerKeyBindListeners() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			while (toggleBuildModeKeyBinding.wasPressed()) {
				ItemStack stack = client.player.getStackInHand(client.player.getActiveHand());
				if (Util.isBankLike(stack)) {
					if (Util.changeBuildMode(stack)) {
						BuildMode buildMode = Util.getOptions(stack).buildMode;

						client.player.sendMessage(Text.translatable("popup.bankstorage.buildmode."
								+ Util.getOptions(stack).buildMode.toString().toLowerCase()), true);

						ClientPlayNetworking.send(new BuildModePacketC2S(buildMode));
					}
				}
			}

			while (togglePickupModeKeyBinding.wasPressed()) {
				ItemStack stack = client.player.getStackInHand(client.player.getActiveHand());
				if (Util.isBankLike(stack)) {
					ClientPlayNetworking.send(new PickupModePacketC2S());
				}
			}
		});
	}

	public void registerNetworkListeners() {
		ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationPacketS2C.TYPE,
				new ItemStackBobbingAnimationPacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketS2C.TYPE,
				new RequestBankStoragePacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(SyncedRandomPacketS2C.TYPE,
				new SyncedRandomPacketReceiver());

		ClientPlayNetworking.registerGlobalReceiver(BankSyncPacketHandler.sync_slot, new SyncLargeSlotS2C());
		ClientPlayNetworking.registerGlobalReceiver(BankSyncPacketHandler.sync_container,
				new SyncLargeSlotInventoryS2C());
		ClientPlayNetworking.registerGlobalReceiver(LockedSlotsPacketS2C.TYPE, new SyncLockedSlotsReceiver());
	}
}
