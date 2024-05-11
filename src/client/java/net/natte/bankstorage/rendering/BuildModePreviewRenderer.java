package net.natte.bankstorage.rendering;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.natte.bankstorage.item.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public class BuildModePreviewRenderer implements EndTick {

    private static final Identifier WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    public ItemStack stackInHand;
    public UUID uuid;
    private MinecraftClient client;

    private CachedBankStorage bankStorage;
    public BankOptions options = new BankOptions();

    private Hand hand;

    private int ticks = 0;

    public short revision = 0;

    public BuildModePreviewRenderer() {
        this.stackInHand = ItemStack.EMPTY;
    }

    public void onEndTick(MinecraftClient client) {
        if (this.client == null)
            this.client = client;

        if (client.player == null)
            return;

        this.ticks++;
        // request cache sync every 2 seconds if holding bank in buildmode
        if (this.ticks % (2 * 20) == 0 && this.uuid != null && this.options.buildMode != BuildMode.NONE)
            CachedBankStorage.requestCacheUpdate(this.uuid);

        ItemStack right = this.client.player.getMainHandStack();
        ItemStack left = this.client.player.getOffHandStack();

        ItemStack bankInHand;
        Hand hand;
        if (Util.isBankLike(right)) {
            bankInHand = right;
            hand = Hand.MAIN_HAND;
        } else if (Util.isBankLike(left)) {
            bankInHand = left;
            hand = Hand.OFF_HAND;
        } else {
            clearBank();
            return;
        }

        if (!Util.hasUUID(bankInHand)) {
            clearBank();
            return;
        }

        // is holding new bank: update things
        if (isHoldingNewBankLikeOrInNewHand(bankInHand, hand)) {

            clearBank();
            this.stackInHand = bankInHand;

            this.uuid = Util.getUUID(this.stackInHand);
            this.bankStorage = CachedBankStorage.getBankStorage(this.uuid);
            this.options = Util.getOptions(this.stackInHand);
            if(this.options == null){
                this.options = new BankOptions();
                System.out.println("options == null???");
            }

            // make sure client has the latest revision
            CachedBankStorage.requestCacheUpdate(this.uuid);


            this.hand = hand;
        }
    }

    private boolean isHoldingNewBankLikeOrInNewHand(ItemStack bankInHand, Hand hand) {

        // yes, new hand
        if (hand != this.hand)
            return true;

        // bruh, ofc it's not new
        if (bankInHand == this.stackInHand)
            return false;
        // one is link, other is bank
        if (this.stackInHand.getItem() != bankInHand.getItem())
            return true;
        // has different uuid
        if (this.uuid == null || !this.uuid.equals(Util.getUUID(bankInHand)))
            return true;

        return false;
    }

    public void setBankStorage(CachedBankStorage bankStorage) {
        this.bankStorage = bankStorage;
    }

    public void clearBank() {
        this.uuid = null;
        this.bankStorage = null;
        this.stackInHand = ItemStack.EMPTY;
    }

    public void render(DrawContext context, float tickDelta) {
        if (this.client == null)
            return;

        // definite condition
        if (this.uuid == null)
            return;

        if (this.bankStorage == null)
            return;
        switch (this.options.buildMode) {
            case NONE:
                // renderNothingLol();
                return;
            case NORMAL:
                renderBlockPreview(context, tickDelta);
                break;
            case RANDOM:
                renderRandomPreview(context, tickDelta);
                break;
        }

    }

    private void renderRandomPreview(DrawContext context, float tickDelta) {

        if (this.bankStorage.blockItems.isEmpty())
            return;

        List<ItemStack> items = this.bankStorage.blockItems;

        int scaledHeight = context.getScaledWindowHeight();
        int scaledWidth = context.getScaledWindowWidth();

        RenderSystem.enableBlend();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        int handXOffset = this.hand == Hand.OFF_HAND ? -169 : 118;

        // draw slot background
        context.drawTexture(WIDGET_TEXTURE,
                scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64 + 62, 0, 62, 22);

        // draw item
        ItemStack itemStack = items.get((ticks / 20) % items.size());
        int y = scaledHeight - 19;
        int x = scaledWidth / 2 + 3 + handXOffset;

        renderHotbarItem(context, x, y, tickDelta, this.client.player, itemStack, 0);

        // draw selection square
        context.drawTexture(WIDGET_TEXTURE, scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.pop();

        RenderSystem.disableBlend();
    }

    private void renderBlockPreview(DrawContext context, float tickDelta) {
        if (this.bankStorage.blockItems.isEmpty())
            return;

        List<ItemStack> items = this.bankStorage.blockItems;

        int scaledHeight = context.getScaledWindowHeight();
        int scaledWidth = context.getScaledWindowWidth();

        RenderSystem.enableBlend();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        int selectedSlot = this.options.selectedItemSlot;

        int handXOffset = this.hand == Hand.OFF_HAND ? -169 : 118;

        if (items.size() == 1) {
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 + handXOffset, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == 0 || selectedSlot == items.size() - 1) {
            boolean isLeft = this.options.selectedItemSlot > 0;
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 - (isLeft ? 20 : 0) + handXOffset, scaledHeight - 22, 22, 0, 42, 22);
        } else {
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64, 0, 62, 22);
        }

        for (int i = -1; i <= 1; ++i) {
            int index = selectedSlot - i;
            if (index < 0 || index >= items.size())
                continue;
            ItemStack itemStack = items.get(index);
            int y = scaledHeight - 19;
            int x = scaledWidth / 2 - i * 20 + 3 + handXOffset;

            renderHotbarItem(context, x, y, tickDelta, this.client.player, itemStack, 0);
        }

        context.drawTexture(WIDGET_TEXTURE,
                scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
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

    public void drawItemCountInSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
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

            matrices.translate(x * (1 - scale), y * (1 - scale) + (1 - scale) * 16, 0);
            matrices.scale(scale, scale, 1);

            int textWidth = (int) (textRenderer.getWidth(string) * scale);
            context.drawText(textRenderer, string, x + 19 - 2 - textWidth, y + 6 + 3, 0xFFFFFF, true);
        }

        matrices.pop();
    }

    public short nextRevision() {
        return ++this.revision;
    }
}
