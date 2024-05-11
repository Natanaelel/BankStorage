package net.natte.bankstorage.screen;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorageClient;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.PickupMode;
import net.natte.bankstorage.options.SortMode;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.natte.bankstorage.packet.server.PickupModePacketC2S;
import net.natte.bankstorage.packet.server.SortPacketC2S;
import net.natte.bankstorage.rendering.ItemCountUtils;
import net.natte.bankstorage.util.Util;

public class BankScreen extends HandledScreen<BankScreenHandler> {

    private static final Identifier WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private BankType type;
    private Identifier texture;

    private SortMode sortMode;

    public static Provider<BankScreenHandler, BankScreen> fromType(BankType type) {
        return (screenHandler, playerInventory, text) -> {
            return new BankScreen(screenHandler, playerInventory, text, type);
        };
    }

    public BankScreen(BankScreenHandler screenHandler, PlayerInventory playerInventory, Text text, BankType type) {
        super(screenHandler, playerInventory, text);

        this.type = type;
        this.texture = this.type.getGuiTexture();
        this.backgroundWidth = this.type.guiTextureWidth;
        this.backgroundHeight = this.type.guiTextureHeight;

        this.playerInventoryTitleY += this.type.rows * 18 - 52;

    }

    @Override
    protected void init() {
        super.init();
        BankOptions options = Util.getOrCreateOptions(this.handler.getBankLikeItem());
        this.sortMode = options.sortMode;
        PickupModeOption initialPickupMode = PickupModeOption.from(options.pickupMode);
        this.addDrawableChild(
                new TexturedCyclingButtonWidget<PickupModeOption>(initialPickupMode,
                        x + titleX + this.type.guiTextureWidth - 49, y + titleY - 4, 14,
                        14, 14, WIDGETS_TEXTURE, this::onPickupModeButtonPress));

        this.addDrawableChild(
                new SortButtonWidget(options.sortMode,
                        x + titleX + this.type.guiTextureWidth - 31, y + titleY - 4,
                        14, 14, WIDGETS_TEXTURE, this::onSortButtonPress));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        // middle click sorting
        if (button == 2 && (this.getSlotAt(mouseX, mouseY) instanceof BankSlot)) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            sendSortPacket();
            return true;
        }
        // left click + lockSlot keybind
        if (button == 0 && BankStorageClient.lockSlotKeyBinding.isPressed()) {
            Slot slot = this.getSlotAt(mouseX, mouseY);
            if (slot instanceof BankSlot hoveredSlot) {
                int hoveredSlotIndex = hoveredSlot.getIndex();
                ItemStack hoveredStack = hoveredSlot.getStack();
                ItemStack cursorStack = this.handler.getCursorStack();
                boolean isLocked = hoveredSlot.isLocked();

                Consumer<@Nullable ItemStack> lockSlot = stack -> {
                    ClientPlayNetworking.send(new LockSlotPacketC2S(this.handler.syncId, hoveredSlotIndex,
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
                    } else if (cursorStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(hoveredStack, cursorStack)) {
                        lockSlot.accept(hoveredStack);
                    }

                }
                this.cancelNextRelease = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode)) {
            BankStorageClient.lockSlotKeyBinding.setPressed(true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (BankStorageClient.lockSlotKeyBinding.matchesKey(keyCode, scanCode)) {
            BankStorageClient.lockSlotKeyBinding.setPressed(false);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.setFocused(null);
    }

    @Override
    protected void calculateOffset() {
        ItemStack itemStack = this.handler.getCursorStack();
        if (itemStack.isEmpty() || !this.cursorDragging) {
            return;
        }
        if (this.heldButtonType == 2) {
            this.draggedStackRemainder = itemStack.getMaxCount();
            return;
        }
        this.draggedStackRemainder = itemStack.getCount();
        for (Slot slot : this.cursorDragSlots) {
            ItemStack itemStack2 = slot.getStack();
            int i = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
            int j = slot.getMaxItemCount(itemStack);
            int k = Math.min(ScreenHandler.calculateStackSize(this.cursorDragSlots, (int) this.heldButtonType,
                    (ItemStack) itemStack) + i, j);
            this.draggedStackRemainder -= k - i;
        }
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight,
                (int) Math.ceil(backgroundWidth / 256d) * 256, (int) Math.ceil(backgroundHeight / 256d) * 256);
    }

    @Override
    public void drawSlot(DrawContext context, Slot slot) {
        if (slot instanceof BankSlot bankSlot) {
            drawBankSlot(context, bankSlot);
        } else {
            super.drawSlot(context, slot);
        }

    }

    private void drawBankSlot(DrawContext context, BankSlot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getStack();
        boolean bl = false;
        boolean bl2 = slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && !this.touchIsRightClickDrag;
        ItemStack itemStack2 = this.handler.getCursorStack();
        boolean drawInYellow = false;
        if (slot == this.touchDragSlotStart && !this.touchDragStack.isEmpty() && this.touchIsRightClickDrag
                && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.cursorDragging && this.cursorDragSlots.contains(slot) && !itemStack2.isEmpty()) {
            if (this.cursorDragSlots.size() == 1) {
                return;
            }
            if (this.canInsertItemIntoSlot((Slot) slot, (ItemStack) itemStack2, (boolean) true)
                    && this.handler.canInsertIntoSlot(slot)) {
                bl = true;
                int k = slot.getMaxItemCount(itemStack2);
                int l = slot.getStack().isEmpty() ? 0 : slot.getStack().getCount();
                int m = this.calculateStackSize(this.cursorDragSlots, (int) this.heldButtonType, (ItemStack) itemStack2)
                        + l;
                if (m > k) {
                    m = k;
                    drawInYellow = true;
                }
                itemStack = itemStack2.copyWithCount(m);
            } else {
                this.cursorDragSlots.remove(slot);
                this.calculateOffset();
            }
        }
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);

        if (slot.isLocked()) {
            // locked slot texture
            context.drawTexture(WIDGETS_TEXTURE, i, j, itemStack.isEmpty() ? 16 : 0, 46, 16, 16);
        }

        if (itemStack.isEmpty() && slot.isEnabled() && slot.isLocked()) {
            // transparent item (not transparent)
            context.drawItem(slot.getLockedStack(), i, j);

            // overlay transparent texture with backgroud color to trick everyone into
            // thinking the item is transparent
            RenderSystem.enableBlend();
            context.drawTexture(WIDGETS_TEXTURE, i, j, 200, 32, 46, 16, 16, 256, 256);
            RenderSystem.disableBlend();
        }

        if (!bl2) {
            if (bl) {
                context.fill(i, j, i + 16, j + 16, -2130706433);
            }
            context.drawItem(itemStack, i, j, slot.x + slot.y * this.backgroundWidth);
            this.drawItemCountInSlot(context, this.textRenderer, itemStack, i, j, drawInYellow);
        }
        context.getMatrices().pop();
    }

    public int calculateStackSize(Set<Slot> slots, int mode, ItemStack stack) {
        return switch (mode) {
            case 0 -> MathHelper.floor((float) stack.getCount() / (float) slots.size());
            case 1 -> 1;
            case 2 -> stack.getItem().getMaxCount();
            default -> stack.getCount();
        };
    }

    public boolean canInsertItemIntoSlot(/* @Nullable */ Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl = !slot.hasStack();
        if (slot instanceof BankSlot bankSlot) {
            if (bankSlot.isLocked()) {
                if (!Util.canCombine(stack, bankSlot.getLockedStack()))
                    return false;
            }
        }
        if (!bl && ItemStack.areItemsAndComponentsEqual(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= slot.getMaxItemCount(stack);
        }
        return bl;
    }

    public void drawItemCountInSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y,
            boolean drawInYellow) {
        ClientPlayerEntity clientPlayerEntity;
        float f;
        int l;
        int k;
        if (stack.isEmpty()) {
            return;
        }
        MatrixStack matrices = context.getMatrices();

        matrices.push();

        if (stack.isItemBarVisible()) {
            int i = stack.getItemBarStep();
            int j = stack.getItemBarColor();
            k = x + 2;
            l = y + 13;
            context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, -16777216);
            context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | 0xFF000000);
        }
        if (stack.getCount() != 1 || drawInYellow) {
            String count = ItemCountUtils.toConsiseString(stack.getCount());
            String string = count;
            String formattedString = drawInYellow ? Formatting.YELLOW.toString() + count : count;
            matrices.translate(0.0f, 0.0f, 200.0f);
            float scale = ItemCountUtils.scale(string);

            matrices.translate(x * (1 - scale), y * (1 - scale) + (1 - scale) * 16, 0);
            matrices.scale(scale, scale, 1);

            int textWidth = (int) (textRenderer.getWidth(string) * scale);
            context.drawText(textRenderer, formattedString, x + 19 - 2 - textWidth, y + 6 + 3, 0xFFFFFF, true);
        }
        f = (clientPlayerEntity = this.client.player) == null ? 0.0f
                : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(),
                        this.client.getTickDelta());
        if (f > 0.0f) {
            k = y + MathHelper.floor((float) (16.0f * (1.0f - f)));
            l = k + MathHelper.ceil((float) (16.0f * f));
            context.fill(RenderLayer.getGuiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
        }
        matrices.pop();
    }

    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            ItemStack itemStack = this.focusedSlot.getStack();
            List<Text> tooltip = this.getTooltipFromItem(itemStack);
            if (itemStack.getCount() > 9999) {
                tooltip.add(1, Text.literal(FORMAT.format(itemStack.getCount())).formatted(Formatting.GRAY));
            }
            context.drawTooltip(this.textRenderer, tooltip, itemStack.getTooltipData(), x, y);
        }
    }

    private void onPickupModeButtonPress(TexturedCyclingButtonWidget<PickupModeOption> button) {
        button.state = switch (button.state) {
            case NO_PICKUP -> PickupModeOption.ALL;
            case ALL -> PickupModeOption.FILTERED;
            case FILTERED -> PickupModeOption.VOID_OVERFLOW;
            case VOID_OVERFLOW -> PickupModeOption.NO_PICKUP;
        };
        button.refreshTooltip();
        ClientPlayNetworking.send(new PickupModePacketC2S());
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

        ClientPlayNetworking.send(new SortPacketC2S(this.sortMode));
    }
}

enum PickupModeOption implements CycleableOption {
    NO_PICKUP("no_pickup", 0, 70),
    ALL("pickup_all", 14, 70),
    FILTERED("filtered", 28, 70),
    VOID_OVERFLOW("void_overflow", 42, 70);

    private Text name;
    private Text info;

    private int uOffset;
    private int vOffset;

    private PickupModeOption(String name, int uOffset, int vOffset) {
        this.name = Text.translatable("title.bankstorage.pickupmode." + name);
        this.info = Text.translatable("tooltip.bankstorage.pickupmode." + name);
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
    public Text getName() {
        return this.name;
    }

    @Override
    public Text getInfo() {
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
