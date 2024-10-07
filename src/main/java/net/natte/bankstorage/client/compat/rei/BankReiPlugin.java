package net.natte.bankstorage.client.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import net.natte.bankstorage.client.screen.BankScreen;

@REIPluginClient
public class BankReiPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new BankDraggableStackVisitor());
        registry.registerFocusedStack(new BankFocusedStackProvider());
        registry.exclusionZones().register(BankScreen.class, new BankExclusionZone());
    }
}
