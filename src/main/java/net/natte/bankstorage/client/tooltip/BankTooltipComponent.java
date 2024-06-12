package net.natte.bankstorage.client.tooltip;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.rendering.ItemCountUtils;
import net.natte.bankstorage.util.Util;

public class BankTooltipComponent implements TooltipComponent {

    public static final Identifier TEXTURE = Util.ID("textures/gui/widgets.png");

    private final List<ItemStack> items;
    private final MinecraftClient client;

    public BankTooltipComponent(List<ItemStack> items) {
        this.items = items;
        this.client = MinecraftClient.getInstance();
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
    public int getWidth(TextRenderer textRenderer) {
        return getColumns() * 18 + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
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

    private void drawBackground(DrawContext context, int x, int y) {
        int row = 0;
        int col = 0;
        for (@SuppressWarnings("unused") ItemStack stack : items) {
            context.drawTexture(TEXTURE, x + col * 18, y + row * 18, 20, 128, 20, 20);
            ++col;
            if (col == 9) {
                col = 0;
                ++row;
            }
        }
    }

    private void drawSlot(ItemStack itemStack, DrawContext context, TextRenderer textRenderer, int x, int y) {
        // slot texture
        context.drawTexture(TEXTURE, x, y, 1, 129, 18, 18);
        // item
        context.drawItem(itemStack, x + 1, y + 1);
        // item count, durability, cooldown
        drawItemCountInSlot(context, textRenderer, itemStack, x + 1, y + 1);
    }

    public void drawItemCountInSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
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
        if (stack.getCount() != 1) {
            String count = ItemCountUtils.toConsiseString(stack.getCount());
            String string = count;
            matrices.translate(0.0f, 0.0f, 200.0f);
            float scale = ItemCountUtils.scale(string);

            int textWidth = (int) (textRenderer.getWidth(string));

            int xOffset = x + 18 - 2;
            int yOffset = y + 18 - 2;
            matrices.push();
            matrices.translate(xOffset, yOffset, 0);
            matrices.scale(scale, scale, 1);
            matrices.translate(-xOffset, -yOffset, 0);
            context.drawText(textRenderer, string, x + 18 - 1 - textWidth, y + 9, 0xFFFFFF, true);
            matrices.pop();

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
}
