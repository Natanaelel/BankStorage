package net.natte.bankstorage.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
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
}
