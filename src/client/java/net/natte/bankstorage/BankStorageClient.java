package net.natte.bankstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.DyeableItem;
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
import net.natte.bankstorage.packet.screensync.BankSyncPacketHandler;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.server.OpenBankFromKeyBindPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;
import net.natte.bankstorage.tooltip.BankTooltipComponent;
import net.natte.bankstorage.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class BankStorageClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

    public static KeyBinding toggleBuildModeKeyBinding;
    public static KeyBinding togglePickupModeKeyBinding;
    public static KeyBinding lockSlotKeyBinding;
    public static KeyBinding openBankFromKeyBinding;

    public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

    static {
        Util.isShiftDown = Screen::hasShiftDown;
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
        registerNetworkListeners();
        registerTickEvents();
        registerEventListeners();
        registerItemColors();

    }

    private void registerItemColors() {

        for (BankType type : BankStorage.BANK_TYPES) {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((DyeableItem) stack.getItem()).getColor(stack), type.item);
        }

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((DyeableItem) stack.getItem()).getColor(stack), BankStorage.LINK_ITEM);
    }

    private void registerEventListeners() {

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> KeyBindUpdateEvents.onKeyBindChange());

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof BankTooltipData bankTooltipData) {
                return new BankTooltipComponent(bankTooltipData.items());
            }
            return null;
        });

    }

    private void registerModelPredicates() {
        ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("linked_bank"),
                (itemStack, clientWorld, livingEntity, i) -> Float.parseFloat(LinkItem.getTypeName(itemStack).split("_")[1]) / 10);
        for (BankType type : BankStorage.BANK_TYPES) {
            ModelPredicateProviderRegistry.register(type.item, new Identifier("has_color"),
                    (itemStack, clientWorld, livingEntity, i) -> type.item.hasColor(itemStack) ? 1.0f : 0.0f);
        }
        ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("has_color"),
                (itemStack, clientWorld, livingEntity, i) -> BankStorage.LINK_ITEM.hasColor(itemStack) ? 1.0f : 0.0f);

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
        for (BankType bankType : BankStorage.BANK_TYPES) {
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
