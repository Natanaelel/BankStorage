package net.natte.bankstorage.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;

public class BankDragDropHandler implements EmiDragDropHandler<BankScreen> {

    @Override
    public boolean dropStack(BankScreen screen, EmiIngredient ingredient, int x, int y) {

        if (!(screen.getSlotUnderMouse() instanceof BankSlot bankSlot))
            return false;

        ItemStack draggedItem = ingredient.getEmiStacks().getFirst().getItemStack();

        if (draggedItem.isEmpty())
            return false;

        // optimistically lock slot on client, will be synced later
        screen.getMenu().lockSlot(bankSlot.index, draggedItem);

        screen.getMinecraft().getConnection().send(new LockSlotPacketC2S(screen.getMenu().containerId, bankSlot.index, draggedItem, true));

        return true;
    }

    @Override
    public void render(BankScreen screen, EmiIngredient dragged, GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (Slot slot : screen.getMenu().slots) {
            if (slot instanceof BankSlot && (slot.getItem().isEmpty() || ItemStack.isSameItemSameComponents(slot.getItem(), dragged.getEmiStacks().getFirst().getItemStack()))) {
                int x = screen.getGuiLeft() + slot.x;
                int y = screen.getGuiTop() + slot.y;
                guiGraphics.fill(x, y, x + 16, y + 16, 0x8822BB33);
            }
        }
    }
}
