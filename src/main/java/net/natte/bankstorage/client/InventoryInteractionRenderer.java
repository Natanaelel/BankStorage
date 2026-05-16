package net.natte.bankstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.container.BankType;
import net.natte.bankstorage.container.CachedBankStorage;
import net.natte.bankstorage.item.BankStashResult;
import net.natte.bankstorage.screen.BankScreenHandler;
import net.natte.bankstorage.util.Util;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Collections;
import java.util.Optional;

/**
 * Renders stash indicators on bank item slots in the player's inventory when
 * an item is held on the cursor.
 *
 * <p>The in-inventory stash UX is inspired by Sophisticated Backpacks, implemented
 * here against BankStorage's own storage, cache, and synchronization model.
 *
 * <p>Green "+" means the bank already has the item and can merge it.
 * Yellow "+" means the bank has space but doesn't yet have this item type.
 * No indicator means the bank has no space for this item.
 *
 * <p>When the mouse hovers over a compatible bank slot, a tooltip is shown
 * instructing the player to right-click to insert.
 */
public class InventoryInteractionRenderer {

    private static final Component RIGHT_CLICK_TOOLTIP = Component.translatable(
            "tooltip.bankstorage.right_click_to_insert").withStyle(ChatFormatting.GRAY);

    public static void onDrawScreen(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)
                || mc.screen instanceof CreativeModeInventoryScreen
                || mc.player == null) {
            return;
        }

        AbstractContainerMenu menu = containerScreen.getMenu();

        // Don't render indicators inside the BankScreen itself; the bank's own
        // slots have their own interaction logic.
        if (menu instanceof BankScreenHandler) {
            return;
        }

        ItemStack cursorStack = menu.getCarried();
        if (cursorStack.isEmpty()) {
            return;
        }

        Slot slotUnderMouse = containerScreen.getSlotUnderMouse();

        for (Slot slot : menu.slots) {
            ItemStack slotStack = slot.getItem();
            if (!slot.mayPickup(mc.player) || slotStack.isEmpty() || !Util.isBankLike(slotStack)) {
                continue;
            }

            CachedBankStorage cached = CachedBankStorage.getAndThrottleUpdate(slotStack, 20);
            if (cached == null) {
                continue;
            }

            BankType type = Util.getType(slotStack);
            BankStashResult stashResult = cached.getItemStashable(cursorStack, type.stackLimit);
            if (stashResult == BankStashResult.NO_SPACE) {
                continue;
            }

            int x = containerScreen.getGuiLeft() + slot.x;
            int y = containerScreen.getGuiTop() + slot.y;

            int color = stashResult == BankStashResult.MATCH_AND_SPACE
                    ? ChatFormatting.GREEN.getColor()
                    : ChatFormatting.YELLOW.getColor();

            PoseStack poseStack = event.getGuiGraphics().pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 300);

            event.getGuiGraphics().drawString(mc.font, "+", x + 10, y + 8, color);

            poseStack.popPose();

            if (slot == slotUnderMouse) {
                renderTooltip(event, mc, x, y);
            }
        }
    }

    private static void renderTooltip(ScreenEvent.Render.Post event, Minecraft mc, int x, int y) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 400);
        guiGraphics.renderTooltip(mc.font, Collections.singletonList(RIGHT_CLICK_TOOLTIP),
                Optional.empty(), event.getMouseX(), event.getMouseY());
        poseStack.popPose();
    }
}
