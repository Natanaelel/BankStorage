package net.natte.bankstorage.client.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.rendering.ItemCountUtils;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.util.Util;

import java.util.List;

public class BankTooltipComponent implements ClientTooltipComponent {

    public static final ResourceLocation TEXTURE = Util.ID("textures/gui/widgets.png");

    private final List<ItemStack> items;
    private final Minecraft client;

    private BankTooltipComponent(List<ItemStack> items) {
        this.items = items;
        this.client = Minecraft.getInstance();
    }

    public static BankTooltipComponent of(BankTooltipData tooltipData) {
        return new BankTooltipComponent(tooltipData.items());
    }

    private int getRows() {
        return (int) Math.ceil(items.size() / 9d);
    }

    private int getColumns() {
        return Math.min(9, items.size());
    }

    @Override
    public int getHeight() {
        return getRows() * 18 + 2 + 4;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return getColumns() * 18 + 2;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, GuiGraphics context) {
        drawBackground(context, x, y);

        int row = 0;
        int col = 0;
        for (ItemStack itemStack : items) {
            drawSlot(itemStack, context, textRenderer, x + col * 18 + 1, y + row * 18 + 1);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawBackground(GuiGraphics context, int x, int y) {
        int row = 0;
        int col = 0;
        for (@SuppressWarnings("unused") ItemStack stack : items) {
            context.blit(TEXTURE, x + col * 18, y + row * 18, 20, 128, 20, 20);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawSlot(ItemStack itemStack, GuiGraphics context, Font textRenderer, int x, int y) {
        // slot texture
        context.blit(TEXTURE, x, y, 1, 129, 18, 18);
        // item
        context.renderItem(itemStack, x + 1, y + 1);
        // item count
        if (itemStack.getCount() > 1)
            BankScreen.drawItemCount(context, textRenderer, itemStack.getCount(), x + 1, y + 1, false);

        // copyWithCount(1) and null text to force not draw count text
        context.renderItemDecorations(textRenderer, itemStack.copyWithCount(1), x, y, null);
    }
}
