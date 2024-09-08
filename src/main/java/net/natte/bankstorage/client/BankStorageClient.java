package net.natte.bankstorage.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.DyedItemColor;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.events.KeyBindUpdateEvents;
import net.natte.bankstorage.client.events.MouseEvents;
import net.natte.bankstorage.client.rendering.BankDockBlockEntityRenderer;
import net.natte.bankstorage.client.rendering.BuildModePreviewRenderer;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.client.tooltip.BankTooltipComponent;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.packet.server.OpenBankFromKeyBindPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.RequestBankStoragePacketC2S;
import net.natte.bankstorage.packet.server.ToggleBuildModePacketC2S;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;


@Mod(value = BankStorage.MOD_ID, dist = Dist.CLIENT)
public class BankStorageClient {


    public static final ResourceLocation WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    public static final KeyMapping toggleBuildModeKeyBinding = new KeyMapping("key.bankstorage.togglebuildmode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage");
    public static final KeyMapping togglePickupModeKeyBinding = new KeyMapping("key.bankstorage.togglepickupmode", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.bankstorage");
    public static final KeyMapping lockSlotKeyBinding = new KeyMapping("key.bankstorage.lockslot", KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.bankstorage");
    public static final KeyMapping openBankFromKeyBinding = new KeyMapping("key.bankstorage.openbankfromkeybind", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.bankstorage");

    public static BuildModePreviewRenderer buildModePreviewRenderer = new BuildModePreviewRenderer();

    static {
        Util.isShiftDown = Screen::hasShiftDown;
        CachedBankStorage.setCacheUpdater(CachedBankStorage.bankRequestQueue::add);
    }

    public BankStorageClient(IEventBus modBus) {

        registerHandledScreens(modBus);
        registerRenderers(modBus);
        registerEventListeners(modBus);


        modBus.addListener(this::registerModelPredicates);
        modBus.addListener(this::registerKeyBinds);
        modBus.addListener(this::initializeClientOnRenderThread);
        modBus.addListener(this::registerItemColors);

        NeoForge.EVENT_BUS.addListener(this::registerTickEvents);
        NeoForge.EVENT_BUS.addListener(MouseEvents::onScroll);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (BankType type : BankStorage.BANK_TYPES) {
            event.register((stack, tintIndex) -> DyedItemColor.getOrDefault(stack, 0), type.item.get());
        }
        event.register((stack, tintIndex) -> DyedItemColor.getOrDefault(stack, 0), BankStorage.BANK_LINK.get());
    }

    private void registerEventListeners(IEventBus modBus) {
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(event -> {
            KeyBindUpdateEvents.onKeyBindChange();
        });
        modBus.<RegisterClientTooltipComponentFactoriesEvent>addListener(event -> {
            event.register(BankTooltipData.class, BankTooltipComponent::of);
        });
    }

    private void registerModelPredicates(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (BankType type : BankStorage.BANK_TYPES) {
                ItemProperties.register(type.item.get(), ResourceLocation.withDefaultNamespace("has_color"), (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1 : 0);
            }
            ItemProperties.register(BankStorage.BANK_LINK.get(), ResourceLocation.withDefaultNamespace("has_color"), (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1 : 0);
        });
    }

    private void registerRenderers(IEventBus modBus) {

        NeoForge.EVENT_BUS.<RenderGuiEvent.Post>addListener(event -> buildModePreviewRenderer.render(event.getGuiGraphics()));

        modBus.<EntityRenderersEvent.RegisterRenderers>addListener(event -> {
            event.registerBlockEntityRenderer(BankStorage.BANK_DOCK_BLOCK_ENTITY.get(), BankDockBlockEntityRenderer::new);
        });
    }

    private void registerTickEvents(ClientTickEvent.Post event) {
        handleInputs();
        buildModePreviewRenderer.tick();
        sendQueuedUpdateRequests();
        CachedBankStorage.advanceThrottledQueue();
    }

    private void registerHandledScreens(IEventBus modBus) {
        modBus.<RegisterMenuScreensEvent>addListener(event -> {
            event.register(BankStorage.MENU_TYPE, BankScreen::new);
        });
    }

    private void initializeClientOnRenderThread(FMLClientSetupEvent event) {
        event.enqueueWork(() -> Util.isClient.set(true));
    }

    public void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(toggleBuildModeKeyBinding);
        event.register(togglePickupModeKeyBinding);
        event.register(lockSlotKeyBinding);
        event.register(openBankFromKeyBinding);
    }

    public void handleInputs() {
        while (toggleBuildModeKeyBinding.consumeClick())
            PacketDistributor.sendToServer(ToggleBuildModePacketC2S.INSTANCE);

        while (togglePickupModeKeyBinding.consumeClick())
            PacketDistributor.sendToServer(PickupModePacketC2S.INSTANCE);

        while (openBankFromKeyBinding.consumeClick())
            PacketDistributor.sendToServer(OpenBankFromKeyBindPacketC2S.INSTANCE);
    }

    private void sendQueuedUpdateRequests() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null)
            return;
        for (UUID uuid : CachedBankStorage.bankRequestQueue) {
            short cachedRevision = 0;
            CachedBankStorage storage = CachedBankStorage.getBankStorage(uuid);
            if (storage != null)
                cachedRevision = storage.revision;
            PacketDistributor.sendToServer(new RequestBankStoragePacketC2S(uuid, cachedRevision));
        }
        CachedBankStorage.bankRequestQueue.clear();
    }
}
