package net.natte.bankstorage.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.BankStorage;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.options.BankOptions;
import net.natte.bankstorage.options.BuildMode;
import net.natte.bankstorage.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BuildModePreviewRenderer {

    private static final ResourceLocation WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    @Nullable
    public UUID uuid;
    private Minecraft client;

    private CachedBankStorage bankStorage;

    // optimistic, meaning this can be more up to date than the item component
    public int selectedSlot;

    public InteractionHand renderingFromHand;
    private HumanoidArm arm;
    private HumanoidArm mainArm;

    private int ticks = 0;
    private boolean hasBank = false;
    private ItemStack bankItem = ItemStack.EMPTY;
    private BuildMode buildMode;

    public void tick() {
        if (this.client == null)
            this.client = Minecraft.getInstance();

        if (client.player == null)
            return;

        updateBank();

        // request cache sync every 2 seconds if holding bank in buildmode
        if (this.hasBank && this.ticks++ % 40 == 0)
            CachedBankStorage.requestCacheUpdate(this.uuid);
    }

    private void updateBank() {
        boolean hadBank = this.hasBank;
        ItemStack oldBank = this.bankItem;

        if (canRenderFrom(this.client.player.getMainHandItem())) {
            this.bankItem = this.client.player.getMainHandItem();
            this.renderingFromHand = InteractionHand.MAIN_HAND;
            this.hasBank = true;
        } else if (canRenderFrom(this.client.player.getOffhandItem())) {
            this.bankItem = this.client.player.getOffhandItem();
            this.renderingFromHand = InteractionHand.OFF_HAND;
            this.hasBank = true;
        } else
            this.hasBank = false;

        if (this.hasBank) {
            this.uuid = Util.getUUID(this.bankItem);
            this.buildMode = this.bankItem.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT).buildMode();
            this.mainArm = this.client.player.getMainArm();
            this.arm = this.renderingFromHand == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
            if (CachedBankStorage.markDirtyForPreview) {
                this.bankStorage = CachedBankStorage.getBankStorage(uuid);
                CachedBankStorage.markDirtyForPreview = false;
            }
            if (oldBank != this.bankItem) {
                this.selectedSlot = this.bankItem.getOrDefault(BankStorage.SelectedSlotComponentType, 0);
                this.bankStorage = CachedBankStorage.getBankStorage(uuid);
            }
            if (this.bankStorage != null) {
                this.selectedSlot = Mth.clamp(this.selectedSlot, 0, this.bankStorage.getBlockItems().size() - 1);
            }
        }
    }

    private boolean canRenderFrom(ItemStack stack) {
        if (!Util.isBankLike(stack))
            return false;
        if (!Util.hasUUID(stack))
            return false;
        if (stack.getOrDefault(BankStorage.OptionsComponentType, BankOptions.DEFAULT).buildMode() == BuildMode.NONE)
            return false;
        CachedBankStorage cachedBankStorage = CachedBankStorage.getBankStorage(stack);
        if (cachedBankStorage == null)
            return false;
        return true;
    }

    public void render(GuiGraphics context) {
        if (!canRender())
            return;

        switch (this.buildMode) {
            case NORMAL -> renderBlockPreview(context);
            case RANDOM -> renderRandomPreview(context);
        }

    }

    private boolean canRender() {
        if (!this.hasBank)
            return false;
        if (this.client == null)
            return false;
        if (this.bankStorage == null)
            return false;

        return true;
    }

    private void renderRandomPreview(GuiGraphics context) {

        List<ItemStack> items = this.bankStorage.getBlockItems();

        if (items.isEmpty())
            return;


        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();

        int handXOffset = this.arm == HumanoidArm.LEFT ? -169 : 118;
        if (mainArm == HumanoidArm.LEFT)
            handXOffset += 29;

        // draw slot background
        context.blit(WIDGET_TEXTURE,
                scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64 + 62, 0, 62, 22);

        // draw item
        ItemStack itemStack = items.get((ticks / 20) % items.size());
        int y = scaledHeight - 19;
        int x = scaledWidth / 2 + 3 + handXOffset;

        renderHotbarItem(context, x, y, itemStack, 0);

        // draw selection square
        context.blit(WIDGET_TEXTURE, scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.popPose();

        RenderSystem.disableBlend();
    }

    private void renderBlockPreview(GuiGraphics context) {
        List<ItemStack> items = this.bankStorage.getBlockItems();
        if (items.isEmpty())
            return;


        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();

        int handXOffset = this.arm == HumanoidArm.LEFT ? -169 : 118;
        if (mainArm == HumanoidArm.LEFT)
            handXOffset += 29;

        if (items.size() == 1) {
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 + handXOffset, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == 0 || selectedSlot == items.size() - 1) {
            boolean isLeft = selectedSlot > 0;
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

            renderHotbarItem(context, x, y, itemStack, i);
        }

        context.blit(WIDGET_TEXTURE,
                scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.popPose();

        RenderSystem.disableBlend();
    }

    private void renderHotbarItem(GuiGraphics context, int x, int y, ItemStack stack,
                                  int seed) {
        if (stack.isEmpty()) {
            return;
        }
        context.renderItem(stack, x, y, seed);
        if (stack.getCount() != 1)
            BankScreen.drawItemCount(context, this.client.font, stack.getCount(), x, y, false);
        // copyWithCount(1) and text null to force not render count
        context.renderItemDecorations(this.client.font, stack.copyWithCount(1), x, y, null);
    }

    @Nullable
    public ItemStack getItem() {
        return this.bankItem;
    }

    public boolean hasBank() {
        return this.hasBank;
    }

    public CachedBankStorage getStorage() {
        return bankStorage;
    }
}
