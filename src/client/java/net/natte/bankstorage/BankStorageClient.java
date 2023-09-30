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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.network.ItemStackBobbingAnimationPacketReceiver;
import net.natte.bankstorage.network.OptionPacketReceiver;
import net.natte.bankstorage.network.RequestBankStoragePacketReceiver;
import net.natte.bankstorage.network.screensync.SyncLargeSlotInventoryS2C;
import net.natte.bankstorage.network.screensync.SyncLargeSlotS2C;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.packet.client.OptionPacketS2C;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.screensync.BankSyncPacketHandler;
import net.natte.bankstorage.packet.server.BuildModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;

import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class BankStorageClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

	public static KeyBinding toggleBuildModeKeyBinding;

	public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerKeyBinds();
		registerKeyBindListeners();
		registerRenderers();
		registerNetworkListeners();
		registerTickEvents();

	}

	private void registerRenderers() {

		HudRenderCallback.EVENT.register(buildModePreviewRenderer::render);

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
				new KeyBinding(
						"key.bankstorage.togglebuildmode",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_I,
						"category.bankstorage"));
	}

	public void registerKeyBindListeners() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			while (toggleBuildModeKeyBinding.wasPressed()) {
				ItemStack stack = client.player.getStackInHand(client.player.getActiveHand());
				if (Util.isBank(stack)) {
					ClientPlayNetworking.send(new BuildModePacketC2S(stack));
				}
			}

		});
	}

	public void registerNetworkListeners() {
		ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationPacketS2C.TYPE,
				new ItemStackBobbingAnimationPacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(OptionPacketS2C.TYPE, new OptionPacketReceiver());
		ClientPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketS2C.TYPE,
				new RequestBankStoragePacketReceiver());

		ClientPlayNetworking.registerGlobalReceiver(BankSyncPacketHandler.sync_slot, new SyncLargeSlotS2C());
		ClientPlayNetworking.registerGlobalReceiver(BankSyncPacketHandler.sync_container,
				new SyncLargeSlotInventoryS2C());
	}
}
