package net.natte.bankstorage.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.BankStorageClient;
import net.natte.bankstorage.client.Resources;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BankScreen extends AbstractContainerScreen<BankScreenHandler> {

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private static final ResourceLocation WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private final BankType type;
    private ResourceLocation texture = Resources.NULL_TEXTURE;
    private int guiTextureWidth = 256;
    private int guiTextureHeight = 256;
    private boolean hasScrollBar = false;
    private int visibleRows;
    private int topVisibleRow = 0;

    private SortMode sortMode;

    @Nullable
    private BankSlot currentlyRenderingBankSlot = null;
    private boolean isLockSlotKeyDown;
    private float scrollValue = 0;
    private ScrollBarWidget scrollBar;

    public BankScreen(BankScreenHandler screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
        this.type = screenHandler.getBankType();
    }

    @Override
    protected void init() {

        this.visibleRows = getVisibleRows();
        hasScrollBar = this.visibleRows < this.type.rows;

        scrollTo(0);
        repositionPlayerSlots();

        this.imageWidth = 176;
        this.imageHeight = 114 + this.visibleRows * 18;

        this.texture = Resources.backGround(this.visibleRows);

        this.guiTextureWidth = 256;
        this.guiTextureHeight = Mth.ceil(this.imageHeight / 256d) * 256;

        this.inventoryLabelY = 20 + this.visibleRows * 18;


        super.init();


        BankOptions options = this.menu.getBankLikeItem().getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT);
        this.sortMode = options.sortMode();
        this.addRenderableWidget(
                new PickupModeButtonWidget(options.pickupMode(),
                        leftPos + titleLabelX + this.imageWidth - 49, topPos + titleLabelY - 4, 14, 14,
                        WIDGETS_TEXTURE, this::onPickupModeButtonPress));

        this.addRenderableWidget(
                new SortButtonWidget(options.sortMode(),
                        leftPos + titleLabelX + this.imageWidth - 31, topPos + titleLabelY - 4,
                        14, 14, WIDGETS_TEXTURE, this::onSortButtonPress));
        if (hasScrollBar)
            this.scrollBar = this.addRenderableWidget(new ScrollBarWidget(leftPos + this.imageWidth - 4, topPos, type.rows, visibleRows, this.scrollValue, this::onScroll));
        else
            this.scrollBar = null;
    }

    private int getVisibleRows() {
        int heightOf6RowsGui = 114 + 18 * 6; // ContainerScreen.<init>()
        int minimumScreenHeight = 240; // Window.calculateScale()
        int screenHeight = this.height;
        int maxRows = (screenHeight - minimumScreenHeight + heightOf6RowsGui - 114) / 18;
        return Mth.clamp(maxRows, 1, this.type.rows);
    }

    public boolean hasScrollBar() {
        return hasScrollBar;
    }

    public int getScrollBarHeight() {
        return this.visibleRows * 18 + 24;
    }

    private void onScroll(float percentage) {
        int hiddenRows = this.type.rows - this.visibleRows;
        int topRow = Mth.clamp(Math.round(percentage * hiddenRows), 0, hiddenRows);
        this.scrollValue = percentage;
        if (topRow != this.topVisibleRow)
            scrollTo(topRow);
    }

    private void scrollTo(int topRow) {
        this.topVisibleRow = topRow;
        for (int i = 0; i < this.type.size(); ++i) {
            int y = i / 9;
            BankSlot slot = (BankSlot) this.menu.getSlot(i);
            if (y < this.topVisibleRow || y >= this.topVisibleRow + this.visibleRows) {
                slot.setActive(false);
            } else {
                slot.setActive(true);
                slot.y = 18 + y * 18 - this.topVisibleRow * 18;
            }
        }
    }

    private void repositionPlayerSlots() {

        int inventoryY = 32 + this.visibleRows * 18;
        int i = this.type.size();
        // inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.menu.slots.get(i++).y = inventoryY + y * 18;
            }
        }

        // hotbar
        for (int x = 0; x < 9; ++x) {
            this.menu.slots.get(i++).y = inventoryY + 58;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (hasScrollBar && this.scrollBar != null && this.scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY))
            return true;

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (hasScrollBar && this.scrollBar != null && this.scrollBar.mouseReleased(mouseX, mouseY, button))
            return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (hasScrollBar && this.scrollBar != null && this.scrollBar.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

        // optimistically lock slot on client, will be synced later
        if (shouldUnLock)
            this.menu.unlockSlot(bankSlot.index);
        else
            this.menu.lockSlot(bankSlot.index, isSlotEmpty ? cursorStack : hoveredStack);

        PacketDistributor.sendToServer(
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
        drawContext.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.guiTextureWidth, this.guiTextureHeight);
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
            context.blit(WIDGETS_TEXTURE, x, y, stack.isEmpty() ? 16 : 0, 46, 16, 16);

        context.renderItem(slot.isLocked() ? slot.getLockedStack() : stack, x, y, seed);

        if (slot.isLocked() && stack.isEmpty()) {
            // overlay transparent texture with background color to trick everyone into
            // thinking the item is transparent
            RenderSystem.enableBlend();
            context.blit(WIDGETS_TEXTURE, x, y, 200, 32, 46, 16, 16, 256, 256);
            RenderSystem.disableBlend();
        }

        if (drawInYellow || stack.getCount() > 1)
            drawItemCount(context, this.font, stack.getCount(), x, y, drawInYellow);

        // copyWithCount(1) and null text to force not draw count text
        context.renderItemDecorations(this.font, stack.copyWithCount(1), x, y, null);
    }

    public static void drawItemCount(GuiGraphics context, Font textRenderer, int count, int x, int y, boolean drawInYellow) {

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

    private void onPickupModeButtonPress(PickupModeButtonWidget button) {
        button.nextState();
        button.refreshTooltip();
        PacketDistributor.sendToServer(new PickupModePacketC2S());
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
        PacketDistributor.sendToServer(new SortPacketC2S(this.sortMode));
    }
}
