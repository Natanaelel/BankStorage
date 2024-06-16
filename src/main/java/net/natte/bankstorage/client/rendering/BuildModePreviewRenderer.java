package net.natte.bankstorage.client.rendering;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;

public class BuildModePreviewRenderer {

    private static final ResourceLocation WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    public ItemStack stackInHand;
    public UUID uuid;
    private Minecraft client;

    private CachedBankStorage bankStorage;
    public BankOptions options = new BankOptions();

    private InteractionHand hand;

    private int ticks = 0;

    public short revision = 0;

    public BuildModePreviewRenderer() {
        this.stackInHand = ItemStack.EMPTY;
    }

    public void tick() {
        if (this.client == null)
            this.client = Minecraft.getInstance();

        if (client.player == null)
            return;

        this.ticks++;
        // request cache sync every 2 seconds if holding bank in buildmode
        if (this.ticks % (2 * 20) == 0 && this.uuid != null && this.options.buildMode != BuildMode.NONE)
            CachedBankStorage.requestCacheUpdate(this.uuid);

        ItemStack right = this.client.player.getMainHandItem();
        ItemStack left = this.client.player.getOffhandItem();

        ItemStack bankInHand;
        InteractionHand hand;
        if (Util.isBankLike(right)) {
            bankInHand = right;
            hand = InteractionHand.MAIN_HAND;
        } else if (Util.isBankLike(left)) {
            bankInHand = left;
            hand = InteractionHand.OFF_HAND;
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
            this.options = Util.getOrCreateOptions(this.stackInHand);

            // make sure client has the latest revision
            CachedBankStorage.requestCacheUpdate(this.uuid);

            this.hand = hand;
        }
    }

    private boolean isHoldingNewBankLikeOrInNewHand(ItemStack bankInHand, InteractionHand hand) {

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

    public void render(GuiGraphics context, DeltaTracker deltaTracker) {
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
                renderBlockPreview(context, deltaTracker);
                break;
            case RANDOM:
                renderRandomPreview(context, deltaTracker);
                break;
        }

    }

    private void renderRandomPreview(GuiGraphics context, DeltaTracker deltaTracker) {

        if (this.bankStorage.blockItems.isEmpty())
            return;

        List<ItemStack> items = this.bankStorage.blockItems;

        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();

        int handXOffset = this.hand == InteractionHand.OFF_HAND ? -169 : 118;

        // draw slot background
        context.blit(WIDGET_TEXTURE,
                scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64 + 62, 0, 62, 22);

        // draw item
        ItemStack itemStack = items.get((ticks / 20) % items.size());
        int y = scaledHeight - 19;
        int x = scaledWidth / 2 + 3 + handXOffset;

        renderHotbarItem(context, x, y, deltaTracker, this.client.player, itemStack, 0);

        // draw selection square
        context.blit(WIDGET_TEXTURE, scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.popPose();

        RenderSystem.disableBlend();
    }

    private void renderBlockPreview(GuiGraphics context, DeltaTracker deltaTracker) {
        if (this.bankStorage.blockItems.isEmpty())
            return;

        List<ItemStack> items = this.bankStorage.blockItems;

        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();

        int selectedSlot = this.options.selectedItemSlot;

        int handXOffset = this.hand == InteractionHand.OFF_HAND ? -169 : 118;

        if (items.size() == 1) {
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 + handXOffset, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == 0 || selectedSlot == items.size() - 1) {
            boolean isLeft = this.options.selectedItemSlot > 0;
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 - (isLeft ? 20 : 0) + handXOffset, scaledHeight - 22, 22, 0, 42, 22);
        } else {
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64, 0, 62, 22);
        }

        for (int i = -1; i <= 1; ++i) {
            int index = selectedSlot - i;
            if (index < 0 || index >= items.size())
                continue;
            ItemStack itemStack = items.get(index);
            int y = scaledHeight - 19;
            int x = scaledWidth / 2 - i * 20 + 3 + handXOffset;

            renderHotbarItem(context, x, y, deltaTracker, this.client.player, itemStack, 0);
        }

        context.blit(WIDGET_TEXTURE,
                scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.popPose();

        RenderSystem.disableBlend();
    }

    // TODO: replace with vanilla methods
    private void renderHotbarItem(GuiGraphics context, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack,
                                  int seed) {
        if (stack.isEmpty()) {
            return;
        }

        float g = (float) stack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            context.pose().pushPose();
            context.pose().translate(x + 8, y + 12, 0.0f);
            context.pose().scale(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            context.pose().translate(-(x + 8), -(y + 12), 0.0f);
        }
        context.renderItem(player, stack, x, y, seed);
        if (g > 0.0f) {
            context.pose().popPose();
        }
        drawItemCountInSlot(context, this.client.font, stack, x, y);
    }

    public void drawItemCountInSlot(GuiGraphics context, Font textRenderer, ItemStack stack, int x, int y) {
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
            BankScreen.drawItemCount(context, textRenderer, stack.getCount(), x, y, false);
//            String count = ItemCountUtils.toConsiseString(stack.getCount());
//            String string = count;
//            matrices.translate(0.0f, 0.0f, 200.0f);
//            float scale = ItemCountUtils.scale(string);
//
//            int textWidth = (int) (textRenderer.width(string));
//
//            int xOffset = x + 18 - 2;
//            int yOffset = y + 18 - 2;
//            matrices.pushPose();
//            matrices.translate(xOffset, yOffset, 0);
//            matrices.scale(scale, scale, 1);
//            matrices.translate(-xOffset, -yOffset, 0);
//            context.drawString(textRenderer, string, x + 18 - 1 - textWidth, y + 9, 0xFFFFFF, true);
//            matrices.popPose();
        }

        matrices.popPose();
    }

    public short nextRevision() {
        return ++this.revision;
    }
}
