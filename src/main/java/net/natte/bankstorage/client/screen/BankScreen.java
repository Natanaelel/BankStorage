package net.natte.bankstorage.client.screen;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.client.rendering.ItemCountUtils;
import net.natte.bankstorage.screen.BankScreenHandler;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.util.Util;

public class BankScreen extends AbstractContainerScreen<BankScreenHandler> {

    private static final ResourceLocation WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private BankType type;
    private ResourceLocation texture;

    private SortMode sortMode;

    @Nullable
    private BankSlot currentlyRenderingBankSlot = null;
    private boolean isLockSlotKeyDown;

    public BankScreen(BankScreenHandler screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);

        this.type = screenHandler.getBankType();
        this.texture = this.type.getGuiTexture();
        this.imageWidth = this.type.guiTextureWidth;
        this.imageHeight = this.type.guiTextureHeight;

        this.inventoryLabelY += this.type.rows * 18 - 52;

    }

    @Override
    protected void init() {
        super.init();
        BankOptions options = Util.getOrCreateOptions(this.menu.getBankLikeItem());
        this.sortMode = options.sortMode;
        PickupModeOption initialPickupMode = PickupModeOption.from(options.pickupMode);
        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<PickupModeOption>(initialPickupMode,
                        leftPos + titleLabelX + this.type.guiTextureWidth - 49, topPos + titleLabelY - 4, 14, 14,
                        WIDGETS_TEXTURE, this::onPickupModeButtonPress));

        this.addRenderableWidget(
                new SortButtonWidget(options.sortMode,
                        leftPos + titleLabelX + this.type.guiTextureWidth - 31, topPos + titleLabelY - 4,
                        14, 14, WIDGETS_TEXTURE, this::onSortButtonPress));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!(this.hoveredSlot instanceof BankSlot bankSlot))
            return super.mouseClicked(mouseX, mouseY, button);

        // middle click sorting
        if (button == 2) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            sendSortPacket();
            return true;
        }
        // left click + lockSlot keybind
        if (button == 0 && this.isLockSlotKeyDown) {

            System.out.println("locking some slot now frfr");
            int hoveredSlotIndex = bankSlot.index;
            ItemStack hoveredStack = bankSlot.getItem();
            ItemStack cursorStack = this.menu.getCarried();
            boolean isLocked = bankSlot.isLocked();

            Consumer<@Nullable ItemStack> lockSlot = stack -> {
                Minecraft.getInstance().getConnection().send(new LockSlotPacketC2S(this.menu.containerId, hoveredSlotIndex,
                        stack == null ? ItemStack.EMPTY : stack, stack != null));
            };

            if (isLocked) {
                if (cursorStack.isEmpty()) {
                    lockSlot.accept(null);
                } else if (hoveredStack.isEmpty()) {
                    lockSlot.accept(cursorStack);
                }
            } else {
                if (hoveredStack.isEmpty()) {
                    lockSlot.accept(cursorStack);
                } else if (cursorStack.isEmpty()
                        || ItemStack.isSameItemSameComponents(hoveredStack, cursorStack)) {
                    lockSlot.accept(hoveredStack);
                }

            }
            this.skipNextRelease = true;
            return true;

        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode)) {
            this.isLockSlotKeyDown = true;
            System.out.println("press: match");
        } else
            System.out.println("press: no match");
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode)) {
            this.isLockSlotKeyDown = false;
            System.out.println("release: match");
        } else
            System.out.println("release: no match");
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
        this.setFocused(null);
    }


    @Override
    protected void renderBg(GuiGraphics drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // wtf?? TODO! fix
        drawContext.blit(this.texture, x, y, 0, 0, imageWidth, imageHeight,
                (int) Math.ceil(imageWidth / 256d) * 256, (int) Math.ceil(imageHeight / 256d) * 256);
    }

    @Override
    public void renderSlot(GuiGraphics context, Slot slot) {
        if (slot instanceof BankSlot bankSlot)
            this.currentlyRenderingBankSlot = bankSlot;
        super.renderSlot(context, slot);

        this.currentlyRenderingBankSlot = null;

    }

    @Override
    protected void renderSlotContents(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot, @Nullable String countString) {
        if (this.currentlyRenderingBankSlot == null) {
            super.renderSlotContents(guiGraphics, itemstack, slot, countString);
            return;
        }
        renderBankSlot(guiGraphics, this.currentlyRenderingBankSlot, itemstack, countString != null);
    }

    private void renderBankSlot(GuiGraphics context, BankSlot slot, ItemStack stack, boolean drawInYellow) {

        // TODO:
        // () render locked texture
        // render item
        // (transparency hack): render transparent overlay
        // render count
        // render decorations without count

        int x = slot.x;
        int y = slot.y;

        if (slot.isLocked()) {
            // locked slot texture
            context.blit(WIDGETS_TEXTURE, x, y, 0, 46, 16, 16);
        }


        if (slot.isLocked() && stack.isEmpty()) {
            // draw transparent item
            // TODO: make transparent
            int seed = x + y * this.imageWidth;
            context.renderItem(stack, x, y, seed);
        } else {
            // draw opaque item
            int seed = x + y * this.imageWidth;
            context.renderItem(stack, x, y, seed);
        }

//        if (itemStack.isEmpty() && slot.isEnabled() && slot.isLocked()) {
//            // transparent item (not transparent)
//            context.drawItem(slot.getLockedStack(), i, j);
//
//            // overlay transparent texture with backgroud color to trick everyone into
//            // thinking the item is transparent
//            RenderSystem.enableBlend();
//            context.drawTexture(WIDGETS_TEXTURE, i, j, 200, 32, 46, 16, 16, 256, 256);
//            RenderSystem.disableBlend();
//        }

        if (drawInYellow || stack.getCount() > 1)
            drawItemCount(context, this.font, stack.getCount(), x, y, drawInYellow);

        // copyWithCount(1) and null text to force not draw count text
        context.renderItemDecorations(this.font, stack.copyWithCount(1), x, y, null);


    }

    public static void drawItemCount(GuiGraphics context, Font textRenderer, int count, int x, int y, boolean drawInYellow) {

        // TODO: this: optimize text scale and such

        PoseStack poseStack = context.pose();

        poseStack.pushPose();


        String countString = ItemCountUtils.toConsiseString(count);
        String formattedString = drawInYellow ? ChatFormatting.YELLOW + countString : countString;
        float scale = ItemCountUtils.scale(countString);

        int textWidth = (int) (textRenderer.width(countString));

        int xOffset = x + 18 - 2;
        int yOffset = y + 18 - 2;

        // scale from origin bottom right corner of slot
        poseStack.translate(xOffset, yOffset, 0);
        poseStack.scale(scale, scale, 1);
        poseStack.translate(-xOffset, -yOffset, 0);

        poseStack.translate(0.0f, 0.0f, 200.0f);
        context.drawString(textRenderer, formattedString, x + 18 - 1 - textWidth, y + 9, 0xFFFFFF, true);


        poseStack.popPose();


    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(stack);
        if (stack.getCount() > 9999)
            tooltip.add(1, Component.literal(FORMAT.format(stack.getCount())).withStyle(ChatFormatting.GRAY));

        return tooltip;
    }

    private void onPickupModeButtonPress(TexturedCyclingButtonWidget<PickupModeOption> button) {
        button.state = switch (button.state) {
            case NO_PICKUP -> PickupModeOption.ALL;
            case ALL -> PickupModeOption.FILTERED;
            case FILTERED -> PickupModeOption.VOID_OVERFLOW;
            case VOID_OVERFLOW -> PickupModeOption.NO_PICKUP;
        };
        button.refreshTooltip();
        Minecraft.getInstance().getConnection().send(new PickupModePacketC2S());
    }

    private void onSortButtonPress(SortButtonWidget button) {
        if (button.timeSinceLastPressed() < 1000) {
            button.sortMode = switch (button.sortMode) {
                case COUNT -> SortMode.NAME;
                case NAME -> SortMode.MOD;
                case MOD -> SortMode.COUNT;
            };
        }
        this.sortMode = button.sortMode;
        button.refreshTooltip();
        sendSortPacket();
    }

    private void sendSortPacket() {

        Minecraft.getInstance().getConnection().send(new SortPacketC2S(this.sortMode));
    }
}

