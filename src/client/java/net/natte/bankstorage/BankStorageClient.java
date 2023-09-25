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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.network.BuildOptionPacket;
import net.natte.bankstorage.network.ItemStackBobbingAnimationS2C;
import net.natte.bankstorage.network.OptionPackets;
import net.natte.bankstorage.network.RequestBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;
import net.natte.bankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class BankStorageClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

	public static KeyBinding toggleBuildModeKeyBinding;

	public static BuildModePreviewRenderer buildModePreviewRenderer;

	@Override
	public void onInitializeClient() {

		registerDockRenderer();

		registerHandledScreens();

		registerKeyBinds();
		registerKeyBindListeners();

		buildModePreviewRenderer = BuildModePreviewRenderer.Instance;

		ClientTickEvents.END_CLIENT_TICK.register(buildModePreviewRenderer);

		HudRenderCallback.EVENT.register(buildModePreviewRenderer::render);

		registerNetworkListeners();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null)
				return;
			for (UUID uuid : CachedBankStorage.bankRequestQueue) {
				ClientPlayNetworking.send(RequestBankStorage.C2S_PACKET_ID, RequestBankStorage.createRequestC2S(uuid));
			}
			CachedBankStorage.bankRequestQueue.clear();
		});

	}

	private void registerDockRenderer() {
		BlockEntityRendererRegistryImpl.register(BankStorage.BANK_DOCK_BLOCK_ENTITY, BankDockBlockEntityRenderer::new);
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
				if (Util.isBank(client.player.getStackInHand(client.player.getActiveHand()))) {
					ClientPlayNetworking.send(BuildOptionPacket.C2S_PACKET_ID, PacketByteBufs.create());
				}
			}

		});
	}

	private void registerNetworkListeners() {

		ClientPlayNetworking.registerGlobalReceiver(OptionPackets.S2C_PACKET_ID,
				(client, handler, buf, responseSender) -> {
					UUID uuid = buf.readUuid();
					CachedBankStorage cachedBankStorage = CachedBankStorage.BANK_CACHE.get(uuid);
					cachedBankStorage.options = BankOptions.fromNbt(buf.readNbt());
				});

		ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationS2C.PACKET_ID,
				(client, handler, buf, responseSender) -> {
					int i = buf.readInt();
					PlayerInventory inventory = client.player.getInventory();
					ItemStack stack = ItemStack.EMPTY;
					if (i < 9 * 4) {
						stack = inventory.main.get(i);
					} else if (i < 9 * 4 + 1) {
						stack = inventory.offHand.get(i - 9 * 4);
					} else {
						stack = inventory.armor.get(i - 9 * 4 - 1);
					}
					stack.setBobbingAnimationTime(5);
				});

		ClientPlayNetworking.registerGlobalReceiver(RequestBankStorage.S2C_PACKET_ID,
				(client, handler, buf, responseSender) -> {
					CachedBankStorage bankStorage = RequestBankStorage.readPacketS2C(buf);
					CachedBankStorage.BANK_CACHE.put(bankStorage.uuid, bankStorage);

					if (bankStorage.uuid.equals(buildModePreviewRenderer.uuid)) {
						buildModePreviewRenderer.setBankStorage(bankStorage);
					}
				});

		ClientPlayNetworking.registerGlobalReceiver(OptionPackets.OPTIONS_S2C_PACKET_ID,
				(client, handler, buf, responseSender) -> {

					UUID uuid = buf.readUuid();
					BankOptions options = BankOptions.fromNbt(buf.readNbt());

					client.execute(() -> {
						CachedBankStorage cachedBankStorage = CachedBankStorage.BANK_CACHE.get(uuid);

						if (cachedBankStorage == null)
							return;

						cachedBankStorage.options = options;
					});

				});
	}
}