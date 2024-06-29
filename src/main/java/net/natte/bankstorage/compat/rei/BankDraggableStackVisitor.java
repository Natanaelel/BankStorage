package net.natte.bankstorage.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.inventory.BankSlot;
import net.natte.bankstorage.packet.server.LockSlotPacketC2S;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.stream.Stream;

// rei's rendering of the yellow overlay is pretty slow...
// some solution could be to always render over *all* slots as 1 big rectangle
public class BankDraggableStackVisitor implements DraggableStackVisitor<BankScreen> {

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof BankScreen;
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<BankScreen> context, DraggableStack stack) {
        if (!(context.getScreen().getSlotUnderMouse() instanceof BankSlot bankSlot))
            return DraggedAcceptorResult.PASS;

        if (!(stack.getStack().getValue() instanceof ItemStack draggedItem))
            return DraggedAcceptorResult.PASS;


        if (draggedItem.isEmpty())
            return DraggedAcceptorResult.PASS;

        BankScreen screen = context.getScreen();

        // optimistically lock slot on client, will be synced later
        screen.getMenu().lockSlot(bankSlot.index, draggedItem);

        PacketDistributor.sendToServer(new LockSlotPacketC2S(screen.getMenu().containerId, bankSlot.index, draggedItem, true));

        return DraggedAcceptorResult.ACCEPTED;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<BankScreen> context, DraggableStack stack) {
        if (!(stack.getStack().getValue() instanceof ItemStack draggedItem))
            return Stream.empty();

        if (draggedItem.isEmpty())
            return Stream.empty();


        BankScreen screen = context.getScreen();
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();

        return screen
                .getMenu()
                .slots
                .stream()
                .filter(slot -> slot instanceof BankSlot bankSlot && (slot.getItem().isEmpty() || !bankSlot.isLocked() && ItemStack.isSameItemSameComponents(slot.getItem(), draggedItem)))
                .map(slot -> DraggableStackVisitor.BoundsProvider.ofRectangle(new Rectangle(left + slot.x, top + slot.y, 16, 16)));
    }


}

