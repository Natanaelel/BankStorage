package net.natte.bankstorage.screen;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.network.PickupModePacket;
import net.natte.bankstorage.network.SortPacket;
import net.natte.bankstorage.rendering.ItemCountUtils;

public class BankScreen extends HandledScreen<BankScreenHandler> {

    public static final ScreenHandlerType<BankScreenHandler> BANK_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(null,
            null);

    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private BankType type;
    private Identifier texture;

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
        playerInventoryTitleY += (this.type.rows - 1) * 18 - 34;

    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.bankstorage.sort"), button -> ClientPlayNetworking.send(SortPacket.C2S_PACKET_ID, PacketByteBufs.create()))
                        .dimensions(x + titleX + this.type.guiTextureWidth - 60, y + titleY - 2, 40, 12).build());

        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("button.bankstorage.pickupmode"), button -> ClientPlayNetworking.send(PickupModePacket.C2S_PACKET_ID, PacketByteBufs.create()))
                        .dimensions(x + titleX + this.type.guiTextureWidth - 110, y + titleY - 2, 40, 12).build());

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
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
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void drawSlot(DrawContext context, Slot slot) {
        if (slot instanceof BankSlot bankSlot) {
            drawBankSlot(context, slot);
        } else {
            super.drawSlot(context, slot);
        }

    }

    private void drawBankSlot(DrawContext context, Slot slot) {
        Pair<Identifier, Identifier> pair;
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
        if (itemStack.isEmpty() && slot.isEnabled() && (pair = slot.getBackgroundSprite()) != null) {
            Sprite sprite = this.client.getSpriteAtlas((Identifier) pair.getFirst())
                    .apply((Identifier) pair.getSecond());
            context.drawSprite(i, j, 0, 16, 16, sprite);
            bl2 = true;
        }
        if (!bl2) {
            if (bl) {
                context.fill(i, j, i + 16, j + 16, -2130706433);
            }
            context.drawItem(itemStack, i, j, slot.x + slot.y * this.backgroundWidth);
            // context.drawItemInSlot(this.textRenderer, itemStack, i, j, string);
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
        // boolean bl = slot == null || !slot.hasStack();
        boolean bl = !slot.hasStack();
        if (!bl && ItemStack.canCombine(stack, slot.getStack())) {
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
            // matrices.translate(x, y, k);
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

}