enum PickupModeOption implements CycleableOption {
    NO_PICKUP("no_pickup", 0, 70),
    ALL("pickup_all", 14, 70),
    FILTERED("filtered", 28, 70),
    VOID_OVERFLOW("void_overflow", 42, 70);

    private final Component name;
    private final Component info;

    private final int uOffset;
    private final int vOffset;

    private PickupModeOption(String name, int uOffset, int vOffset) {
        this.name = Component.translatable("title.bankstorage.pickupmode." + name);
        this.info = Component.translatable("tooltip.bankstorage.pickupmode." + name);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public static PickupModeOption from(PickupMode pickupMode) {
        return switch (pickupMode) {
            case NONE -> NO_PICKUP;
            case ALL -> ALL;
            case FILTERED -> FILTERED;
            case VOID -> VOID_OVERFLOW;
        };
    }

    public PickupMode toPickupMode() {
        return switch (this) {
            case NO_PICKUP -> PickupMode.NONE;
            case ALL -> PickupMode.ALL;
            case FILTERED -> PickupMode.FILTERED;
            case VOID_OVERFLOW -> PickupMode.VOID;
        };
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public Component getInfo() {
        return this.info;
    }

    @Override
    public int uOffset() {
        return this.uOffset;
    }

    @Override
    public int vOffset() {
        return this.vOffset;
    }
}
