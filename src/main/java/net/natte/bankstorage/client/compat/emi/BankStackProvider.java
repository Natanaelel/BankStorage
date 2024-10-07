package net.natte.bankstorage.client.compat.emi;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.natte.bankstorage.client.screen.BankScreen;
import net.natte.bankstorage.inventory.BankSlot;

public class BankStackProvider implements EmiStackProvider<BankScreen> {

    @Override
    public EmiStackInteraction getStackAt(BankScreen screen, int x, int y) {
        if (screen.getSlotUnderMouse() instanceof BankSlot bankSlot) {
            if (bankSlot.isLocked()) {
                return new EmiStackInteraction(EmiStack.of(bankSlot.getLockedStack()), null, false);
            }
            if (bankSlot.getItem().getCount() > 99)
                return new EmiStackInteraction(EmiStack.of(bankSlot.getItem().copyWithCount(64)), null, false);
        }
        return EmiStackInteraction.EMPTY;
    }
}
