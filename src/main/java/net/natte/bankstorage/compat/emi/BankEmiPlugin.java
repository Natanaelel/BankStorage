package net.natte.bankstorage.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.natte.bankstorage.client.screen.BankScreen;

@EmiEntrypoint
public class BankEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addDragDropHandler(BankScreen.class, new BankDragDropHandler());
    }
}
