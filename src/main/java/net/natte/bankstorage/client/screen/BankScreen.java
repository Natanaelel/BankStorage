package net.natte.bankstorage.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.client.rendering.ItemCountUtils;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class BankScreen extends AbstractContainerScreen<BankScreenHandler> {

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private final BankType type;
    private final ResourceLocation texture;

    private SortMode sortMode;

    @Nullable
    private BankSlot currentlyRenderingBankSlot = null;
    private boolean isLockSlotKeyDown;

    public BankScreen(BankScreenHandler screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);

        this.type = screenHandler.getBankType();
        this.texture = this.type.getGuiTexture();
        this.imageWidth = this.type.guiImageWidth;
        this.imageHeight = this.type.guiImageHeight;

        this.inventoryLabelY += this.type.rows * 18 - 52;
    }

    @Override
    protected void init() {
        super.init();
        BankOptions options = Util.getOrCreateOptions(this.menu.getBankLikeItem());
        this.sortMode = options.sortMode();
        PickupModeOption initialPickupMode = PickupModeOption.from(options.pickupMode());
        this.addRenderableWidget(
                new TexturedCyclingButtonWidget(initialPickupMode,
                        leftPos + titleLabelX + this.imageWidth - 49, topPos + titleLabelY - 4, 14, 14,
                        BankStorageClient.WIDGETS_TEXTURE, this::onPickupModeButtonPress));

        this.addRenderableWidget(
                new SortButtonWidget(options.sortMode(),
                        leftPos + titleLabelX + this.imageWidth - 31, topPos + titleLabelY - 4,
                        14, 14, BankStorageClient.WIDGETS_TEXTURE, this::onSortButtonPress));
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
            handleSlotLock(bankSlot);

            this.skipNextRelease = true;
            return true;

        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    private void handleSlotLock(BankSlot bankSlot) {

        int hoveredSlotIndex = bankSlot.index;
        ItemStack hoveredStack = bankSlot.getItem();
        ItemStack cursorStack = this.menu.getCarried();

        boolean isSlotEmpty = hoveredStack.isEmpty();
        ItemStack lockedStack = bankSlot.getLockedStack();

        boolean shouldUnLock = bankSlot.isLocked() && (cursorStack.isEmpty() || !isSlotEmpty || ItemStack.isSameItemSameComponents(cursorStack, lockedStack));

        minecraft.getConnection().send(
                new LockSlotPacketC2S(
                        this.menu.containerId,
                        hoveredSlotIndex,
                        isSlotEmpty ? cursorStack : hoveredStack,
                        !shouldUnLock));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode)) {
            this.isLockSlotKeyDown = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matches(keyCode, scanCode)) {
            this.isLockSlotKeyDown = false;
        }
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
        drawContext.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.type.guiTextureWidth, this.type.guiTextureHeight);
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

        int x = slot.x;
        int y = slot.y;
        int seed = x + y * this.imageWidth;

        if (slot.isLocked())
            // locked slot texture
            context.blit(BankStorageClient.WIDGETS_TEXTURE, x, y, stack.isEmpty() ? 16 : 0, 46, 16, 16);

        context.renderItem(slot.isLocked() ? slot.getLockedStack() : stack, x, y, seed);

        if (slot.isLocked() && stack.isEmpty()) {
            // overlay transparent texture with background color to trick everyone into
            // thinking the item is transparent
            RenderSystem.enableBlend();
            context.blit(BankStorageClient.WIDGETS_TEXTURE, x, y, 200, 32, 46, 16, 16, 256, 256);
            RenderSystem.disableBlend();
        }

        if (drawInYellow || stack.getCount() > 1)
            drawItemCount(context, this.font, stack.getCount(), x, y, drawInYellow);

        // copyWithCount(1) and null text to force not draw count text
        context.renderItemDecorations(this.font, stack.copyWithCount(1), x, y, null);


    }

    public static void drawItemCount(GuiGraphics context, Font textRenderer, int count, int x, int y,
                                     boolean drawInYellow) {


        PoseStack poseStack = context.pose();

        poseStack.pushPose();


        String countString = ItemCountUtils.toConsiseString(count);
        String formattedString = drawInYellow ? ChatFormatting.YELLOW + countString : countString;
        float scale = ItemCountUtils.scale();

        int textWidth = textRenderer.width(countString);

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

    private void onPickupModeButtonPress(TexturedCyclingButtonWidget button) {
        button.nextState();
        button.refreshTooltip();
        Minecraft.getInstance().getConnection().send(new PickupModePacketC2S());
    }

    private void onSortButtonPress(SortButtonWidget button) {
        if (button.timeSinceLastPressed() < 1000) {
            button.sortMode = button.sortMode.next();
        }
        this.sortMode = button.sortMode;
        button.refreshTooltip();
        sendSortPacket();
    }

    private void sendSortPacket() {
        Minecraft.getInstance().getConnection().send(new SortPacketC2S(this.sortMode));
    }
}

