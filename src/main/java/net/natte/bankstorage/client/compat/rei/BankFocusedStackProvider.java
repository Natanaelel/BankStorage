package net.natte.bankstorage.client.compat.rei;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.inventory.BankSlot;

public class BankFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {
        if (screen instanceof BankScreen bankScreen && bankScreen.getSlotUnderMouse() instanceof BankSlot bankSlot && bankSlot.isLocked())
            return CompoundEventResult.interruptTrue(EntryStacks.of(bankSlot.getLockedStack()));

        return CompoundEventResult.pass();
    }
}
