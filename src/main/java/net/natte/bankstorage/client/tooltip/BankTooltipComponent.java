package net.natte.bankstorage.client.tooltip;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.rendering.ItemCountUtils;
import net.natte.bankstorage.item.tooltip.BankTooltipData;
import net.natte.bankstorage.rendering.ItemCountUtils;
import net.natte.bankstorage.util.Util;

public class BankTooltipComponent implements ClientTooltipComponent {

    public static final ResourceLocation TEXTURE = Util.ID("textures/gui/widgets.png");

    private final List<ItemStack> items;
    private final Minecraft client;

    private BankTooltipComponent(List<ItemStack> items) {
        this.items = items;
        this.client = Minecraft.getInstance();
    }

    public static BankTooltipComponent of(BankTooltipData tooltipData){
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
        // item count, durability, cooldown
        drawItemCountInSlot(context, textRenderer, itemStack, x + 1, y + 1);
    }

    public void drawItemCountInSlot(GuiGraphics context, Font textRenderer, ItemStack stack, int x, int y) {
        LocalPlayer clientPlayerEntity;
        float f;
        int l;
        int k;
        if (stack.isEmpty()) {
            return;
        }
        PoseStack matrices = context.pose();
        matrices.pushPose();

        if (stack.isBarVisible()) {
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            k = x + 2;
            l = y + 13;
            context.fill(RenderType.guiOverlay(), k, l, k + 13, l + 2, -16777216);
            context.fill(RenderType.guiOverlay(), k, l, k + i, l + 1, j | 0xFF000000);
        }
        if (stack.getCount() != 1) {
            String count = ItemCountUtils.toConsiseString(stack.getCount());
            String string = count;
            matrices.translate(0.0f, 0.0f, 200.0f);
            float scale = ItemCountUtils.scale(string);

            int textWidth = (int) (textRenderer.width(string));

            int xOffset = x + 18 - 2;
            int yOffset = y + 18 - 2;
            matrices.pushPose();
            matrices.translate(xOffset, yOffset, 0);
            matrices.scale(scale, scale, 1);
            matrices.translate(-xOffset, -yOffset, 0);
            // TODO: check out drawCenteredString maybe?
            context.drawString(textRenderer, string, x + 18 - 1 - textWidth, y + 9, 0xFFFFFF, true);
            matrices.popPose();

        }
        f = (clientPlayerEntity = this.client.player) == null ? 0.0f
                : clientPlayerEntity.getCooldowns().getCooldownPercent(stack.getItem(),
                        this.client.getFrameTime());
        if (f > 0.0f) {
            k = y + Mth.floor((float) (16.0f * (1.0f - f)));
            l = k + Mth.ceil((float) (16.0f * f));
            context.fill(RenderType.guiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
        }
        matrices.popPose();
    }
}
