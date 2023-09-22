package net.natte.bankstorage.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public class BuildModePreviewRenderer implements EndTick {

    public static final BuildModePreviewRenderer Instance = new BuildModePreviewRenderer();

    public List<ItemStack> stacks = new ArrayList<>();

    public ItemStack stackInHand;
    public UUID uuid;
    private MinecraftClient client;

    private CachedBankStorage bankStorage;

    public BuildModePreviewRenderer() {
        this.client = MinecraftClient.getInstance();
        this.stackInHand = ItemStack.EMPTY;
    }

    public void render(DrawContext context, float tickDelta) {

        // if (!Util.isBank(this.stackInHand))
        // return;
        if (this.bankStorage == null)
            return;
        if (this.bankStorage.options.buildMode == BuildMode.NONE)
            return;
        if (this.bankStorage.items.isEmpty())
            return;

        List<ItemStack> itemStacks = this.bankStorage.items;

        int scaledHeight = context.getScaledWindowHeight();
        int scaledWidth = context.getScaledWindowWidth();

        RenderSystem.enableBlend();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        int selectedSlot = bankStorage.options.selectedItemSlot;
        
        if (itemStacks.size() == 1) {
            context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
            scaledWidth / 2 - 169, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == 0 || selectedSlot == itemStacks.size() - 1) {
            boolean isLeft = bankStorage.options.selectedItemSlot > 0;
            context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
                    scaledWidth / 2 - (isLeft ? 189 : 169), scaledHeight - 22, 22, 0, 42, 22);
        } else {
            context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
                    scaledWidth / 2 - 189, scaledHeight - 22, 64, 0, 62, 22);
        }
        // matrixStack.pop();
        for (int i = -1; i <= 1; ++i) {
            int index = selectedSlot - i;
            if (index < 0 || index >= itemStacks.size())
                continue;
            ItemStack itemStack = itemStacks.get(index);
            int y = scaledHeight - 16 - 3;
            int x = scaledWidth / 2 - 90 - 29 - 29 - i * 20 + 2 - 20;

            renderHotbarItem(context, x, y, tickDelta, client.player, itemStack, 0);
        }

        // matrixStack.push();

        context.drawTexture(new Identifier(BankStorage.MOD_ID, "textures/gui/widgets.png"),
                scaledWidth / 2 - 170, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.pop();

        RenderSystem.disableBlend();

    }

    private void renderHotbarItem(DrawContext context, int x, int y, float f, PlayerEntity player, ItemStack stack,
            int seed) {
        if (stack.isEmpty()) {
            return;
        }

        float g = (float) stack.getBobbingAnimationTime() - f;
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            context.getMatrices().push();
            context.getMatrices().translate(x + 8, y + 12, 0.0f);
            context.getMatrices().scale(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            context.getMatrices().translate(-(x + 8), -(y + 12), 0.0f);
        }
        context.drawItem((LivingEntity) player, stack, x, y, seed);
        if (g > 0.0f) {
            context.getMatrices().pop();
        }
        drawItemCountInSlot(context, this.client.textRenderer, stack, x, y);
    }

    synchronized public List<ItemStack> getItemStacks(ItemStack bank) {

        return this.stacks;

    }

    synchronized public void setItemStacks(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void onEndTick(MinecraftClient client) {
        if (client.player == null)
            return;
        ItemStack stackInHand = client.player.getStackInHand(client.player.getActiveHand());

        if (Util.isBank(stackInHand)) {
            if (stackInHand != this.stackInHand) {
                clearBank();
                this.stackInHand = stackInHand;
                if (Util.hasUUID(this.stackInHand)) {
                    this.setUUID(Util.getUUID(this.stackInHand));
                }
                updateBank();
                // setBankStorage(CachedBankStorage.getBankStorage(this.stackInHand));
            }
        } else {
            this.stackInHand = ItemStack.EMPTY;
            this.bankStorage = null;
            this.uuid = null;
        }
    }

    public void setBankStorage(CachedBankStorage bankStorage) {
        this.bankStorage = bankStorage;
    }

    public void updateBank() {
        setBankStorage(CachedBankStorage.getBankStorage(this.stackInHand));
    }

    public void clearBank() {
        this.uuid = null;
        this.bankStorage = null;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
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
            // matrices.translate(x, y, k);
            float scale = ItemCountUtils.scale(string);

            matrices.translate(x * (1 - scale), y * (1 - scale) + (1 - scale) * 16, 0);
            matrices.scale(scale, scale, 1);

            int textWidth = (int) (textRenderer.getWidth(string) * scale);
            context.drawText(textRenderer, string, x + 19 - 2 - textWidth, y + 6 + 3, 0xFFFFFF, true);
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
