package net.natte.bankstorage.client.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.natte.bankstorage.client.screen.BankScreen;

@EmiEntrypoint
public class BankEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addDragDropHandler(BankScreen.class, new BankDragDropHandler());
        registry.addStackProvider(BankScreen.class, new BankStackProvider());
        registry.addExclusionArea(BankScreen.class, new BankScreenExclusionArea());
    }
}
