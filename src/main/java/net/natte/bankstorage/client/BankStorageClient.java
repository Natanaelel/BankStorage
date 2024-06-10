package net.natte.bankstorage.client;

import java.util.UUID;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.rendering.BuildModePreviewRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
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
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyMapping;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.events.KeyBindUpdateEvents;
import net.natte.bankstorage.events.MouseEvents;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.network.ItemStackBobbingAnimationPacketReceiver;
import net.natte.bankstorage.network.RequestBankStoragePacketReceiver;
import net.natte.bankstorage.network.SyncedRandomPacketReceiver;
import net.natte.bankstorage.network.screensync.SyncLockedSlotsReceiver;
import net.natte.bankstorage.packet.client.ItemStackBobbingAnimationPacketS2C;
import net.natte.bankstorage.packet.client.RequestBankStoragePacketS2C;
import net.natte.bankstorage.packet.client.SyncedRandomPacketS2C;
import net.natte.bankstorage.packet.screensync.LockedSlotsPacketS2C;
import net.natte.bankstorage.packet.server.OpenBankFromKeyBindPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.screen.BankScreen;
import net.natte.bankstorage.tooltip.BankTooltipComponent;
import net.natte.bankstorage.util.Util;

//@Environment(EnvType.CLIENT)
@Mod(value = BankStorage.MOD_ID, dist = Dist.CLIENT)
public class BankStorageClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(BankStorage.MOD_ID);

    public static KeyMapping toggleBuildModeKeyBinding;
    public static KeyMapping togglePickupModeKeyBinding;
    public static KeyMapping lockSlotKeyBinding;
    public static KeyMapping openBankFromKeyBinding;

    public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

    static {
        Util.isShiftDown = Screen::hasShiftDown;
        Util.onToggleBuildMode = MouseEvents::onToggleBuildMode;
        CachedBankStorage.setCacheUpdater(CachedBankStorage.bankRequestQueue::add);
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
        registerCommands();
        registerItemColors();

    }

    private void registerItemColors() {

        for (BankType type : BankStorage.bankTypes) {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                return DyedColorComponent.getColor(stack, 0);
            }, new ItemConvertible[] { type.item });
        }

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return DyedColorComponent.getColor(stack, 0);
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
        for (BankType type : BankStorage.bankTypes) {
            ModelPredicateProviderRegistry.register(type.item, new Identifier("has_color"),
                    (itemStack, clientWorld, livingEntity, i) -> {
                        return itemStack.contains(DataComponentTypes.DYED_COLOR) ? 1.0f : 0.0f;
                    });
        }
        ModelPredicateProviderRegistry.register(BankStorage.LINK_ITEM, new Identifier("has_color"),
                (itemStack, clientWorld, livingEntity, i) -> {
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
                new KeyMapping("key.bankstorage.togglebuildmode", GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage"));
        togglePickupModeKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.bankstorage.togglepickupmode", GLFW.GLFW_KEY_P, "category.bankstorage"));
        lockSlotKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.bankstorage.lockslot", GLFW.GLFW_KEY_LEFT_ALT, "category.bankstorage"));
        openBankFromKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.bankstorage.openbankfromkeybind", GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage"));
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

        ClientPlayNetworking.registerGlobalReceiver(ItemStackBobbingAnimationPacketS2C.PACKET_ID,
                new ItemStackBobbingAnimationPacketReceiver());
        ClientPlayNetworking.registerGlobalReceiver(RequestBankStoragePacketS2C.PACKET_ID,
                new RequestBankStoragePacketReceiver());
        ClientPlayNetworking.registerGlobalReceiver(SyncedRandomPacketS2C.PACKET_ID,
                new SyncedRandomPacketReceiver());

        ClientPlayNetworking.registerGlobalReceiver(LockedSlotsPacketS2C.PACKET_ID, new SyncLockedSlotsReceiver());
    }
}
